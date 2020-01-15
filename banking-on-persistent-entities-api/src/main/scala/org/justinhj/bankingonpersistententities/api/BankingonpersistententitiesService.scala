package org.justinhj.bankingonpersistententities.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import com.lightbend.lagom.scaladsl.api.transport.Method

object BankingonpersistententitiesService  {
  val TOPIC_NAME = "bankevents"
}

/**
  * The banking-on-persistent-entities service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the BankingonpersistententitiesService.
  */
trait BankingonpersistententitiesService extends Service {

  def bankAccountDeposit(id: String, description: String, amount: Int) : ServiceCall[NotUsed, Int]

  /**
    * This gets published to Kafka.
    */
  def accountTopic(): Topic[Account]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("banking-on-persistent-entities")
      .withCalls(
        restCall(Method.PUT, "/api/bankaccount/deposit/:id/:description/:amount", bankAccountDeposit _)
      )
      .withTopics(
        topic(BankingonpersistententitiesService.TOPIC_NAME, accountTopic _)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[Account](_.id)
          )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

case class Account(id: String, balance: Int, accountHolder: Option[String])

object Account {
  implicit val format: Format[Account] = Json.format[Account]
}