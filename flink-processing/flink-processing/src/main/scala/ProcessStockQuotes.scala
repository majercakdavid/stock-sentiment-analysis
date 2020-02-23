import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.functions.AggregateFunction

import spray.json._
import DefaultJsonProtocol._

case class StockQuote(
    val symbol: String,
    val price: Double,
    val timestamp: Long
)
case class StockQuoteAvg(
    val symbol: String,
    val avgPrice: Double
)

object ProcessStockQuotesJsonProtocol extends DefaultJsonProtocol {
  implicit val stockQuoteFormat = jsonFormat2(StockQuoteAvg)
}
import ProcessStockQuotesJsonProtocol._

class AveragePriceAggregate
    extends AggregateFunction[
      StockQuote,
      (String, Double, Long),
      StockQuoteAvg
    ] {
  override def createAccumulator() = ("", 0.toDouble, 0L)

  override def add(
      stockQuote: StockQuote,
      accumulator: (String, Double, Long)
  ) =
    (stockQuote.symbol, accumulator._2 + stockQuote.price, accumulator._3 + 1L)

  override def getResult(accumulator: (String, Double, Long)) =
    StockQuoteAvg(accumulator._1, accumulator._2 / accumulator._3)

  override def merge(a: (String, Double, Long), b: (String, Double, Long)) =
    (a._1, a._2 + b._2, a._3 + b._3)
}

object ProcessStockQuotes {
  def process(
      stockQuotesEvents: DataStream[String],
      windowLengthMinutes: Int,
      windowTriggerMinutes: Int
  ) = {
    stockQuotesEvents
      .map { msg =>
        {
          msg.parseJson.asJsObject.getFields("symbol", "price", "timestamp") match {
            case Seq(JsString(symbol), JsNumber(price), JsNumber(timestamp)) => {
              println(
                s"Received $symbol, price: $price, timestamp: $timestamp"
              )
            }
            StockQuote(symbol, price.toDouble, timestamp.toLong)
          case _ =>
              throw new DeserializationException(
                "Stock quote deserializatiom error"
              )
          }
        }
      }
      .assignTimestampsAndWatermarks(
        new AssignerWithPunctuatedWatermarks[StockQuote] {
          override def extractTimestamp(
              stockQuote: StockQuote,
              previousStockQuoteTimestamp: Long
          ): Long = {
            val timestampMilliseconds = stockQuote.timestamp * 1000L
            timestampMilliseconds
          }
          override def checkAndGetNextWatermark(
              lastStockQuote: StockQuote,
              extractedTimestamp: Long
          ): Watermark = {
            // Close events by watermark which is 30 seconds
            // less then current event time
            new Watermark(extractedTimestamp - 30000L)
          }
        }
      )
      .keyBy("symbol")
      .timeWindow(
        Time.minutes(windowLengthMinutes),
        Time.minutes(windowTriggerMinutes)
      )
      .aggregate(new AveragePriceAggregate())
      .map(v => {
        val averageJson = v.toJson.compactPrint
        println(s"Window average $averageJson")
        averageJson
      })
  }
}
