# clamav scala client

Akka Streams based ClamAV client

# Example usage 

Add client as local depedency

```
git clone https://github.com/arempter/clamav-scala-client.git
sbt publishLocal
``` 

Scan stream of bytes using either
```
val sampleSource = Source.single(ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))

sampleSource.via(scanFlow).toMat(resultSink)(Keep.right).run()
```

Or using client api - scala ByteString
```

 ClamAVClient().scanStream(
        ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*")) 
```
        
Or using client api - or Java inputStream (eg. AWS S3 client output)

```
 ClamAVClient()
        .scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
        
```


More advanced example - [AntivirRestScanner](https://github.com/arempter/AntivirRestScanner)
