import jakarta.jms.Connection;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import org.apache.qpid.jms.JmsConnectionFactory;

public class AMQPClient {
    public static void main(String[] args) {
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
            //assertEquals("Error :: ", "javax.net.ssl.SSLHandshakeException: Received fatal alert: certificate_unknown",  e.getMessage());
            e.printStackTrace();
        }
    }
}
