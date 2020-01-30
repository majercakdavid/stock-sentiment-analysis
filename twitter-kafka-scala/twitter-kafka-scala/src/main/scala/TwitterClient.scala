import collection.JavaConverters._
import java.util.concurrent.LinkedBlockingQueue

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

import com.twitter.hbc.core.Constants
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.ClientBuilder

class TwitterClient(twitterConfig: Config, keyWords: List[String], queueSize: Int = 100000) extends DataSourceTraits {
  val queue = new LinkedBlockingQueue[String](queueSize)
  val endPoint = new StatusesFilterEndpoint()
  endPoint.trackTerms(keyWords.asJava)
  endPoint.stallWarnings(false)
  endPoint.addPostParameter("lang", "en")

  val conf = ConfigFactory.load()

  val auth = new OAuth1(
    twitterConfig.getString("consumerKey"),
    twitterConfig.getString("consumerSecret"),
    twitterConfig.getString("accessToken"),
    twitterConfig.getString("accessTokenSecret")
  )

  val client = new ClientBuilder()
    .hosts(Constants.STREAM_HOST)
    .endpoint(endPoint)
    .authentication(auth)
    .processor(new StringDelimitedProcessor(queue))
    .build()

  println("TwitterClient created...")

  def start = client.connect()
  def stop = client.stop()
  def take = queue.take()
}