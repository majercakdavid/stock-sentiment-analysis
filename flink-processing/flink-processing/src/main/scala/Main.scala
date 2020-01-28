import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

object Main {
  def main(args1: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.disableClosureCleaner()

    val kafkaConsumer = new KafkaFlinkConsumer("tweets")
    val kafkaProducer = new KafkaFlinkProducer("tweets-analysed")

    val tweetEvents = env
      .addSource(kafkaConsumer.consumer)
      .map{v=> {
        println(v)
        v
      }} // TODO sentiment analysis
      .addSink(kafkaProducer.producer)
    env.execute()
  }
}
