package actors

import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.Map.Entry
import java.util.concurrent.ConcurrentHashMap

import akka.actor._

/**
  * Created by fangzhongwei on 2017/1/21.
  */
object AppWebSocketActor {

  private val map: ConcurrentHashMap[String, ActorRef] = new ConcurrentHashMap[String, ActorRef ]()

  def push():String = {
    val iterator: util.Iterator[Entry[String, ActorRef]] = map.entrySet().iterator()

    val date: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(new Date())
    while (iterator.hasNext) {
      val actor: ActorRef = iterator.next().getValue
      actor ! date
    }

    date
  }

  var identity = 0

  def props(out: ActorRef) = {
    identity = identity + 1
    Props(classOf[AppWebSocketActor], identity.toString, out)
  }

  private def putActor(identity:String, actor:ActorRef) = {
    map.put(identity, actor)
  }

  def close(identity:String):String = {
    map.get(identity) ! PoisonPill
    "success"
  }
}

class AppWebSocketActor(identity:String, out: ActorRef) extends Actor {
  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    println(s"acotor register : $identity")
    AppWebSocketActor.putActor(identity, out)
  }

  def receive = {
    case msg: String =>
      println(msg)
      out ! ("echo: " + msg)
  }

  override def postStop() = {
    println(s"me stop ... identity:$identity")
  }
}

//import akka.actor.PoisonPill
//
//self ! PoisonPill