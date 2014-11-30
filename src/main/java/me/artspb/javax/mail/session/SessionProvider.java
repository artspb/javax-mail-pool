package me.artspb.javax.mail.session;

import javax.mail.Authenticator;
import javax.mail.Session;
import java.util.Properties;

/**
 * @author Artem Khvastunov
 */
public interface SessionProvider {

    Session getSession(Properties properties, Authenticator authenticator);
}
