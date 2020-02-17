import java.util.concurrent._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.KillSwitches
import akka.stream.scaladsl._

import scala.concurrent.duration._

import com.typesafe.config.Config

import spray.json._
import DefaultJsonProtocol._

class IEXClient(iexConfig: Config, symbols: List[String], queueSize: Int = 100000) extends DataSourceTraits {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val queue = new LinkedBlockingQueue[String](queueSize)
  val iexKillSwitch = KillSwitches.shared("iex-kill-switch")

  def take = queue.take()
  def start: Unit = {
    val symbolsString = symbols.mkString(",")
    Source
      .repeat(HttpRequest(uri = s"https://financialmodelingprep.com/api/v3/quote/$symbolsString"))
      .throttle(1, 60.seconds)
      .via(iexKillSwitch.flow)
      .mapAsync(1)(Http().singleRequest(_)
        .flatMap { response => 
          response.entity.dataBytes
            .runReduce(_ ++ _)
            .map(line => {
              line.utf8String
                .parseJson
                .convertTo[List[JsValue]]
                .map(_.compactPrint)
            })
        })
      .runWith(Sink.foreach(_.foreach{queue.put(_)}))
  }
  def stop: Unit = {
    iexKillSwitch.shutdown()
  }
}