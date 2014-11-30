package me.artspb.javax.mail.benchmark;

import me.artspb.javax.mail.session.CachedSessionFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static me.artspb.javax.mail.Configuration.getAuthenticator;
import static me.artspb.javax.mail.Configuration.getProperties;

/**
 * @author Artem Khvastunov
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 10)
@Measurement(iterations = 3, time = 10)
@Fork(1)
public class SessionBenchmark {

    private static final Properties PROPERTIES = getProperties();
    private static final Authenticator AUTHENTICATOR = getAuthenticator();

    private CachedSessionFactory factory;

    @Setup
    public void setUp() {
        factory = new CachedSessionFactory();
    }

    @Benchmark
    @Threads(1)
    public Session obtainSessionFromFactory() throws MessagingException {
        return factory.getSession(PROPERTIES, AUTHENTICATOR);
    }

    @Benchmark
    @Threads(1)
    public Session createNewSession() throws MessagingException {
        return Session.getInstance(PROPERTIES, AUTHENTICATOR);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SessionBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
