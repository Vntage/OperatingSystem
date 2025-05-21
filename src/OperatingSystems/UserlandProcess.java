package OperatingSystems;

public abstract class UserlandProcess {
    private boolean stopRequested = false;
    private boolean done = false;
    private Thread thread;

    public UserlandProcess() {
        thread = new Thread(() -> {
            main();
            done = true;
        });
    }

    // All child classes must implement this
    public abstract void main();

    // Starts the thread
    public void start() {
        thread.start();
    }

    // Check if thread has finished
    public boolean isDone() {
        return done;
    }

    // Set done manually (optional)
    public void setDone(boolean d) {
        done = d;
    }

    // Mark that the process should stop
    public void requestStop() {
        stopRequested = true;
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    // Cooperative multitasking support
    public void cooperate() {
        OS.switchProcess();
    }

    public abstract void run();
}
