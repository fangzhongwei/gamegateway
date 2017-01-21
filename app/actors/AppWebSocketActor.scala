package actors

import akka.actor._

/**
  * Created by fangzhongwei on 2017/1/21.
  */
object AppWebSocketActor {
  def props(out: ActorRef) = Props(new AppWebSocketActor(out))
}

class AppWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      println(msg)
      out ! ("I received your message: " + msg)
  }

  override def postStop() = {
    println("me stop ...")
  }
}

//import akka.actor.PoisonPill
//
//self ! PoisonPill