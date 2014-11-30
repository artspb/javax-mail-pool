package me.artspb.javax.mail.transport.cache;

import javax.mail.Transport;

/**
 * @author Artem Khvastunov
 */
public interface LeasedCache {

    void add(Transport transport);

    void remove(Transport transport);

    void stop();
}
