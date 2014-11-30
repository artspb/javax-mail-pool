package me.artspb.javax.mail.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author Artem Khvastunov
 */
public class BenchmarksRunner {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include("me.artspb.javax.mail.benchmark.*")
                .build();

        new Runner(opt).run();
    }
}
