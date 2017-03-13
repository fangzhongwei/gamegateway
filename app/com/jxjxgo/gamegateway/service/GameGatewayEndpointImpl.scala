package com.jxjxgo.gamegateway.service

import javax.inject.Inject

import com.jxjxgo.gamegateway.actors.AppWebSocketActor
import com.jxjxgo.gamegateway.rpc.domain.{GameGatewayBaseResponse, GameGatewayEndpoint, SocketResponse}
import com.jxjxgo.scrooge.thrift.template.ScroogeThriftServerTemplate
import com.twitter.util.Future
import org.slf4j.{Logger, LoggerFactory}
import play.inject.Injector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

/**
  * Created by fangzhongwei on 2017/2/13.
  */
class GameGatewayEndpointImpl @Inject()(injector: Injector) extends GameGatewayEndpoint[Future] {
  private[this] val logger: Logger = LoggerFactory.getLogger(getClass)
  override def push(traceId: String, r: SocketResponse): Future[GameGatewayBaseResponse] = {

    //    1: string code,
    //    2: string action,
    //    3: i64 gameId = 0,
    //    4: i32 gameType = 0,
    //    5: i32 deviceType = 0,
    //    6: string cards = "",
    //    7: string landlordCards = "",
    //    8: string proCardsInfo = "",
    //    9: i32 baseAmount = 0,
    //    10: i32 multiples = 0,
    //    11: string previousNickname = "",
    //    12: i32 previousCardsCount = 0,
    //    13: string nextNickname = "",
    //    14: i32 nextCardsCount = 0,
    //    15: string playStatus = "",
    //    16: bool landlord = false,
    //    17: string fingerPrint = "",
    //    18: long memberId = 0,
    //    19: long seatId = 0,
    logger.info(s"receive socket push message:$r")
    AppWebSocketActor.pushMessage(new StringBuilder(r.p18).append('_').append(r.p17).toString(),
      com.jxjxgo.gamegateway.domain.ws.resp.socketresponse.SocketResponse(r.p1, r.p2, r.p3, r.p4, r.p5, r.p6, r.p7, r.p8, r.p9, r.p10, r.p11, r.p12, r.p13, r.p14, r.p15, r.p16, r.p17, r.p18, r.p19, r.p20, r.p21, r.p22).toByteArray)
    Future.value(GameGatewayBaseResponse("0"))
  }

  def startRpc(): scala.concurrent.Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    scala.concurrent.Future {
      injector.instanceOf(classOf[ScroogeThriftServerTemplate]).init
      logger.info("startRpc success.")
      promise.success()
    }
    promise.future
  }

  startRpc()

}
