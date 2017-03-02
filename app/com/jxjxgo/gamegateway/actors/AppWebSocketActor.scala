package com.jxjxgo.gamegateway.actors

import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.Map.Entry
import java.util.concurrent.ConcurrentHashMap

import akka.actor._
import com.jxjxgo.common.edecrypt.DESUtils
import com.jxjxgo.common.helper.{GZipHelper, UUIDHelper}
import com.jxjxgo.gamecenter.rpc.domain._
import com.jxjxgo.gamegateway.domain.ws.req.socketrequest.SocketRequest
import com.jxjxgo.sso.rpc.domain.{SSOServiceEndpoint, SessionResponse}
import com.twitter.util.{Await, Future}
import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by fangzhongwei on 2017/1/21.
  */
object AppWebSocketActor {
  private[this] val logger: Logger = LoggerFactory.getLogger(getClass)
  private[this] val map: ConcurrentHashMap[String, ActorRef] = new ConcurrentHashMap[String, ActorRef]()
  private[this] val memberMap: ConcurrentHashMap[String, (Long, SessionResponse)] = new ConcurrentHashMap[String, (Long, SessionResponse)]()

  val defaultKey = ConfigFactory.load().getString("edecrypt.default.des.key")

  def push(): String = {
    val iterator: util.Iterator[Entry[String, ActorRef]] = map.entrySet().iterator()
    val date: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
    while (iterator.hasNext) {
      val actor: ActorRef = iterator.next().getValue
      actor ! date
    }
    date
  }

  def pushMessage(key: String, array: Array[Byte]) = {
    val ref: ActorRef = map.get(key)
    if (ref != null) ref ! array
    else logger.error(s"did not find actor for key : $key")
  }

  def props(out: ActorRef) = {
    Props(classOf[AppWebSocketActor], out)
  }

  private def putActor(identity: String, actor: ActorRef) = {
    map.put(identity, actor)
  }

  def putMember(identity: String, memberInfo: (Long, SessionResponse)) = {
    memberMap.put(identity, memberInfo)
  }

  private def removeActor(identity: String) = {
    map.remove(identity)
  }

  private def removeMember(identity: String) = {
    memberMap.remove(identity)
  }

  def getMemberInfo(identity: String): (Long, SessionResponse) = {
    memberMap.get(identity)
  }

  def close(identity: String): String = {
    map.get(identity) ! PoisonPill
    "success"
  }

  var ssoClientService: SSOServiceEndpoint[Future] = _
  var gameEndpoint: GameEndpoint[Future] = _


}

class AppWebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  private[this] var token: String = _
  private[this] var memberId: Long = _
  private[this] var socketId: Long = _
  private[this] var deviceType: Int = _
  private[this] var fingerPrint: String = _
  private[this] var ip: Long = _

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    println("actor register ...")
    //todo generate socket id
  }

  def receive = {
    case buffer: Array[Byte] =>
      println(s"receive bytes , length:${buffer.length}")

      var decrypt: Array[Byte] = null
      var uncompress: Array[Byte] = null

      try {
        decrypt = DESUtils.decrypt(buffer, AppWebSocketActor.defaultKey)
        uncompress = GZipHelper.uncompress(decrypt)
      } catch {
        case ex: Exception =>
          log.error(ex, "parse error")
          killSelf
      }

      if (uncompress != null) {
        val r: SocketRequest = SocketRequest.parseFrom(uncompress)
        log.info(s"receive SockerRequest:$r")
        val traceId: String = r.p1
        val tk: String = r.p3
        val fp: String = r.p4
        r.p2 match {
          case "login" =>
            val response: SessionResponse = Await.result(AppWebSocketActor.ssoClientService.touch(traceId, tk))
            response.code match {
              case "0" =>
                response.fingerPrint.equals(fp) match {
                  case true =>
                    token = tk
                    memberId = response.memberId
                    socketId = response.memberId
                    deviceType = response.deviceType
                    fingerPrint = response.fingerPrint
//                    log.info(s"remote ip is:${out.path.address.host}")
//                    ip = IPv4Helper.ipToLong(out.path.address.host.get)
                    ip = response.ip
                    AppWebSocketActor.putMember(tk, (response.memberId, response))
                    AppWebSocketActor.putActor(new StringBuilder(memberId.toString).append('_').append(response.fingerPrint).toString(), out)
                    val generateSocketIdResponse: GenerateSocketIdResponse = Await.result(AppWebSocketActor.gameEndpoint.generateSocketId(traceId))
                    generateSocketIdResponse.code match {
                      case "0" =>
                        AppWebSocketActor.gameEndpoint.playerOnline(traceId, OnlineRequest(generateSocketIdResponse.socketId, memberId, response.ip, response.deviceType, response.fingerPrint, ConfigFactory.load().getString("finagle.thrift.host.port")))
                      case _ =>
                        log.error(s"generateSocketId eroor, code:${generateSocketIdResponse.code}")
                        killSelf
                    }
                  case false =>
                    log.error(s"fingerPrint not matched.")
                    killSelf
                }
              case _ =>
                log.error(s"invalid ws:[tk:$tk]")
                killSelf
            }
          case operate: String =>

            val memberInfo: (Long, SessionResponse) = AppWebSocketActor.getMemberInfo(tk)

            memberInfo == null match {
              case true =>
                log.error("memberInfo not found.")
                killSelf
              case false =>
                checkSession(memberInfo._2) match {
                  case true =>
                    operate match {
                      case "join" =>
                        val gameType: Int = r.p5.toInt
                        log.info("now call game center to join.")
                        val result: GameBaseResponse = Await.result(AppWebSocketActor.gameEndpoint.joinGame(traceId, JoinGameRequest(memberId, socketId, deviceType, fingerPrint, ip, gameType, 0)))
                        log.info(s"join result is : $result")
                      case "playCards" =>
                      case _ =>
                        log.error("unknown operate")
                        killSelf
                    }
                  case false =>
                    log.error("checkSession not pass.")
                    killSelf
                }
            }
        }
      }
  }

  private def checkSession(s: SessionResponse): Boolean = {
    token.equals(s.token) && memberId == s.memberId && socketId != 0 && deviceType == s.deviceType && fingerPrint.equals(s.fingerPrint)
  }

  def killSelf = {
    out ! PoisonPill
  }

  override def postStop() = {
    AppWebSocketActor.removeActor(token)
    AppWebSocketActor.removeMember(token)
    AppWebSocketActor.gameEndpoint.playerOffline(UUIDHelper.generate(), socketId, memberId)
  }
}