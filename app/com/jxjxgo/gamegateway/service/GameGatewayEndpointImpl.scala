package com.jxjxgo.gamegateway.service

import javax.inject.Inject

import com.jxjxgo.gamegateway.actors.AppWebSocketActor
import com.jxjxgo.gamegateway.rpc.domain.{GameGatewayBaseResponse, GameGatewayEndpoint, SocketResponse}
import com.jxjxgo.scrooge.thrift.template.ScroogeThriftServerTemplate
import com.twitter.util.Future
import play.inject.Injector

import scala.concurrent.Promise

/**
  * Created by fangzhongwei on 2017/2/13.
  */
class GameGatewayEndpointImpl @Inject() (injector: Injector) extends GameGatewayEndpoint[Future] {
  override def push(traceId: String, r: SocketResponse): Future[GameGatewayBaseResponse] = {
    //    1: string code,
    //    2: string action,
    //    3: i64 gameId = 0,
    //    4: i32 gameType = 0,
    //    5: i32 deviceType = 0,
    //    6: string cards = "",
    //    7: string landlordCards = "",
    //    8: i32 baseAmount = 0,
    //    9: i32 multiples = 0,
    //    10: string previousNickname = "",
    //    11: i32 previousCardsCount = 0,
    //    12: string nextNickname = "",
    //    13: i32 nextCardsCount = 0,
    //    14: bool choosingLandlord = false,
    //    15: bool landlord = false,
    //    16: bool turnToPlay = false,
    //    17: string fingerPrint = "",
    AppWebSocketActor.pushMessage(new StringBuilder(r.p3).append('_').append(r.p17).toString(),
      com.jxjxgo.gamegateway.domain.ws.resp.socketresponse.SocketResponse(r.p1, r.p2, r.p3, r.p4, r.p5, r.p6, r.p7, r.p8, r.p9, r.p10, r.p11, r.p12, r.p13, r.p14, r.p15, r.p16, r.p17, r.p18, r.p19, r.p20, r.p21, r.p22).toByteArray)
    Future.value(GameGatewayBaseResponse("0"))
  }

  def startRpc():scala.concurrent.Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    scala.concurrent.Future {
      injector.instanceOf(classOf[ScroogeThriftServerTemplate]).init
      promise.success()
    }
    promise.future
  }

  startRpc()

}
