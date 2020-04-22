//package ch.wsl.box.rest
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.marshalling.Marshaller
//import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
//import ch.wsl.box.rest.service.RouteRoot
//import org.specs2.mutable.Specification
//import org.specs2.time.NoTimeConversions
//
//import concurrent.duration._
//import akka.http.scaladsl.testkit._
//import ch.wsl.box.rest.routes.Root
//
//import scala.concurrent.duration.DurationInt
//
///**
//  * Created by andreaminetti on 10/03/16.
//  */
//trait BaseSpec extends Specification with Specs2RouteTest with Root {
//
//  sequential
//
//  def actorRefFactory = system
//
//  implicit def default(implicit system: ActorSystem) = ??? //RouteTestTimeout(new DurationInt(20).second.dilated(system))
//
//  val withAuth = addHeader(Authorization(BasicHttpCredentials("andreaminetti", "")))
//
//  val endpoint = "/api/v1"
//
//  def get[T](url:String)(body: => T) = Get(url) ~> withAuth ~> route ~> check(body)
//  def delete[T](url:String)(body: => T) = Delete(url) ~> withAuth ~> route ~> check(body)
//  def post[T: Marshaller,U](url: String, content: T)(body: => U) = Post(url,content) ~> withAuth ~> route ~> check(body)
//  def put[T: Marshaller,U](url: String, content: T)(body: => U) = Put(url,content) ~> withAuth ~> route ~> check(body)
//
//}
