package com.github.kornilova_l.profiler.logger;

/**
 * This thread is set as shutdown hook
 * it waits for daemon Logger-thread to finish logging
 */
public class WaitingLoggingToFinish extends Thread {
    public WaitingLoggingToFinish(String name) {
        super(name);
    }

    @Override
    public void run() {
        Logger logger = Logger.getInstance();
        logger.printStatus();
        while (!logger.isDone) { // wait for logger to log all events
            Thread.yield();
        }
        logger.closeOutputStream();
        logger.printDataForHuman();
    }
}
