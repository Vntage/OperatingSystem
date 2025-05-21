package OperatingSystems;

// Main.java
public class Main {
    public static void main(String[] args) {
        // Startup the OS with the HelloWorld process as the initial process
        OS.Startup(new Init());
    }
}