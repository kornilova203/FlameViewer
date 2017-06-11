package profiler;

/**
 * This thread is set as shutdown hook
 * it waits for daemon Logger-thread to finish logging
 */
public class WaitingLoggingToFinish extends Thread {
    WaitingLoggingToFinish(String name) {
        super(name);
    }

    @Override
    public void run() {
        while (!Agent.loggingQueue.isEmpty()) { // wait for logger to log all events
            Thread.yield();
        }
        Logger.printDataForHuman();
    }
}
