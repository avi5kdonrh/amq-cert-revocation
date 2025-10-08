# amq-cert-revocation

The src/test/resources/ssl directory contains the broker keystore (amq.keystore.p12) along with its keypair (amq-cert-signed, amq.key).
The same directory, also contains the client keypair (client.crt, client.key) and client keystore (client-keystore.p12).
Both client and broker certs are signed by the same CA (ca-cert, ca-key), so amq.truststore.p12 (which contains the CA cert) can be used
as a truststore for both the client and the server.


Run the WithCrlPathTest to see that by setting the truststore at JVM level gives the following warning:

```declarative
System.setProperty("javax.net.ssl.trustStore","src/test/resources/ssl/amq.truststore.p12");
System.setProperty("javax.net.ssl.trustStorePassword","password");
System.setProperty("com.sun.security.enableCRLDP","true");
System.setProperty("com.sun.net.ssl.checkRevocation","true");
```
### Warning in the broker logs
```declarative
2025-10-08 16:42:07,295 WARN  [org.apache.activemq.artemis.core.server] AMQ222208:
SSL handshake failed for client from /127.0.0.1:38382:
java.security.cert.CertPathValidatorException: Could not determine revocation status.
```
