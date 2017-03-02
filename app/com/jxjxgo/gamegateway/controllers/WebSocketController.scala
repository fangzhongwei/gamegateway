package com.jxjxgo.gamegateway.controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.jxjxgo.gamegateway.actors.AppWebSocketActor
import com.jxjxgo.sso.rpc.domain.SSOServiceEndpoint
import com.twitter.util.Future
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, RequestHeader, WebSocket}


/**
  * Created by fangzhongwei on 2017/1/21.
  */
@Singleton
class WebSocketController @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  private[this] val logger: Logger = LoggerFactory.getLogger(getClass)

  def greeter = WebSocket.acceptOrResult[Array[Byte], Array[Byte]] { request =>
    scala.concurrent.Future.successful(checkIdentity(request) match {
      case true =>
        Right(ActorFlow.actorRef(AppWebSocketActor.props))
      case false =>
        logger.error(s"Invalid request : $request")
        Left(Forbidden)
    })
  }

  def checkIdentity(request: RequestHeader): Boolean = {
    logger.error(s"socket headers:${request.headers}")
    true
  }

  //  def checkIdentity(request: RequestHeader): Option[(String, Long, SessionResponse)] = {
  //    request.headers.get("TK") match {
  //      case Some(token) =>
  //        request.headers.get("TI") match {
  //          case Some(traceId) =>
  //            request.headers.get("X-Real-Ip") match {
  //              case Some(ip) =>
  //                request.headers.get("FP") match {
  //                  case Some(fingerPrint) =>
  //                    val sessionResponse: SessionResponse = Await.result(ssoClientService.touch(traceId, token))
  //                    sessionResponse.code match {
  //                      case "0" =>
  //                        sessionResponse.fingerPrint.equals(fingerPrint) match {
  //                          case true =>
  //                            Some(traceId, IPv4Helper.ipToLong(ip), sessionResponse)
  //                          case false => None
  //                        }
  //                      case _ => None
  //                    }
  //                  case None => None
  //                }
  //              case None => None
  //            }
  //          case None => None
  //        }
  //      case None => None
  //    }
  //  }

  // Home page that renders template
  def pushTime = Action { implicit request =>
    Ok(AppWebSocketActor.push())
  }

  // Home page that renders template
  def kill = Action { implicit request =>
    Ok(AppWebSocketActor.close("1"))
  }
}
