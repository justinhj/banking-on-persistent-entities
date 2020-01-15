package org.justinhj.bankingonpersistententities.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.lightbend.lagom.scaladsl.api.transport.Method
import julienrf.json.derived
import play.api.libs.json._

/**
  * The banking-on-persistent-entities service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BankingonpersistententitiesService.
  */
trait BankingonpersistententitiesService extends Service {

  def bankAccountDeposit(id: String, description: String, amount: Int) : ServiceCall[NotUsed, Int]
  def bankAccountWithdrawal(id: String, description: String, amount: Int) : ServiceCall[NotUsed, WithdrawalResponse]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("banking-on-persistent-entities")
      .withCalls(
        restCall(Method.PUT, "/api/bankaccount/deposit/:id/:description/:amount", bankAccountDeposit _),
        restCall(Method.PUT, "/api/bankaccount/withdraw/:id/:description/:amount", bankAccountWithdrawal _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

case class Account(id: String, balance: Int, accountHolder: Option[String])

object Account {
  implicit val format: Format[Account] = Json.format[Account]
}

sealed trait WithdrawalResponse
case class SuccessfulWithdrawalResponse(newBalance: Int) extends WithdrawalResponse
case class FailedWithdrawalResponse(message: String) extends WithdrawalResponse

object SuccessfulWithdrawalResponse {
  implicit val format: Format[SuccessfulWithdrawalResponse] = Json.format[SuccessfulWithdrawalResponse]
}

object FailedWithdrawalResponse {
  implicit val format: Format[FailedWithdrawalResponse] = Json.format[FailedWithdrawalResponse]
}

object WithdrawalResponse {
  implicit val format: Format[WithdrawalResponse] =
    derived.flat.oformat((__ \ "type").format[String])
}