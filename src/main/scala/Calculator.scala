import cats.implicits._
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.circe._
import io.circe.generic.auto._
import cats.effect._
import io.circe.syntax._
import org.http4s.dsl.io._

case class ComputationRequest(computation: String)
sealed trait ComputationResponse

case class ComputationResponseOk(result: String) extends ComputationResponse
case class ComputationResponseFailed(message: String) extends ComputationResponse


object CalculatorApp extends IOApp {

  final val ERROR_MESSAGE:String = "Given expression is invalid!";

  implicit val computationRequestDecoder: EntityDecoder[IO, ComputationRequest] = jsonOf[IO, ComputationRequest]

  def processOutPut(input: ComputationRequest): ComputationResponse = {

    import fastparse._, NoWhitespace._

    def number[_: P]: P[Int] = P( CharIn("0-9").rep(1).!.map(_.toInt) )
    def parens[_: P]: P[Int] = P( "(" ~/ addSub ~ ")" )
    def factor[_: P]: P[Int] = P( number | parens )
    def divMul[_: P]: P[Int] = P( factor ~ (CharIn("*/").! ~/ factor).rep ).map(eval)
    def addSub[_: P]: P[Int] = P( divMul ~ (CharIn("+\\-").! ~/ divMul).rep ).map(eval)
    def expr[_: P]: P[Int]   = P( addSub ~ End )

    def eval(tree: (Int, Seq[(String, Int)])) = {
      val (base, ops) = tree
      ops.foldLeft(base){ case (left, (op, right)) => op match{
        case "+" => left + right
        case "-" => left - right
        case "*" => left * right
        case "/" => left / right
      }}
    }
    val withoutSpace = input.computation.replaceAll("\\s+", "")
    val parseResult = parse(withoutSpace, expr(_))
    parseResult match {
      case Parsed.Success(value, index) =>  ComputationResponseOk(s"$value")
      case Parsed.Failure(str, i, extra) => ComputationResponseFailed(ERROR_MESSAGE)//ComputationResponseFailed("Given expression is invalid!") // "Given expression is invalid!" //
    }
  }

  val calculatorService = HttpRoutes.of[IO] {
    case req@POST -> Root / "calculator" =>
      for {
        output <- req.as[ComputationRequest].map(processOutPut)
        response <- output match{
        case o : ComputationResponseOk => Ok(o.asJson)
        case o : ComputationResponseFailed => BadRequest(o.asJson)
      }
      } yield response
  }

  val httpApp = Router("/" -> calculatorService).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}