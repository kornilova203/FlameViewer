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
        while (!Logger.queue.isEmpty() || Logger.isWriting) { // wait for logger to log all events
            try {
                Thread.sleep(100); // Logger may dequeue queue but did not have time to update isWriting
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Logger.isWriting) { // if still writing
                Thread.yield();
            } else { // queue is empty and logger is not writing
                try {
                    Logger.outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Logger.printDataForHuman();
                return;
            }
        }
    }
}
