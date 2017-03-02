package com.jxjxgo.gamegateway.guice

import java.util

import com.google.inject.{AbstractModule, TypeLiteral}
import com.google.inject.name.Names
import com.jxjxgo.common.helper.ConfigHelper
import com.jxjxgo.common.rabbitmq.{RabbitmqProducerTemplate, RabbitmqProducerTemplateImpl}
import com.jxjxgo.common.redis.{RedisClientTemplate, RedisClientTemplateImpl}
import com.jxjxgo.gamecenter.rpc.domain.GameEndpoint
import com.jxjxgo.gamegateway.service.GameGatewayEndpointImpl
import com.jxjxgo.scrooge.thrift.template.{ScroogeThriftServerTemplate, ScroogeThriftServerTemplateImpl}
import com.jxjxgo.sso.rpc.domain.SSOServiceEndpoint
import com.twitter.finagle.Thrift
import com.twitter.scrooge.ThriftService
import com.twitter.util.Future
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by fangzhongwei on 2017/1/22.
  */
class AppModule extends AbstractModule{
  override def configure(): Unit = {
    val map: util.HashMap[String, String] = ConfigHelper.configMap
    Names.bindProperties(binder(), map)
    val config: Config = ConfigFactory.load()
    bind(new TypeLiteral[SSOServiceEndpoint[Future]]() {}).toInstance(Thrift.client.newIface[SSOServiceEndpoint[Future]](config.getString("sso.thrift.host.port")))
    bind(new TypeLiteral[GameEndpoint[Future]]() {}).toInstance(Thrift.client.newIface[GameEndpoint[Future]](config.getString("gamecenter.thrift.host.port")))
    bind(classOf[RedisClientTemplate]).to(classOf[RedisClientTemplateImpl]).asEagerSingleton()
    bind(classOf[RabbitmqProducerTemplate]).to(classOf[RabbitmqProducerTemplateImpl]).asEagerSingleton()
    bind(classOf[InstanceHolder]).to(classOf[InstanceHolderImpl]).asEagerSingleton()

    bind(classOf[ThriftService]).to(classOf[GameGatewayEndpointImpl]).asEagerSingleton()
    bind(classOf[ScroogeThriftServerTemplate]).to(classOf[ScroogeThriftServerTemplateImpl]).asEagerSingleton()
  }
}
