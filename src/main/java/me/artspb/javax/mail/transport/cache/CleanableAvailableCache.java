package me.artspb.javax.mail.transport.cache;

import me.artspb.javax.mail.util.MapUtils;
import me.artspb.javax.mail.util.TransportUtils;

import javax.mail.Session;
import javax.mail.Transport;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Artem Khvastunov
 */
public class CleanableAvailableCache implements AvailableCache {

    private final ConcurrentMap<Session, Queue<Transport>> cache;
    private final Map<Session, Long> accessTime;
    private final Timer timer;
    private final long accessPeriod;

    private volatile boolean stopped = false;

    public CleanableAvailableCache(long accessPeriod) {
        this.cache = new ConcurrentHashMap<>();
        this.accessTime = new ConcurrentHashMap<>();
        this.timer = new Timer();
        this.accessPeriod = accessPeriod;
        scheduleRemovingTask();
    }

    private void scheduleRemovingTask() {
        timer.schedule(new RemovingTask(), accessPeriod, accessPeriod);
    }

    @Override
    public void add(Session session, Transport transport) {
        if (stopped) {
            throw new IllegalStateException("Cache has been stopped");
        }
        getTransports(session).add(transport);
    }

    @Override
    public Transport poll(Session session) {
        return getTransports(session).poll();
    }

    @Override
    public void remove(Session session, Transport transport) {
        getTransports(session).remove(transport);
    }

    private Queue<Transport> getTransports(Session session) {
        accessTime.put(session, System.currentTimeMillis());
        Queue<Transport> transports = cache.get(session);
        if (transports == null) {
            Queue<Transport> candidate = new ConcurrentLinkedQueue<>();
            transports = MapUtils.putOrGet(cache, session, candidate);
        }
        return transports;
    }

    @Override
    public void stop() {
        stopped = true;
        for (Queue<Transport> transports : cache.values()) {
            cleanQueue(transports);
        }
    }

    private void cleanQueue(Queue<Transport> transports) {
        Transport transport;
        do {
            transport = transports.poll();
            TransportUtils.closeQuietly(transport);
        } while (transport != null);
    }

    private final class RemovingTask extends TimerTask {

        private final Collection<Queue<Transport>> removed = new ArrayList<>();

        @Override
        public void run() {
            cleanRemoved();
            for (Map.Entry<Session, Long> entry : accessTime.entrySet()) {
                if (timeIsOver(entry.getValue())) {
                    remove(entry.getKey());
                }
            }
            if (stopped && cache.isEmpty() && removed.isEmpty()) {
                accessTime.clear();
                timer.cancel();
            }
        }

        private void cleanRemoved() {
            for (Queue<Transport> transports : removed) {
                if (transports != null) {
                    cleanQueue(transports);
                }
            }
            removed.clear();
        }

        private boolean timeIsOver(Long accessTime) {
            return System.currentTimeMillis() - accessTime > accessPeriod;
        }

        private void remove(Session session) {
            accessTime.remove(session);
            Queue<Transport> transports = cache.remove(session);
            removed.add(transports);
            cleanQueue(transports);
        }
    }
}
