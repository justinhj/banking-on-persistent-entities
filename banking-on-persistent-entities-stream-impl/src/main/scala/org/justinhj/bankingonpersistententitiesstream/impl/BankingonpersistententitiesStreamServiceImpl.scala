package org.justinhj.bankingonpersistententitiesstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import org.justinhj.bankingonpersistententitiesstream.api.BankingonpersistententitiesStreamService
import org.justinhj.bankingonpersistententities.api.BankingonpersistententitiesService

import scala.concurrent.Future

/**
  * Implementation of the BankingonpersistententitiesStreamService.
  */
class BankingonpersistententitiesStreamServiceImpl(bankingonpersistententitiesService: BankingonpersistententitiesService) extends BankingonpersistententitiesStreamService {
  def stream = ServiceCall { x =>
    ??? // Future.successful(hellos.mapAsync(8)(bankingonpersistententitiesService.hello(_).invoke()))
  }
}
