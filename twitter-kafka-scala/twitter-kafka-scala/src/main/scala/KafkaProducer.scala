import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties

class KafkaEventProducer(topic: String, dataSource: DataSourceTraits) {
  var shouldContinue = false

  def getProducerProps() = {
    val properties = new Properties()
    properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("client.id", "KafkaTwitterProducer")
    properties.setProperty("bootstrap.servers", "broker:29092")
    properties.setProperty("acks","all")
    properties.setProperty("retries","3")
    properties
  }

  def sendMessage(kafkaProducer: KafkaProducer[String, String],message: String) = {
    val record = new ProducerRecord[String, String](topic, message)
    kafkaProducer.send(record)
  }

  def start() = {
    dataSource.start

    shouldContinue = true
    val kafkaProducer: KafkaProducer[String, String] = new KafkaProducer[String, String](getProducerProps())

    try {
      while(shouldContinue) {
        val message = dataSource.take
        println(s"Message for topic '$topic' received: $message")
        
        sendMessage(kafkaProducer, message)
        kafkaProducer.flush()
      }
    }
    catch { case e: Throwable => println(e) }
    finally{ kafkaProducer.close() }
  }

  def stop() = {
    shouldContinue = false

    dataSource.stop
  }
}