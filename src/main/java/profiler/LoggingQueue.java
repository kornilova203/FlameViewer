package profiler;

import java.util.LinkedList;

public class LoggingQueue {
    private final LinkedList<EventData> queue = new LinkedList<>();

    public synchronized void enqueue(EventData eventData) {
        queue.addLast(eventData);
    }

    synchronized EventData dequeue() {
        return queue.removeLast();
    }

    synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}
