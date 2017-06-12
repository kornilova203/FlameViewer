package profiler;

import java.io.IOException;

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
        while (!LoggingQueue.isEmpty()) { // wait for logger to log all events
            Thread.yield();
        }
        try {
            Logger.outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.printDataForHuman();
    }
}
