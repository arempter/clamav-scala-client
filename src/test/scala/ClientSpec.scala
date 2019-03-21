import java.io.ByteArrayInputStream

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.arempter.client.data.{ ObjectClean, ObjectInfected }
import com.arempter.client.provider.{ ClamAVClient, StreamComponents }
import org.scalatest.{ AsyncWordSpec, DiagrammedAssertions }

import scala.concurrent.Future

class ClientSpec extends AsyncWordSpec with DiagrammedAssertions with StreamComponents {

  "Client" should {
    "scan should find eicar" in {
      ClamAVClient()
        .scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
        .map(r => assert(r == ObjectInfected))
    }

    "parallel scan should find eicar" in {
      for {
        _ <- ClamAVClient().scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
          .map(r => assert(r == ObjectInfected))
        s2 <- ClamAVClient().scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
          .map(r => assert(r == ObjectInfected))
      } yield s2
    }

    "scan should be OK for clean string" in {
      ClamAVClient().scanStream(
        new ByteArrayInputStream("Just a String".getBytes()))
        .map(r => assert(r == ObjectClean))
    }

    "scan should find eicar in ByteString" in {
      ClamAVClient().scanStream(
        ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))
        .map(r => assert(r == ObjectInfected))
    }

    "scan should find eicar in Source" in {
      ClamAVClient().scanStream(
        Source.single(ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*")))
        .map(r => assert(r == ObjectInfected))
    }

    "scan should find eicar in Future Source" in {
      ClamAVClient().scanStream(
        Source.fromFuture(Future(ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))))
        .map(r => assert(r == ObjectInfected))
    }
  }
}
