package me.artspb.javax.mail.transport.cache;

import me.artspb.javax.mail.util.TransportUtils;

import javax.mail.Transport;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Artem Khvastunov
 */
public class CleanableLeasedCache implements LeasedCache {

    private final Map<Transport, Long> cache;
    private final Timer timer;
    private final long cleanInterval;

    private volatile boolean stopped = false;

    public CleanableLeasedCache(long cleanInterval) {
        this.cache = new ConcurrentHashMap<>();
        this.timer = new Timer(true);
        this.cleanInterval = cleanInterval;
        scheduleCleaningTask();
    }

    private void scheduleCleaningTask() {
        timer.schedule(new CleaningTask(), cleanInterval, cleanInterval);
    }

    @Override
    public void add(Transport transport) {
        if (stopped) {
            throw new IllegalStateException("Cache has been stopped");
        }
        cache.put(transport, System.currentTimeMillis());
    }

    @Override
    public void remove(Transport transport) {
        cache.remove(transport);
    }

    @Override
    public void stop() {
        stopped = true;
    }

    private final class CleaningTask extends TimerTask {

        @Override
        public void run() {
            for (Map.Entry<Transport, Long> entry : cache.entrySet()) {
                if (System.currentTimeMillis() - entry.getValue() > cleanInterval) {
                    Transport transport = entry.getKey();
                    if (cache.remove(transport) != null) {
                        TransportUtils.closeQuietly(transport);
                    }
                }
            }
            if (stopped && cache.isEmpty()) {
                timer.cancel();
            }
        }
    }
}
