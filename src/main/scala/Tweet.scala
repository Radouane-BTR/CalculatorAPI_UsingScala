import cats.effect.{ContextShift, IO, Timer}
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import cats.implicits._
import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.server.Router

import cats.effect._, org.http4s._, org.http4s.dsl.io._, scala.concurrent.ExecutionContext.Implicits.global



object TweetApp extends App{
  case class Tweet(id: Int, message: String)
  // defined class Tweet

  implicit def tweetEncoder: EntityEncoder[IO, Tweet] = ???
  // tweetEncoder: org.http4s.EntityEncoder[cats.effect.IO,Tweet]

  implicit def tweetsEncoder: EntityEncoder[IO, Seq[Tweet]] = ???
  // tweetsEncoder: org.http4s.EntityEncoder[cats.effect.IO,Seq[Tweet]]

  def getTweet(tweetId: Int): IO[Tweet] = ???
  // getTweet: (tweetId: Int)cats.effect.IO[Tweet]

  def getPopularTweets(): IO[Seq[Tweet]] = ???
  // getPopularTweets: ()cats.effect.IO[Seq[Tweet]]

  val tweetService = HttpRoutes.of[IO] {
    case GET -> Root / "tweets" / "popular" =>
      getPopularTweets().flatMap(Ok(_))
    case GET -> Root / "tweets" / IntVar(tweetId) =>
      getTweet(tweetId).flatMap(Ok(_))
  }

  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }

  val services = tweetService <+> helloWorldService

  val httpApp = Router("/" -> helloWorldService, "/api" -> services).orNotFound

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  val serverBuilder = BlazeServerBuilder[IO].bindHttp(8080, "localhost").withHttpApp(httpApp)

  val fiber = serverBuilder.resource.use(_ => IO.never).start.unsafeRunSync()

}