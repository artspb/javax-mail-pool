package me.artspb.javax.mail.transport;

import me.artspb.javax.mail.SmtpServer;
import me.artspb.javax.mail.session.CachedSessionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.mail.Session;
import javax.mail.Transport;
import java.util.Properties;

import static me.artspb.javax.mail.Configuration.*;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * @author Artem Khvastunov
 */
public class SendMailIT {

    private static final SmtpServer SMTP_SERVER = new SmtpServer();

    private CachedSessionFactory factory;
    private TransportPool pool;

    @BeforeClass
    public static void runServer() throws Exception {
        SMTP_SERVER.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        SMTP_SERVER.stop();
    }

    @Before
    public void setUp() throws Exception {
        factory = new CachedSessionFactory();
        pool = new TransportPool();
    }

    @Test
    public void testSendMessageWithAuthenticatorSessionsAndTransportsMustBeSame() throws Exception {
        Session session1 = factory.getSession(getProperties(SMTP_SERVER.getPort()), getAuthenticator());
        Transport transport1 = pool.lease(session1);
        transport1.sendMessage(getMessage(), getRecipients());
        pool.release(session1, transport1);

        Session session2 = factory.getSession(getProperties(SMTP_SERVER.getPort()), getAuthenticator());
        Transport transport2 = pool.lease(session2);
        transport2.sendMessage(getMessage(), getRecipients());

        assertSame(session1, session2);
        assertSame(transport1, transport2);
    }

    @Test
    public void testSendMessageWithoutAuthenticatorSessionsAndTransportsMustBeSame() throws Exception {
        Session session1 = factory.getSession(getProperties(SMTP_SERVER.getPort()));
        Transport transport1 = pool.lease(session1);
        transport1.sendMessage(getMessage(), getRecipients());
        pool.release(session1, transport1);

        Session session2 = factory.getSession(getProperties(SMTP_SERVER.getPort()));
        Transport transport2 = pool.lease(session2);
        transport2.sendMessage(getMessage(), getRecipients());

        assertSame(session1, session2);
        assertSame(transport1, transport2);
    }

    @Test
    public void testSendMessageWithDifferentAuthenticatorsSessionsAndTransportsMustBeDifferent() throws Exception {
        Session session1 = factory.getSession(getProperties(SMTP_SERVER.getPort()), getAuthenticator());
        Transport transport1 = pool.lease(session1);
        transport1.sendMessage(getMessage(), getRecipients());
        pool.release(session1, transport1);

        Session session2 = factory.getSession(getProperties(SMTP_SERVER.getPort()), getAuthenticator("user2", "pass2"));
        Transport transport2 = pool.lease(session2);
        transport2.sendMessage(getMessage(), getRecipients());

        assertNotSame(session1, session2);
        assertNotSame(transport1, transport2);
    }

    @Test
    public void testSendMessageWithDifferentPropertiesSessionsAndTransportsMustBeDifferent() throws Exception {
        Session session1 = factory.getSession(getProperties(SMTP_SERVER.getPort()), getAuthenticator());
        Transport transport1 = pool.lease(session1);
        transport1.sendMessage(getMessage(), getRecipients());
        pool.release(session1, transport1);

        Properties properties = getProperties(SMTP_SERVER.getPort());
        properties.setProperty("key", "value");
        Session session2 = factory.getSession(properties, getAuthenticator());
        Transport transport2 = pool.lease(session2);
        transport2.sendMessage(getMessage(), getRecipients());

        assertNotSame(session1, session2);
        assertNotSame(transport1, transport2);
    }

    @Test
    public void testSendMessageWithAuthenticatorAndAutoreleaseSessionsAndTransportsMustBeSame() throws Exception {
        Session session1 = factory.getSession(getProperties(SMTP_SERVER.getPort()), getAuthenticator());
        Transport transport1 = pool.lease(session1);
        transport1.sendMessage(getMessage(), getRecipients());

        Thread.sleep(1); // poll will be released in other thread

        Session session2 = factory.getSession(getProperties(SMTP_SERVER.getPort()), getAuthenticator());
        Transport transport2 = pool.lease(session2);
        transport2.sendMessage(getMessage(), getRecipients());

        assertSame(session1, session2);
        assertSame(transport1, transport2);
    }
}
