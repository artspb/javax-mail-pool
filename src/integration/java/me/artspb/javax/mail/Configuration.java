package me.artspb.javax.mail;

import lombok.SneakyThrows;
import me.artspb.javax.mail.session.PlainTextAuthenticator;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Artem Khvastunov
 */
public class Configuration {

    private static final Session DEFAULT_SESSION = Session.getInstance(new Properties());
    private static final InternetAddress TEST_ADDRESS = getInternetAddress("test@gmail.com");

    private Configuration() {
    }

    public static Properties getProperties(int port) {
        Properties properties = getProperties();
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        return properties;
    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", "localhost");
        return properties;
    }

    public static Authenticator getAuthenticator() {
        return getAuthenticator("user", "pass");
    }

    public static Authenticator getAuthenticator(String username, String password) {
        return new PlainTextAuthenticator(username, password);
    }

    @SneakyThrows(MessagingException.class)
    public static MimeMessage getMessage() {
        MimeMessage message = new MimeMessage(DEFAULT_SESSION);
        message.setFrom(TEST_ADDRESS);
        message.setText("text");
        return message;
    }

    public static Address[] getRecipients() {
        return new Address[]{TEST_ADDRESS};
    }

    @SneakyThrows(AddressException.class)
    private static InternetAddress getInternetAddress(String address) {
        return new InternetAddress(address);
    }
}
