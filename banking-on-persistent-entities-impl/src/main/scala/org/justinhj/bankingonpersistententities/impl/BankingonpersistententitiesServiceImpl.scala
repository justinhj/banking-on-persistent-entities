package org.justinhj.bankingonpersistententities.impl

import org.justinhj.bankingonpersistententities.api
import org.justinhj.bankingonpersistententities.api.BankingonpersistententitiesService
import akka.Done
import akka.NotUsed
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.justinhj.bankingonpersistententities.impl._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.util.Timeout
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import java.time.Instant
import org.justinhj.bankingonpersistententities.impl._
/**
  * Implementation of the BankingonpersistententitiesService.
  */
class BankingonpersistententitiesServiceImpl(
  clusterSharding: ClusterSharding,
  persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext)
  extends BankingonpersistententitiesService {

  implicit val timeout = Timeout(5.seconds)

  override def bankAccountDeposit(id: String, description: String, amount: Int) : ServiceCall[NotUsed, Int] = ServiceCall {
    _ =>
      // Look up the sharded entity (aka the aggregate instance) for the given ID.
      val ref = persistentEntityRegistry.refFor[BankAccountEntity](id)

      // Ask the aggregate instance the Deposit command.
      ref.ask(DepositCmd(Instant.now, description, amount))
  }

  override def accountTopic(): Topic[api.Account] =
    TopicProducer.singleStreamWithOffset { fromOffset =>
      persistentEntityRegistry
        .eventStream(BankAccountEvent.bankAccountEventTag, fromOffset)
        .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(
    helloEvent: EventStreamElement[BankAccountEvent]
  ): api.Account = {
    ???
    // helloEvent.event match {
    //   case GreetingMessageChanged(msg) =>
    //     api.GreetingMessageChanged(helloEvent.entityId, msg)
    // }
  }
}
