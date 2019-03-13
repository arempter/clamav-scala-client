import java.io.ByteArrayInputStream

import akka.actor.ActorSystem
import akka.util.ByteString
import com.arempter.client.provider.ClamAVStreamClient
import org.scalatest.{AsyncWordSpec, DiagrammedAssertions}

class ClientSpec extends AsyncWordSpec with DiagrammedAssertions {

  implicit val system = ActorSystem("testScanner")


  "Client" should {
    "scan should find eicar" in {
      val scanner = new ClamAVStreamClient
      scanner.scanStream(
        new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
        .map(r => assert(r == "noOK"))
    }


    "parallel scan should find eicar" in {
      val scanner = new ClamAVStreamClient
      for {
        _ <- scanner.scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
          .map(r => assert(r == "noOK"))
        s2 <- scanner.scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
          .map(r => assert(r == "noOK"))
      } yield s2
    }

    "scan should be OK for clean string" in {
      val scanner = new ClamAVStreamClient
      scanner.scanStream(
        new ByteArrayInputStream("Just a String".getBytes()))
        .map(r => assert(r == "OK"))
    }

    "scan should find eicar in ByteString" in {
      val scanner = new ClamAVStreamClient
      scanner.scanStream(
        ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))
        .map(r => assert(r == "noOK"))
    }

  }
}
