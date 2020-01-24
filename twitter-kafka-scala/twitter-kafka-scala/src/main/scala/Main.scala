object Main extends App {
  println("Hello, World!")
}

import java.util.concurrent.LinkedBlockingQueue

import com.twitter.hbc.endpoint.StatusesFilterEndpoint

class HttpClient(keyWds: List[String], authMap: Map[String, String]) {
  val queue = new LinkedBlockingQueue[String](10000)
  val endPoint = new StatusesFilterEndpoint()
  endPoint.trackTerms(keyWords.asJava)
}