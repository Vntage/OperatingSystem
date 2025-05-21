package OperatingSystems;

// GoodbyeWorld.java
public class GoodbyeWorld extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            System.out.println("Goodbye World!!");
            cooperate(); // Allow the OS to switch processes
        }
    }

    @Override
    public void run() {
        main();
    }
}