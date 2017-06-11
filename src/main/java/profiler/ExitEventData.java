package profiler;

public class ExitEventData extends EventData {
    Object returnValue;
    public ExitEventData(long threadId, long exitTime, Object returnValue) {
        this.threadId = threadId;
        this.time = exitTime;
        this.returnValue = returnValue;
    }
}
