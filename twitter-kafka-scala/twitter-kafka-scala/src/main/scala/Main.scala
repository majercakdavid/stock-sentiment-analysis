import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory

object Main {
  def main(args1: Array[String]): Unit = {
    println("Starting App...")

    val config = ConfigFactory.load("application.conf").getConfig("config")
    val twitterConfig = config.getConfig("twitter")
    val iexConfig = config.getConfig("iex")
    val stockSymbols: List[String] =
      config.getStringList("symbols").asScala.toList

    val twitterClient = new TwitterClient(twitterConfig, stockSymbols)
    val twitterProducer = new KafkaEventProducer("tweets", twitterClient)
    val twitterFuture: Unit = Future {
      twitterProducer.start()
    }

    val iexClient = new IEXClient(iexConfig, stockSymbols)
    val iexProducer = new KafkaEventProducer("stock-quotes", iexClient)
    val iexFuture: Unit = Future {
      iexProducer.start()
    }
  }
}
