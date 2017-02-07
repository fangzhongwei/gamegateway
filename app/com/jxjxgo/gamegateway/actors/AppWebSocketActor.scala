package com.jxjxgo.gamegateway.actors

import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.Map.Entry
import java.util.concurrent.ConcurrentHashMap

import akka.actor._
import com.jxjxgo.common.edecrypt.DESUtils
import com.jxjxgo.common.helper.GZipHelper
import com.jxjxgo.gamegateway.domain.ws.req.socketrequest.SocketRequest
import com.jxjxgo.sso.rpc.domain.SessionResponse
import com.typesafe.config.ConfigFactory

/**
  * Created by fangzhongwei on 2017/1/21.
  */
object AppWebSocketActor {
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
}

class AppWebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  private var login = false
  private var token: String = ""

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    println("actor register ...")
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
        r.p2 match {
          case "login" =>
            val tk: String = r.p3
            val info: (Long, SessionResponse) = AppWebSocketActor.getMemberInfo(tk)
            info == null match {
              case false =>
                login = true
                token = info._2.token
              case true => log.error(s"invalid ws:[tk:$tk]")
                killSelf
            }
          case operate: String =>
            checkSession match {
              case true =>
                operate match {
                  case "join" =>
                    val traceId: String = r.p1
                    traceId
                  case "playCards" =>
                  case _ =>
                    log.error("unknown operate")
                    killSelf
                }
              case false =>
                log.error("operate without login")
                killSelf
            }
        }
      }
  }

  private def checkSession = {
    login && !token.equals("")
  }

  def killSelf = {
    out ! PoisonPill
  }

  override def postStop() = {
    AppWebSocketActor.removeActor(token)
    AppWebSocketActor.removeMember(token)
    println(s"me stop ...")
  }
}