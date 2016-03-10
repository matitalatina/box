package ch.wsl.rest

import akka.actor.ActorSystem
import ch.wsl.rest.service.MainService
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import spray.http.BasicHttpCredentials
import spray.http.HttpHeaders.Authorization
import spray.testkit._
import concurrent.duration._
import akka.testkit._

import scala.concurrent.duration.DurationInt

/**
  * Created by andreaminetti on 10/03/16.
  */
trait BaseSpec extends Specification with Specs2RouteTest with MainService with NoTimeConversions{

  sequential

  def actorRefFactory = system

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(new DurationInt(20).second.dilated(system))

  val withAuth = addHeader(Authorization(BasicHttpCredentials("andreaminetti", "")))

}
