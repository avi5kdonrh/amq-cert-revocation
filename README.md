# amq-cert-revocation

The src/test/resources/ssl directory contains the broker keystore (amq.keystore.p12) along with its keypair (amq-cert-signed, amq.key).
The same directory, also contains the client keypair (client.crt, client.key) and client keystore (client-keystore.p12).
Both client and broker certs are signed by the same CA (ca-cert, ca-key), so amq.truststore.p12 (which contains the CA cert) can be used
as a truststore for both the client and the server.

# Expected (baseline test)

Run the ```mvn clean test -Dtest=WithTrustStoreAtAcceptorTest``` to see that after setting the truststore at the acceptor level,
and the crl path, the broker rejects the revoked client certificate.

### Warning in the broker logs
```declarative
2026-03-26 19:04:58,698 WARN  [org.apache.activemq.artemis.core.server] AMQ222208: SSL handshake failed for client from /127.0.0.1:56156:
java.security.cert.CertificateRevokedException: Certificate has been revoked, reason: KEY_COMPROMISE,
revocation date: Fri Sep 26 22:02:56 IST 2025, authority: CN=myhost, OU=Kafka, O=Strimzi, L=Local, ST=Local, C=US, extension OIDs: [2.5.29.21].

```

# Unexpected test

But if the broker trustStore is configured at the JVM level, the revoked client certificate is not rejected even if the 
correct crlPath is configured at the acceptor level. Ideally, the client cert should be rejected and the connection should fail.

```mvn clean test -Dtest=WithTrustStoreAtJVMProps```