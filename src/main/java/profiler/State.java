package profiler;

public class State {
    private final String name;
    private final long threadId;

    State(String name, long threadId) {
        this.name = name;
        this.threadId = threadId;
    }

    public void methodFinish() {
        Profiler.writeToFile(threadId + " f " + name + " " + System.nanoTime());
    }
}