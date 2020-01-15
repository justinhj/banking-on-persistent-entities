package org.justinhj.bankingonpersistententities.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import java.time.Instant
import akka.Done

class BankAccountEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

    private val system = ActorSystem("BankAccountEntitySpec",
        JsonSerializerRegistry.actorSystemSetupFor(BankAccountSerializerRegistry))

    override protected def afterAll(): Unit = {
        TestKit.shutdownActorSystem(system)
    }

    private def withTestDriver(block: PersistentEntityTestDriver[BankAccountCommand[_],
        BankAccountEvent,
        Account] => Unit): Unit = {
            val driver = new PersistentEntityTestDriver(system, new BankAccountEntity, "bankaccount-1")
            block(driver)
            driver.getAllIssues should have size 0
        }

    "BankAccountEntity" should {
        "handle deposit" in withTestDriver { driver =>
            val outcome = driver.run(DepositCmd(Instant.now, "Opening deposit", 100))
            outcome.replies should contain only 100
        }
        "handle deposit and successful withdrawal" in withTestDriver { driver =>
            val t1 = Instant.now
            val t2 = t1.plusSeconds(100)

            val outcome = driver.run(DepositCmd(t1, "Opening deposit", 100))
            outcome.replies should contain only 100

            val outcome2 = driver.run(WithdrawCmd(t2, "Transfer to savings", 20))
            outcome2.replies should contain only SuccessfulWithdrawalResponse(80)
            outcome2.issues should be(Nil)
            outcome2.state should ===(Account(80, None))
            outcome2.events should ===(Vector(WithdrawEvt(t2, "Transfer to savings", 20)))
        }
        "handle deposit and failed withdrawal" in withTestDriver { driver =>
            val outcome = driver.run(DepositCmd(Instant.now, "Opening deposit", 100))
            outcome.replies should contain only 100

            val outcome2 = driver.run(WithdrawCmd(Instant.now, "Transfer to savings", 200))
            outcome2.replies should contain only FailedWithdrawalResponse("Insufficient funds (current balance 100)")
        }
        "handle account holder assignment and reassignment" in withTestDriver { driver =>
            val outcome = driver.run(DepositCmd(Instant.now, "Opening deposit", 100))
            outcome.replies should contain only 100

            val outcome2 = driver.run(AssignAccountHolderCmd(Instant.now, "John Money"))
            outcome2.replies should contain only Done
            outcome2.state should ===(Account(100, Some("John Money")))

            val outcome3 = driver.run(AssignAccountHolderCmd(Instant.now, "John Richard Money"))
            outcome3.replies should contain only Done
            outcome3.state should ===(Account(100, Some("John Richard Money")))
        }
    }

}
