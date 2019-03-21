# clamav scala client

Akka Streams based Clam AV client

# Example usage 

Add client as local depedency

```
sbt publishLocal
``` 

Scan stream of bytes using either
```
val sampleSource = Source.single(ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))

sampleSource.via(scanFlow).toMat(resultSink)(Keep.right).run().map(r => assert(r.contains("FOUND")))
```

Or using client api
```
scala ByteString

 ClamAVClient().scanStream(
        ByteString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*")) 
        
or Java inputStream (eg. AWS S3 client output)

 ClamAVClient()
        .scanStream(
          new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()))
        
```


More advanced example - [AntivirRestScanner](https://github.com/arempter/AntivirRestScanner)
