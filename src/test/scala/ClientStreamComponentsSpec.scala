import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import com.arempter.client.provider.StreamComponents
import org.scalatest.{AsyncWordSpec, DiagrammedAssertions}

class ClientStreamComponentsSpec extends AsyncWordSpec with DiagrammedAssertions with StreamComponents {

  implicit val system = ActorSystem("testStreamComponents")
  implicit val materializer: Materializer = ActorMaterializer()

  "ClientStreamComponents" should {
    "scan using flowComponent should find eicar" in {
      implicit val sIO = getSocketInOut()
      val sampleSource = Source.single(ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))

      sampleSource.via(scanFlow).toMat(resultSink)(Keep.right).run().map(r=>assert(r.contains("FOUND")))
    }

    "scan using flowComponent should not find eicar" in {
      implicit val sIO = getSocketInOut()
      val sampleSource = Source.single(ByteString("Some test string"))

      sampleSource.via(scanFlow).toMat(resultSink)(Keep.right).run().map(r=>assert(!r.contains("FOUND")))
    }

  }
}
