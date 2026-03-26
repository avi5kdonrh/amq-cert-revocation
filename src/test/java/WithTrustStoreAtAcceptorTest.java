import jakarta.jms.*;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.settings.impl.AddressFullMessagePolicy;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
/*
* With trust store configured at the acceptor level #L66, the certificate revocation works just fine.
* mvn clean test -Dtest=WithTrustStoreAtAcceptorTest
* 2026-03-26 18:08:05,211 WARN  [org.apache.activemq.artemis.core.server] AMQ222208: SSL handshake failed for client from /127.0.0.1:39532:
*  java.security.cert.CertificateRevokedException: Certificate has been revoked,
*  reason: KEY_COMPROMISE, revocation date: Fri Sep 26 22:02:56 IST 2025,
*  authority: CN=adongre, OU=Kafka, O=Strimzi, L=Local, ST=Local, C=US, extension OIDs: [2.5.29.21].
*
*  */
public class WithTrustStoreAtAcceptorTest extends ActiveMQTestBase {

    static {
        System.setProperty("com.sun.security.enableCRLDP","true");
        System.setProperty("com.sun.net.ssl.checkRevocation","true");
    }

   ActiveMQServer server1;
   ActiveMQServer server2;
   CountDownLatch countDownLatch = new CountDownLatch(1);
   private static final String address = "Queue";
   private static final String queue = "Queue";
   private static final String fqqn = address + "::" + queue;
   public void startServer() throws Exception {

   }

   @BeforeClass
   public static void beforeClass() throws Exception {
     // Assume.assumeTrue(CheckLeak.isLoaded());
   }

   @Override
   @Before
   public void setUp() throws Exception {
      startServer();
   }

   @Override
   public void tearDown() throws Exception {
      super.tearDown();
      server1 = null;
      server2 = null;
   }

   @Test
   public void testRedistributor() throws Exception {
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("host", "localhost");
      map.put("port", 5672);
      map.put("sslEnabled", "true");
      map.put("keyStorePath", "src/test/resources/ssl/amq.keystore.p12");
      map.put("trustStorePath", "src/test/resources/ssl/amq.truststore.p12");
      map.put("keyStorePassword", "password");
      map.put("trustStorePassword", "password");
      map.put("crlPath", "src/test/resources/ssl/crl.pem");
      map.put("needClientAuth", "true");
      map.put("protocols","AMQP");

      ConfigurationImpl config1 =  createBasicConfig(0);
       config1.setName("broker1");
      config1.getConnectorConfigurations().put("local", new TransportConfiguration(NETTY_CONNECTOR_FACTORY,map));
      config1.getAcceptorConfigurations().add(new TransportConfiguration(NETTY_ACCEPTOR_FACTORY,map));
      //config1.setWildCardConfiguration(wildcardConfiguration);

      AddressSettings addressSettings = new AddressSettings();
      addressSettings.setRedistributionDelay(0);
      addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.PAGE);
      addressSettings.setAutoCreateAddresses(true);
      addressSettings.setAutoDeleteAddresses(false);
      addressSettings.setAutoCreateQueues(true);
      addressSettings.setAutoDeleteQueues(false);

      server1 = createServer(false, config1);
      server1.getConfiguration().addAddressSetting("#", addressSettings);
      server1.getConfiguration().addAddressSetting("activemq.management#", addressSettings);
      server1.getConfiguration().setClusterUser("admin");
      server1.getConfiguration().setClusterPassword("admin");
      server1.getConfiguration().setPersistenceEnabled(true);
      server1.getConfiguration().setJournalDirectory("target/journal1");
      server1.getConfiguration().setBindingsDirectory("target/bindings1");
      server1.getConfiguration().setLargeMessagesDirectory("target/lm1");
      server1.getConfiguration().setPagingDirectory("target/pg1");
      server1.start();

      // Running the client in a separate process so it doesn't inherit server's JVM properties

      List<String> commands = new ArrayList<>();
      commands.add("java");
      commands.add("-cp");
      commands.add(System.getProperty("java.class.path"));
      commands.add("AMQPClient");
      ProcessBuilder processBuilder = new ProcessBuilder(commands);
      processBuilder.inheritIO();
      Process process = processBuilder.start();
      process.waitFor();

      }

}
