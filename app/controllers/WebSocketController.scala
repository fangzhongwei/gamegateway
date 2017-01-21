package controllers

import javax.inject.{Inject, Singleton}

import actors.AppWebSocketActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}

import scala.concurrent.Future

/**
  * Created by fangzhongwei on 2017/1/21.
  */
@Singleton
class WebSocketController  @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {
  def ws2 = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => AppWebSocketActor.props(out))
  }

  def socket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(request.headers.get("user") match {
      case None => Left(Forbidden)
      case Some(_) => Right(ActorFlow.actorRef(AppWebSocketActor.props))
    })
  }

}
