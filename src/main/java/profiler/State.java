package profiler;

public class State {
    private final String name;
    private final long threadId;

    State(String name, long threadId) {
        this.name = name;
        this.threadId = threadId;
    }

    public void methodFinish(String returnVal) {
        Profiler.log(threadId + " f " + name + " " + returnVal + " " + System.nanoTime());
    }
}