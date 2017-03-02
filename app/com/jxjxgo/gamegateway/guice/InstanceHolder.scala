package com.jxjxgo.gamegateway.guice

import javax.inject.Inject

import com.jxjxgo.gamecenter.rpc.domain.GameEndpoint
import com.jxjxgo.gamegateway.actors.AppWebSocketActor
import com.jxjxgo.sso.rpc.domain.SSOServiceEndpoint
import com.twitter.util.Future

/**
  * Created by fangzhongwei on 2017/3/2.
  */
trait InstanceHolder {

}

class InstanceHolderImpl @Inject()(ssoClientService: SSOServiceEndpoint[Future], gameEndpoint:GameEndpoint[Future]) extends InstanceHolder {
  AppWebSocketActor.ssoClientService = ssoClientService
  AppWebSocketActor.gameEndpoint = gameEndpoint
}
