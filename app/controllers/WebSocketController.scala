package controllers

import javax.inject.{Inject, Singleton}

import actors.AppWebSocketActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, RequestHeader, WebSocket}

import scala.concurrent.Future

/**
  * Created by fangzhongwei on 2017/1/21.
  */
@Singleton
class WebSocketController @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  def greeter = WebSocket.acceptOrResult[Array[Byte], Array[Byte]] { request =>
    Future.successful(checkAuto(request) match {
      case None => Left(Forbidden)
      case Some(_) => Right(ActorFlow.actorRef(AppWebSocketActor.props))
    })
  }

  def checkAuto(request: RequestHeader): Option[String] = {
    val maybeString: Option[String] = request.headers.get("user")
    Some("someuser")
  }

  // Home page that renders template
  def pushTime = Action { implicit request =>
    Ok(AppWebSocketActor.push())
  }

  // Home page that renders template
  def kill = Action { implicit request =>
    Ok(AppWebSocketActor.close("1"))
  }
}
