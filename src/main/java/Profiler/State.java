package Profiler;

public class State {
    private final String name;
    private final int threadHashCode;

    State(String name, int threadHashCode) {
        this.name = name;
        this.threadHashCode = threadHashCode;
    }

    public void methodFinish() {
        Profiler.writeToFile(threadHashCode + " f " + name + " " + System.nanoTime());
    }
}