import org.apache.flink.streaming.api.scala.{
  DataStream,
  StreamExecutionEnvironment
}
import org.apache.flink.streaming.api.scala._

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory

import spray.json._
import DefaultJsonProtocol._

object Main {
  def main(args1: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf").getConfig("config")
    val stockSymbols: List[String] =
      config.getStringList("symbols").asScala.toList

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.disableClosureCleaner()

    // Kafka consumer and producer for aggregating tweets sentiment
    val kafkaTweetsSentimentConsumer = new KafkaFlinkConsumer(
      "tweets-sentiment"
    )
    val kafkaSentimentMA5T1Producer = new KafkaFlinkProducer(
      "symbol-sentiment-ma-5-1"
    )
    val tweetSentimentEvents = env
      .addSource(kafkaTweetsSentimentConsumer.consumer)

    ProcessSentiment
      .process(tweetSentimentEvents, stockSymbols, 15, 1)
      .addSink(kafkaSentimentMA5T1Producer.producer)

    env.execute()
  }
}
