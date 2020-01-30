import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.typesafe.config.ConfigFactory

object Main {
  def main(args1: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf").getConfig("config") 
    val twitterConfig = config.getConfig("twitter")
    val iexConfig = config.getConfig("iex")
    val symbols = List("TSLA", "BYND")
    
    val twitterClient = new TwitterClient(twitterConfig, symbols)

    val twitterProducer = new KafkaEventProducer("tweets", twitterClient)
    val twitterFuture : Unit = Future {
      twitterProducer.start()
    }

    val iexClient = new IEXClient(iexConfig, symbols)
    val iexProducer = new KafkaEventProducer("iex", iexClient)
    val iexFuture : Unit = Future {
      iexProducer.start()
    }

    scala.io.StdIn.readLine()

    twitterProducer.stop()
    iexProducer.stop()

    System.exit(1)
  }
}
