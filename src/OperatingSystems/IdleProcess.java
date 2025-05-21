package OperatingSystems;

public class IdleProcess extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            try {
                cooperate();
                Thread.sleep(50);
            } catch (Exception e) { }
        }
    }

    @Override
    public void run() {
        // Start the main method of the process
        main();
    }
}
