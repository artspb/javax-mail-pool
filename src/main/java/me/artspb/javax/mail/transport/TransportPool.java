package me.artspb.javax.mail.transport;

import me.artspb.javax.mail.transport.cache.AvailableCache;
import me.artspb.javax.mail.transport.cache.CleanableAvailableCache;
import me.artspb.javax.mail.transport.cache.CleanableLeasedCache;
import me.artspb.javax.mail.transport.cache.LeasedCache;
import me.artspb.javax.mail.util.TransportUtils;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * @author Artem Khvastunov
 */
public class TransportPool {

    private static final int LEASED_CLEAN_INTERVAL = 60 * 60;
    private static final int AVAILABLE_ACCESS_TIMEOUT = 60 * 60;

    private final LeasedCache leased;
    private final AvailableCache available;
    private final TransportEventsListener listener;
    private final TransportProvider provider;

    private volatile boolean stopped = false;

    public TransportPool() {
        leased = new CleanableLeasedCache(LEASED_CLEAN_INTERVAL);
        available = new CleanableAvailableCache(AVAILABLE_ACCESS_TIMEOUT);
        listener = new TransportEventsListener(this);
        provider = new DefaultTransportProvider();
    }

    public Transport lease(Session session) throws MessagingException {
        checkStopped();
        Transport transport = checkStaleAndGet(session);
        if (transport == null) {
            transport = createTransport(session);
        }
        leased.add(transport);
        return transport;
    }

    private void checkStopped() {
        if (stopped) {
            throw new IllegalStateException("Pool has been stopped");
        }
    }

    private Transport checkStaleAndGet(Session session) {
        Transport transport;
        do {
            transport = available.poll(session);
        } while (transport != null && !transport.isConnected());
        return transport;
    }

    private Transport createTransport(Session session) throws MessagingException {
        Transport transport = provider.getTransport(session);
        transport.addTransportListener(listener);
        transport.connect();
        return transport;
    }

    public void release(Session session, Transport transport) {
        leased.remove(transport);
        if (stopped) {
            TransportUtils.closeQuietly(transport);
        } else {
            available.add(session, transport);
        }
    }

    public void remove(Session session, Transport transport) {
        TransportUtils.closeQuietly(transport);
        leased.remove(transport);
        available.remove(session, transport);
    }

    public void stop() {
        stopped = true;
        leased.stop();
        available.stop();
    }

    private static class DefaultTransportProvider implements TransportProvider {

        @Override
        public Transport getTransport(Session session) throws NoSuchProviderException {
            return session.getTransport();
        }
    }
}
