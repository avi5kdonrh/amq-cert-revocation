# amq-cert-revocation

The src/test/resources/ssl directory contains the broker keystore (amq.keystore.p12) along with its keypair (amq-cert-signed, amq.key).
The same directory, also contains the client keypair (client.crt, client.key) and client keystore (client-keystore.p12).
Both client and broker certs are signed by the same CA (ca-cert, ca-key), so amq.truststore.p12 (which contains the CA cert) can be used
as a truststore for both the client and the server.

Run the WithoutCrlPathTest to see that without setting the crlPath (in acceptor config), the revoked certificate work as usual.

```declarative
 mvn clean test -Dtest=WithoutCrlPathTest
```

Run the WithCrlPathTest to see that by setting the crlPath (in acceptor config), the client for which the certificate has been revoked, doesn't work.

```declarative
mvn clean test -Dtest=WithCrlPathTest
```
The WithCrlPathTest logs will give a warning like so:

```declarative
2025-09-26 23:02:38,055 WARN  [org.apache.activemq.artemis.core.server]
AMQ222208: SSL handshake failed for client from /127.0.0.1:38780:
java.security.cert.CertificateRevokedException: Certificate has been revoked,
reason: KEY_COMPROMISE, revocation date: Fri Sep 26 22:02:56 IST 2025,
authority: CN=myhost, OU=Kafka, O=Strimzi, L=Local, ST=Local, C=US, extension OIDs: [2.5.29.21].

```