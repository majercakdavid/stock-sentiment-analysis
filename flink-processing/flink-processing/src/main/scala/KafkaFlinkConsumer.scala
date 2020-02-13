import java.util.Properties
import collection.JavaConverters._

import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

class KafkaFlinkConsumer(topic: String) {

  def getProps() = {
    val properties = new Properties()
    properties.setProperty("zookeeper.connect", "zookeeper:2181")
    properties.setProperty("bootstrap.servers", "broker:29092")
    properties
  }

  val consumer = new FlinkKafkaConsumer(
    topic,
    new SimpleStringSchema(),
    getProps()
  )
}
