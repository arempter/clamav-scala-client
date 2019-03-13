import java.io.ByteArrayInputStream

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.arempter.client.provider.ClamAVStreamClient
import org.scalatest.{AsyncWordSpec, DiagrammedAssertions}

import scala.concurrent.Future

class ClientSpec extends AsyncWordSpec with DiagrammedAssertions {

  "Client" should {
    "scan should find eicar" in {
     ClamAVStreamClient()
      .scanStream(
        new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
        .map(r => assert(r == "noOK"))
    }

    "parallel scan should find eicar" in {
      for {
        _ <- ClamAVStreamClient().scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
          .map(r => assert(r == "noOK"))
        s2 <- ClamAVStreamClient().scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
          .map(r => assert(r == "noOK"))
      } yield s2
    }

    "scan should be OK for clean string" in {
      ClamAVStreamClient().scanStream(
        new ByteArrayInputStream("Just a String".getBytes()))
        .map(r => assert(r == "OK"))
    }

    "scan should find eicar in ByteString" in {
      ClamAVStreamClient().scanStream(
        ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))
        .map(r => assert(r == "noOK"))
    }

    "scan should find eicar in Source" in {
      ClamAVStreamClient().scanStream(
        Source.single(ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*")))
        .map(r => assert(r == "noOK"))
    }

    "scan should find eicar in Future Source" in {
      ClamAVStreamClient().scanStream(
        Source.fromFuture(Future(ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))))
        .map(r => assert(r == "noOK"))
    }

  }
}
