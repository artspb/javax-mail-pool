package me.artspb.javax.mail.session;

import lombok.*;
import me.artspb.javax.mail.util.MapUtils;

import javax.mail.Authenticator;
import javax.mail.Session;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Artem Khvastunov
 */
@RequiredArgsConstructor
public class CachedSessionFactory {

    private final ConcurrentMap<Key, Session> cache = new ConcurrentHashMap<>();
    @NonNull
    private final SessionProvider provider;

    public CachedSessionFactory() {
        this(new DefaultSessionProvider());
    }

    public Session getSession(@NonNull Properties properties) {
        return getSession(properties, null);
    }

    public Session getSession(@NonNull Properties properties, Authenticator authenticator) {
        Key key = new Key(properties, authenticator);
        Session session = cache.get(key);
        if (session == null) {
            Session candidate = provider.getSession(properties, authenticator);
            session = MapUtils.putOrGet(cache, key, candidate);
        }
        return session;
    }

    public void clearCache() {
        cache.clear();
    }

    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Key {

        private final Properties properties;
        private final Authenticator authenticator;
    }

    private static final class DefaultSessionProvider implements SessionProvider {

        @Override
        public Session getSession(Properties properties, Authenticator authenticator) {
            return Session.getInstance(properties, authenticator);
        }
    }
}
