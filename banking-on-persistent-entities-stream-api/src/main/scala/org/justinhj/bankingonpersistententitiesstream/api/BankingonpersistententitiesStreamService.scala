package org.justinhj.bankingonpersistententitiesstream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

/**
  * The banking-on-persistent-entities stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BankingonpersistententitiesStream service.
  */
trait BankingonpersistententitiesStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("banking-on-persistent-entities-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

