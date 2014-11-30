package me.artspb.javax.mail.transport;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * @author Artem Khvastunov
 */
public interface TransportProvider {

    Transport getTransport(Session session) throws MessagingException;
}
