package profiler;

import java.util.LinkedList;

public class LoggingQueue {
    private static final LinkedList<EventData> queue = new LinkedList<>();

    public synchronized static void enqueue(EventData eventData) {
        queue.addLast(eventData);
    }

    synchronized static EventData dequeue() {
        return queue.removeFirst();
    }

    synchronized static boolean isEmpty() {
        return queue.isEmpty();
    }
}
