package org.justinhj.bankingonpersistententities.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import org.justinhj.bankingonpersistententities.api._

class BankingonpersistententitiesServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new BankingonpersistententitiesApplication(ctx) with LocalServiceLocator
  }

  val client: BankingonpersistententitiesService = server.serviceClient.implement[BankingonpersistententitiesService]

  override protected def afterAll(): Unit = server.stop()

  "banking-on-persistent-entities service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}
