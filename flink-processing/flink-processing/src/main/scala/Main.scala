import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

object Main {
  def main(args1: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.disableClosureCleaner()

    val kafkaConsumer = new KafkaFlinkConsumer("tweets")

    val tweetEvents = env.addSource(kafkaConsumer.consumer)
    tweetEvents.map{v=> println(v)}

    env.execute()
  }
}
