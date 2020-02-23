import scala.collection.mutable.ListBuffer

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.util.Collector

import spray.json._
import DefaultJsonProtocol._

case class TweetText(val id: Long, val text: String)
case class TweetSentiment(
    val symbol: String,
    val sentiment: Double,
    val timestamp: Long
)
case class SymbolWindowSentiment(symbol: String, averageSentiment: Double)

object ProcessSentimentJsonProtocol extends DefaultJsonProtocol {
  implicit val tweetTextFormat = jsonFormat2(TweetText)
  implicit val tweetSentimentFormat = jsonFormat3(TweetSentiment)
  implicit val symbolWindowSentimentFormat = jsonFormat2(SymbolWindowSentiment)
}
import ProcessSentimentJsonProtocol._

class AverageAggregate
    extends AggregateFunction[
      TweetSentiment,
      (String, Double, Long),
      SymbolWindowSentiment
    ] {
  override def createAccumulator() = ("", 0.toDouble, 0L)

  override def add(
      value: TweetSentiment,
      accumulator: (String, Double, Long)
  ) = {
    (
      value.symbol,
      accumulator._2 + value.sentiment,
      accumulator._3 + 1L
    )
  }

  override def getResult(accumulator: (String, Double, Long)) =
    SymbolWindowSentiment(accumulator._1, accumulator._2 / accumulator._3)

  override def merge(a: (String, Double, Long), b: (String, Double, Long)) =
    (a._1, a._2 + b._2, a._3 + b._3)
}

object ProcessSentiment {
  def process(
      tweetSentimentEvents: DataStream[String],
      stockSymbols: List[String],
      windowLengthMinutes: Int = 5,
      windowTriggerMinutes: Int = 1
  ): DataStream[String] = {
    tweetSentimentEvents
    // One tweet can contain multiple symbols, flatmap results in separate
    // object for each instance
      .flatMap({ msg: String =>
        {
          stockSymbols
            .foldLeft(List[TweetSentiment]())((acc, symbol) => {
              msg.contains(symbol) match {
                case true => {
                  msg.parseJson.asJsObject
                  // We are only interested in timestamp because of windowing
                    .getFields("sentiment", "timestamp_ms") match {
                    case Seq(JsNumber(sentiment), JsString(timestamp_ms)) => {
                      println(
                        s"Received $symbol, sentiment: $sentiment, timestamp: $timestamp_ms"
                      )
                      TweetSentiment(
                        symbol.toString(),
                        sentiment.toDouble,
                        timestamp_ms.toLong
                      ) :: acc
                    }
                    case _ =>
                      throw new DeserializationException("Tweet text expected")
                  }
                }
                case _ => acc
              }
            })
        }
      })
      .assignTimestampsAndWatermarks(
        new AssignerWithPunctuatedWatermarks[TweetSentiment] {
          override def extractTimestamp(
              tweetSentiment: TweetSentiment,
              previousTweetSentimentTimestamp: Long
          ): Long = tweetSentiment.timestamp

          override def checkAndGetNextWatermark(
              tweetSentiment: TweetSentiment,
              extractedTimestamp: Long
          ): Watermark = {
            // Close events by watermark which is 30 seconds
            // less then current event time
            new Watermark(extractedTimestamp - 30000)
          }
        }
      )
      .keyBy("symbol")
      .timeWindow(
        Time.minutes(windowLengthMinutes),
        Time.minutes(windowTriggerMinutes)
      )
      .aggregate(new AverageAggregate())
      .map(v => {
        val averageJson = v.toJson.compactPrint
        println(s"Window average $averageJson")
        averageJson
      })
  }
}
