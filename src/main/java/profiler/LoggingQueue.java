package profiler;

import java.util.LinkedList;

public class LoggingQueue {
    private LinkedList<EventData> queue = new LinkedList<>();

    public synchronized void enqueue(EventData eventData) {
        queue.addLast(eventData);
    }

    public synchronized EventData dequeue() {
        return queue.removeLast();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}
