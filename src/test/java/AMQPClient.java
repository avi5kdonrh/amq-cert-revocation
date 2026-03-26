import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMQPClient {
    public static void main(String[] args) throws JMSException {

        Logger logger = LoggerFactory.getLogger(AMQPClient.class);

        String amqp = "amqps://localhost:5672?transport.trustStoreLocation=src/test/resources/ssl/amq.truststore.p12";
        amqp = amqp+"&transport.trustStorePassword=password&transport.verifyHost=false&transport.useOpenSSL=false";
        amqp = amqp+"&transport.keyStoreLocation=src/test/resources/ssl/client-keystore.p12";
        amqp = amqp+"&transport.keyStorePassword=password";

        JmsConnectionFactory connectionFactory = new JmsConnectionFactory(amqp);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(session.createQueue("TEST"));
            messageProducer.send(session.createTextMessage("Hi"));
            logger.info("\n\nSent message to TEST\n");
        } catch (Exception e) {
            //assertEquals("Error :: ", "javax.net.ssl.SSLHandshakeException: Received fatal alert: certificate_unknown",  e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
