import collection.JavaConverters._
import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.ConfigFactory
import com.twitter.hbc.core.Constants
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.ClientBuilder

object Main {
  def main(args1: Array[String]): Unit = {
    println("Starting App...")
    val args: Array[String] = Array(

    )
    val authMap = Map("consumerKey" -> args(0), "consumerSecret" -> args(1), "accessToken" -> args(2), "accessTokenSecret" -> args(3))
    val keyWords: List[String] = List("trump", "twitterapi", "#yolo", "great", "like", "happy", "love")
    val twitterClient = new TwitterHttpClient(keyWords, authMap)

    var counter = 100

    // println("Starting TwitterClient connecting...")
    // twitterClient.connect
    // Future {
    //   println("in future...")
    //   while(counter > 0) {
    //     println("in while...")
    //     val message = twitterClient.take
    //     println(s"Message: message")
    //     // counter -= 1
    //   }
    // }

    // println("Stopping TwitterClient...")
    // twitterClient.stop

    twitterClient.take() match {
      case None => println("TwitterClient stopped without message")
      case Some(value) => println(s"Twitter value: $value")
    }
  }
}

class TwitterHttpClient(keyWords: List[String], authMap: Map[String, String]) {
  println("Starting TwitterClient...")

  val queue = new LinkedBlockingQueue[String](100000)
  val endPoint = new StatusesFilterEndpoint()
  endPoint.trackTerms(keyWords.asJava)
  endPoint.stallWarnings(false)
  endPoint.addPostParameter("lang", "en")

  val conf = ConfigFactory.load()

  val auth = new OAuth1(
    authMap("consumerKey"),
    authMap("consumerSecret"),
    authMap("accessToken"),
    authMap("accessTokenSecret")
  )

  val client = new ClientBuilder()
    .hosts(Constants.STREAM_HOST)
    .endpoint(endPoint)
    .authentication(auth)
    .processor(new StringDelimitedProcessor(queue))
    .build()

  println("Starting TwitterClient created...")

  client.connect()

  def take(): Option[String] = {
    if (client.isDone)
      None
    else
      Some(queue.take())
  }

  // def connect = client.connect()
  // def stop = client.stop()
  // def take = queue.take()
}
