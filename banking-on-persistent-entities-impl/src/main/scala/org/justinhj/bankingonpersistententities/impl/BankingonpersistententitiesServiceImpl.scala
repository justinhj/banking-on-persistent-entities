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

  override def getBankAccount(id: String) : ServiceCall[NotUsed, api.Account] = ServiceCall {
    _ =>
      val ref = persistentEntityRegistry.refFor[BankAccountEntity](id)
      ref.ask(GetAccountCmd).map {
        account =>
          api.Account(id, account.balance, account.accountHolder)
      }
  }

  override def bankAccountDeposit(id: String, description: String, amount: Int) : ServiceCall[NotUsed, Int] = ServiceCall {
    _ =>
      val ref = persistentEntityRegistry.refFor[BankAccountEntity](id)
      ref.ask(DepositCmd(Instant.now, description, amount))
  }

  override def bankAccountWithdrawal(id: String, description: String, amount: Int) : ServiceCall[NotUsed, api.WithdrawalResponse] = ServiceCall {
    _ =>
      val ref = persistentEntityRegistry.refFor[BankAccountEntity](id)
      ref.ask(WithdrawCmd(Instant.now, description, amount)).map { event =>
        event match {
          case SuccessfulWithdrawalResponse(newBalance) =>
            api.SuccessfulWithdrawalResponse(newBalance)
          case FailedWithdrawalResponse(message) =>
            api.FailedWithdrawalResponse(message)
        }
      }
  }

  override def bankAccountAssignHolder(id: String, holder: String) : ServiceCall[NotUsed, Done] = ServiceCall {
    _ =>
      val ref = persistentEntityRegistry.refFor[BankAccountEntity](id)
      ref.ask(AssignAccountHolderCmd(Instant.now, holder))
  }

}
