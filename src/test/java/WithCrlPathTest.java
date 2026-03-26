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


import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class WithCrlPathTest extends ActiveMQTestBase {

   static {
       //System.setProperty("com.sun.security.enableCRLDP", "true");
      // System.setProperty("javax.net.debug","all");
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

       String amqp = "amqps://localhost:5672?transport.trustStoreLocation=src/test/resources/ssl/amq.truststore.p12";
       amqp = amqp+"&transport.trustStorePassword=password&transport.verifyHost=false&transport.useOpenSSL=false";
       amqp = amqp+"&transport.keyStoreLocation=src/test/resources/ssl/client-keystore.p12";
       amqp = amqp+"&transport.keyStorePassword=password";

       JmsConnectionFactory connectionFactory = new JmsConnectionFactory(amqp);
       connectionFactory.setUsername("admin");
       connectionFactory.setPassword("admin");
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(session.createQueue("TEST"));
            messageProducer.send(session.createTextMessage("Hi"));
            System.out.println(">> DONE >>");
        } catch (Exception e) {
            assertEquals("Error :: ", "javax.net.ssl.SSLHandshakeException: Received fatal alert: certificate_unknown",  e.getMessage());
            e.printStackTrace();
        }


      }

}
