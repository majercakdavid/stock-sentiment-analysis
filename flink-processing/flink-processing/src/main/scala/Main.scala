import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.scala._

import spray.json._
import DefaultJsonProtocol._
import scala.math.min

case class TweetText(val id: Long, val text: String)
case class TweetSentiment(val id: Long, val sentiment: Double)
case class StockQuote(val symbol: String, val price: Double, val timestamp: Long, val count: Long)

object FlinkProcessingJsonProtocol extends DefaultJsonProtocol {
  implicit val tweetTextFormat = jsonFormat2(TweetText)
  implicit val tweetSentimentFormat = jsonFormat2(TweetSentiment)
}
import FlinkProcessingJsonProtocol._

object Main {
  def main(args1: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.disableClosureCleaner()

    val kafkaTweetsConsumer = new KafkaFlinkConsumer("tweets")
    val kafkaTweetsTextProducer = new KafkaFlinkProducer("tweets-text")

    // val tweetEvents = env
    //   .addSource(kafkaTweetsConsumer.consumer)
    //   .map{msg => {
    //     msg.parseJson
    //       .asJsObject.getFields("id", "text") match {
    //         case Seq(JsNumber(id), JsString(text)) => {
    //           val tweetText = TweetText(id.toLong, text)
    //           tweetText.toJson.compactPrint
    //         }
    //         case _ => throw new DeserializationException("Tweet text expected")
    //       }
    //   }}
    //   .addSink(kafkaTweetsTextProducer.producer)

    // val kafkaTweetsSentimentConsumer = new KafkaFlinkConsumer("tweets-sentiment")
    // val tweetSentimentEvents = env
    //   .addSource(kafkaTweetsSentimentConsumer.consumer)
    //   .map{msg => {
    //     msg.parseJson
    //       .asJsObject.getFields("id", "sentiment") match {
    //         case Seq(JsNumber(id), JsNumber(sentiment)) => TweetSentiment(id.toLong, sentiment.toDouble)
    //         case _ => throw new DeserializationException("Tweet text expected")
    //       }
    //   }}
    //   .addSink(print(_))

    val kafkaStockQuotesConsumer = new KafkaFlinkConsumer("stock-quotes")
    val stockQuotesEvents = env
      .addSource(kafkaStockQuotesConsumer.consumer)
      .map{msg => {
        msg.parseJson
          .asJsObject.getFields("symbol", "price", "timestamp") match {
            case Seq(JsString(symbol), JsNumber(price), JsNumber(timestamp)) => StockQuote(symbol, price.toDouble, timestamp.toLong, 1)
            case _ => throw new DeserializationException("Stock quote deserializatiom error")
          }
      }}
      .addSink(print(_))

    val stockQuotesAggregatedEvents = env
      .addSource(kafkaStockQuotesConsumer.consumer)
      .map{msg => {
        msg.parseJson
          .asJsObject.getFields("symbol", "price", "timestamp") match {
            case Seq(JsString(symbol), JsNumber(price), JsNumber(timestamp)) => StockQuote(symbol, price.toDouble, timestamp.toLong, 1)
            case _ => throw new DeserializationException("Stock quote deserializatiom error")
          }
      }}
      .keyBy(_.symbol)
      .assignTimestampsAndWatermarks(
        new AssignerWithPunctuatedWatermarks[StockQuote] {
          override def extractTimestamp(stockQuote: StockQuote, previousStockQuoteTimestamp: Long): Long = stockQuote.timestamp
          override def checkAndGetNextWatermark(lastStockQuote: StockQuote, extractedTimestamp: Long): Watermark = {
            new Watermark(extractedTimestamp - 2)
          }
        })
      .timeWindowAll(Time.seconds(60))
      .reduce((sq1, sq2) => StockQuote(
        sq1.symbol,
        sq1.price + sq2.price,
        min(sq1.timestamp, sq2.timestamp),
        sq1.count + sq2.count
      ))
      .addSink(print(_))

    env.execute()
  }
}