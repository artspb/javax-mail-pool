package me.artspb.javax.mail.transport;

import lombok.SneakyThrows;

import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import java.lang.reflect.Field;

/**
 * @author Artem Khvastunov
 */
class TransportEventsListener implements TransportListener {

    private static final String SESSION_FIELD_NAME = "session";

    private final TransportPool pool;
    private final Field sessionField;

    @SneakyThrows(NoSuchFieldException.class)
    TransportEventsListener(TransportPool pool) {
        this.pool = pool;
        Field sessionField = Service.class.getDeclaredField(SESSION_FIELD_NAME);
        sessionField.setAccessible(true);
        this.sessionField = sessionField;
    }

    @Override
    public void messageDelivered(TransportEvent e) {
        release(e);
    }

    @Override
    public void messagePartiallyDelivered(TransportEvent e) {
        release(e);
    }

    @Override
    public void messageNotDelivered(TransportEvent e) {
        remove(e);
    }

    private void release(TransportEvent e) {
        Transport transport = (Transport) e.getSource();
        Session session = getSession(transport);
        pool.release(session, transport);
    }

    private void remove(TransportEvent e) {
        Transport transport = (Transport) e.getSource();
        Session session = getSession(transport);
        pool.remove(session, transport);
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private Session getSession(Service service) {
        return (Session) sessionField.get(service);
    }
}
