import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.typesafe.config.ConfigFactory

object Main {
  def main(args1: Array[String]): Unit = {
    println("Starting App...")

    val config = ConfigFactory.load("application.conf").getConfig("config")
    val twitterConfig = config.getConfig("twitter")

    val twitterKeyWords = List("TSLA", "BYND")
    val twitterClient = new TwitterClient(twitterKeyWords, twitterConfig)

    val twitterProducer = new KafkaEventProducer("tweets", twitterClient)

    twitterProducer.start()
  }
}
