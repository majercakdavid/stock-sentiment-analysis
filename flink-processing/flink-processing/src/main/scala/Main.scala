import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

import spray.json._
import DefaultJsonProtocol._

case class TweetText(val id: Int, val text: String)
case class TweetSentiment(val id: Int, val sentiment: Double)

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

    val tweetEvents = env
      .addSource(kafkaTweetsConsumer.consumer)
      .map{msg => {
        msg.parseJson
          .asJsObject.getFields("id", "text") match {
            case Seq(JsNumber(id), JsString(text)) => {
              val tweetText = TweetText(id.toInt, text)
              tweetText.toJson.compactPrint
            }
            case _ => throw new DeserializationException("Tweet text expected")
          }
      }}
      .addSink(kafkaTweetsTextProducer.producer)

    val kafkaTweetsSentimentConsumer = new KafkaFlinkConsumer("tweets-sentiment")
    val tweetSentimentEvents = env
      .addSource(kafkaTweetsSentimentConsumer.consumer)
      .map{msg => {
        msg.parseJson
          .asJsObject.getFields("id", "sentiment") match {
            case Seq(JsNumber(id), JsNumber(sentiment)) => TweetSentiment(id.toInt, sentiment.toDouble)
            case _ => throw new DeserializationException("Tweet text expected")
          }
      }}
      .addSink(print(_))

    env.execute()
  }
}