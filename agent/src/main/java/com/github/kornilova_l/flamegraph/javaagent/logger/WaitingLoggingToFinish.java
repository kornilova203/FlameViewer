package com.github.kornilova_l.flamegraph.javaagent.logger;

/**
 * This thread is set as shutdown hook
 * it waits for daemon Logger-thread to finish logging
 */
public class WaitingLoggingToFinish extends Thread {
    private Logger logger;
    public WaitingLoggingToFinish(String name, Logger logger) {
        super(name);
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.finish();
    }
}
