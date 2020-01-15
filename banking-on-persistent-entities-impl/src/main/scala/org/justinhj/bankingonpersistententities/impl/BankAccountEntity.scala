package org.justinhj.bankingonpersistententities.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}
import scala.collection.immutable.Seq
import java.time.Instant

sealed trait WithdrawalResponse
case class SuccessfulWithdrawalResponse(newBalance: Int) extends WithdrawalResponse
case class FailedWithdrawalResponse(message: String) extends WithdrawalResponse

sealed trait BankAccountCommand[R] extends ReplyType[R]
final case class DepositCmd(time: Instant, description: String, amount: Int)
    extends BankAccountCommand[Int]
final case class WithdrawCmd(time: Instant, description: String, amount: Int)
    extends BankAccountCommand[WithdrawalResponse]
final case class AssignAccountHolderCmd(time: Instant, accountHolder: String)
    extends BankAccountCommand[Done]

sealed trait BankAccountEvent extends AggregateEvent[BankAccountEvent] {
  override def aggregateTag = BankAccountEvent.bankAccountEventTag
}

object BankAccountEvent {
  val bankAccountEventTag = AggregateEventTag[BankAccountEvent]
}

case class DepositEvt(time: Instant, description: String, amount: Int)
    extends BankAccountEvent
case class WithdrawEvt(time: Instant, description: String, amount: Int)
    extends BankAccountEvent
case class AssignAccountHolderEvt(time: Instant, accountHolder: String)
    extends BankAccountEvent

case class Account(balance: Int, accountHolder: Option[String])

object SuccessfulWithdrawalResponse {
    implicit val format: Format[SuccessfulWithdrawalResponse] = Json.format
}

object FailedWithdrawalResponse {
    implicit val format: Format[FailedWithdrawalResponse] = Json.format
}

object Account {
    implicit val format: Format[Account] = Json.format
  }

object WithdrawEvt {
    implicit val format: Format[WithdrawEvt] = Json.format
  }

object DepositEvt {
    implicit val format: Format[DepositEvt] = Json.format
  }

object AssignAccountHolderEvt {
    implicit val format: Format[AssignAccountHolderEvt] = Json.format
  }

object WithdrawCmd {
    implicit val format: Format[WithdrawCmd] = Json.format
  }

object DepositCmd {
    implicit val format: Format[DepositCmd] = Json.format
  }

object AssignAccountHolderCmd {
  implicit val format: Format[AssignAccountHolderCmd] = Json.format
}

object BankAccountSerializerRegistry extends JsonSerializerRegistry {
    override def serializers: Seq[JsonSerializer[_]] = Seq(
      JsonSerializer[WithdrawEvt],
      JsonSerializer[DepositEvt],
      JsonSerializer[AssignAccountHolderEvt],
      JsonSerializer[WithdrawCmd],
      JsonSerializer[DepositCmd],
      JsonSerializer[AssignAccountHolderCmd],
      JsonSerializer[Account],
      JsonSerializer[SuccessfulWithdrawalResponse],
      JsonSerializer[FailedWithdrawalResponse]
    )
  }

class BankAccountEntity extends PersistentEntity {
    override type Command = BankAccountCommand[_]
    override type Event = BankAccountEvent
    override type State = Account

    override def initialState : State = Account(0, None)

    override def behavior = {

        Actions().onCommand[DepositCmd, Int]{
            case (DepositCmd(time, description, amount), ctx, state) =>
                // We're going to cheat a little here and prevent negative deposits
                // It should really be an error...
                val safeAmount = Math.max(0, amount)
                ctx.thenPersist(DepositEvt(time, description, safeAmount)){ _ =>
                    ctx.reply(state.balance + safeAmount)
                }
        }.onCommand[WithdrawCmd, WithdrawalResponse]{
          case (WithdrawCmd(time, description, amount), ctx, state) =>
            if(state.balance - amount >= 0)
              ctx.thenPersist(WithdrawEvt(time, description, amount)){ _ =>
                ctx.reply(SuccessfulWithdrawalResponse(state.balance - amount))
              }
            else {
              ctx.reply(FailedWithdrawalResponse(s"Insufficient funds (current balance ${state.balance})"))
              ctx.done
            }
        }.onCommand[AssignAccountHolderCmd, Done]{
          case (AssignAccountHolderCmd(time, accountHolder), ctx, state) =>
            ctx.thenPersist(AssignAccountHolderEvt(time, accountHolder)){
              _ =>
                ctx.reply(Done)
            }
        }
        .onEvent {
            case (WithdrawEvt(time, description, amount), state) =>
                Account(state.balance - amount, state.accountHolder)
            case (DepositEvt(time, description, amount), state) =>
                Account(state.balance + amount, state.accountHolder)
            case (AssignAccountHolderEvt(time, accountHolder), state) =>
                Account(state.balance, Some(accountHolder))
        }
    }

}