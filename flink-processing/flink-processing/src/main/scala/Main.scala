import org.apache.flink.streaming.api.scala.{
  DataStream,
  StreamExecutionEnvironment
}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.scala._

import spray.json._
import DefaultJsonProtocol._

case class TweetText(val id: Long, val text: String)
case class TweetSentiment(val id: Long, val sentiment: Double)

object FlinkProcessingJsonProtocol extends DefaultJsonProtocol {
  implicit val tweetTextFormat = jsonFormat2(TweetText)
  implicit val tweetSentimentFormat = jsonFormat2(TweetSentiment)
}
import FlinkProcessingJsonProtocol._

object Main {
  def main(args1: Array[String]): Unit = {
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

    env.execute()
  }
}
