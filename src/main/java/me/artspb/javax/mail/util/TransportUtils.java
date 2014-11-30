package me.artspb.javax.mail.util;

import javax.mail.MessagingException;
import javax.mail.Transport;

/**
 * @author Artem Khvastunov
 */
public class TransportUtils {

    private TransportUtils() {
    }

    public static void closeQuietly(Transport transport) {
        try {
            if (transport != null) {
                transport.close();
            }
        } catch (MessagingException ignored) {
        }
    }
}
