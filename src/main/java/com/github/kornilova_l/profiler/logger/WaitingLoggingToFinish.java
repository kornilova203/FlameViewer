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
        while (!logger.isDone()) { // wait for logger to log all events
            try {
                Thread.sleep(100); // Logger may dequeue queue but did not have time to update isWriting
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!logger.isDone()) { // if still writing
                Thread.yield();
            } else { // queue is empty and logger is not writing
                logger.closeOutputStream();
                logger.printDataForHuman();
                return;
            }
        }
    }
}
