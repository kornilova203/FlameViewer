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
        logger.printStatus();
        while (!logger.isDone) { // wait for logger to log all events
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Output stream was closed");
            }
        }
        logger.closeOutputStream();
    }
}
