package me.artspb.javax.mail.benchmark;

import me.artspb.javax.mail.SmtpServer;
import me.artspb.javax.mail.transport.TransportPool;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
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
public class TransportBenchmark {

    private TransportPool pool;
    private SmtpServer smtpServer;
    private Session session;

    @Setup
    public void setUp() {
        pool = new TransportPool();
        smtpServer = new SmtpServer();
        smtpServer.start();
        session = Session.getInstance(getProperties(smtpServer.getPort()), getAuthenticator());
    }

    @TearDown
    public void tearDown() {
        pool.stop();
        smtpServer.stop();
    }

    @Benchmark
    @Threads(1)
    public void obtainTransportFromPool() throws MessagingException {
        Transport transport = pool.lease(session);
        pool.release(session, transport);
    }

    @Benchmark
    @Threads(1)
    public void createNewTransport() throws MessagingException {
        Transport transport = session.getTransport();
        transport.connect();
        transport.close();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TransportBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
