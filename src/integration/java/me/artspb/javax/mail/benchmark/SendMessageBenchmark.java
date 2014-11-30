package me.artspb.javax.mail.benchmark;

import me.artspb.javax.mail.SmtpServer;
import me.artspb.javax.mail.session.CachedSessionFactory;
import me.artspb.javax.mail.transport.TransportPool;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static me.artspb.javax.mail.Configuration.*;
import static org.openjdk.jmh.annotations.Threads.MAX;

/**
 * @author Artem Khvastunov
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 10)
@Measurement(iterations = 3, time = 10)
@Fork(1)
public class SendMessageBenchmark {

    private static final Properties PROPERTIES = getProperties();
    private static final Authenticator AUTHENTICATOR = getAuthenticator();
    private static final MimeMessage MESSAGE = getMessage();
    private static final Address[] RECIPIENTS = getRecipients();

    private CachedSessionFactory factory;
    private TransportPool pool;
    private SmtpServer smtpServer;

    @Setup
    public void setUp() {
        factory = new CachedSessionFactory();
        pool = new TransportPool();
        smtpServer = new SmtpServer();
        smtpServer.start();
        PROPERTIES.setProperty("mail.smtp.port", String.valueOf(smtpServer.getPort()));
    }

    @TearDown
    public void tearDown() {
        pool.stop();
        smtpServer.stop();
    }

    @Benchmark
    @Threads(1)
    public void sendMailWithPoolSingleThread() throws MessagingException {
        withPool();
    }

    @Benchmark
    @Threads(MAX)
    public void sendMailWithPoolManyThreads() throws MessagingException {
        withPool();
    }

    private void withPool() throws MessagingException {
        Session session = factory.getSession(PROPERTIES, AUTHENTICATOR);
        Transport transport = pool.lease(session);
        transport.sendMessage(MESSAGE, RECIPIENTS);
    }

    @Benchmark
    @Threads(1)
    public void sendMailWithoutPoolSingleThread() throws MessagingException {
        withoutPool();
    }

    @Benchmark
    @Threads(MAX)
    public void sendMailWithoutPoolManyThreads() throws MessagingException {
        withoutPool();
    }

    private void withoutPool() throws MessagingException {
        Session session = Session.getInstance(PROPERTIES, AUTHENTICATOR);
        Transport transport = session.getTransport();
        transport.connect();
        transport.sendMessage(MESSAGE, RECIPIENTS);
        transport.close();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SendMessageBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
