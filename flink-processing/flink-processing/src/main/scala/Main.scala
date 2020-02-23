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

    // Kafka consumer and producers for aggregating stock quotes sentiment
    val kafkaStockQuotesConsumer = new KafkaFlinkConsumer("stock-quotes")
    val kafkaStockQuotesFastMAProducer = new KafkaFlinkProducer(
      "stock-quotes-ma-5-1"
    )
    val kafkaStockQuotesSlowMAProducer = new KafkaFlinkProducer(
      "stock-quotes-ma-15-1"
    )

    val stockQuotesAggregatedEvents = env
      .addSource(kafkaStockQuotesConsumer.consumer)

    ProcessStockQuotes
      .process(stockQuotesAggregatedEvents, 5, 1)
      .addSink(kafkaStockQuotesFastMAProducer.producer)

    ProcessStockQuotes
      .process(stockQuotesAggregatedEvents, 15, 1)
      .addSink(kafkaStockQuotesSlowMAProducer.producer)

    // Kafka consumer and producer for aggregating tweets sentiment
    val kafkaTweetsSentimentConsumer = new KafkaFlinkConsumer(
      "tweets-sentiment"
    )
    val kafkaSentimentMA5T1Producer = new KafkaFlinkProducer(
      "symbol-sentiment-ma-15-1"
    )
    val tweetSentimentEvents = env
      .addSource(kafkaTweetsSentimentConsumer.consumer)

    ProcessSentiment
      .process(tweetSentimentEvents, stockSymbols, 15, 1)
      .addSink(kafkaSentimentMA5T1Producer.producer)

    env.execute()
  }
}
