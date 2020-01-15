package org.justinhj.bankingonpersistententitiesstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import org.justinhj.bankingonpersistententitiesstream.api.BankingonpersistententitiesStreamService
import org.justinhj.bankingonpersistententities.api.BankingonpersistententitiesService
import com.softwaremill.macwire._

class BankingonpersistententitiesStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new BankingonpersistententitiesStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BankingonpersistententitiesStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[BankingonpersistententitiesStreamService])
}

abstract class BankingonpersistententitiesStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[BankingonpersistententitiesStreamService](wire[BankingonpersistententitiesStreamServiceImpl])

  // Bind the BankingonpersistententitiesService client
  lazy val bankingonpersistententitiesService: BankingonpersistententitiesService = serviceClient.implement[BankingonpersistententitiesService]
}
