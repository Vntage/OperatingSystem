package OperatingSystems;

// HelloWorld.java
public class HelloWorld extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            System.out.println("Hello World!!");
            cooperate(); // Allow the OS to switch processes
        }
    }

    @Override
    public void run() {
        main();
    }
}