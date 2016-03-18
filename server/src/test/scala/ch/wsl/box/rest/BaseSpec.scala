package ch.wsl.box.rest

import akka.actor.ActorSystem
import ch.wsl.rest.service.{RouteRoot, MainService}
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import spray.http.BasicHttpCredentials
import spray.http.HttpHeaders.Authorization
import spray.httpx.marshalling.Marshaller
import spray.testkit._
import concurrent.duration._
import akka.testkit._

import scala.concurrent.duration.DurationInt

/**
  * Created by andreaminetti on 10/03/16.
  */
trait BaseSpec extends Specification with Specs2RouteTest with RouteRoot with NoTimeConversions{

  sequential

  def actorRefFactory = system

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(new DurationInt(20).second.dilated(system))

  val withAuth = addHeader(Authorization(BasicHttpCredentials("andreaminetti", "")))

  val endpoint = "/api/v1"

  def get[T](url:String)(body: => T) = Get(url) ~> withAuth ~> route ~> check(body)
  def delete[T](url:String)(body: => T) = Delete(url) ~> withAuth ~> route ~> check(body)
  def post[T: Marshaller,U](url: String, content: T)(body: => U) = Post(url,content) ~> withAuth ~> route ~> check(body)
  def put[T: Marshaller,U](url: String, content: T)(body: => U) = Put(url,content) ~> withAuth ~> route ~> check(body)

}
