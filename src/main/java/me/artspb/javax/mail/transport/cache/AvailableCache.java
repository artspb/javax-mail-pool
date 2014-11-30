package me.artspb.javax.mail.transport.cache;

import javax.mail.Session;
import javax.mail.Transport;

/**
 * @author Artem Khvastunov
 */
public interface AvailableCache {

    void add(Session session, Transport transport);

    Transport poll(Session session);

    void remove(Session session, Transport transport);

    void stop();
}
