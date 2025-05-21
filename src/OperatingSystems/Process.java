package OperatingSystems;

import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable {
    private Thread thread; // Thread to run the process
    private Semaphore semaphore; // Semaphore to control process execution
    private boolean quantumExpired; // Flag to indicate if the quantum has expired
    private boolean isDone; // Flag to indicate if the process is done executing

    // Constructor
    public Process() {
        this.semaphore = new Semaphore(0); // Initialize semaphore with 0 permits
        this.quantumExpired = false; // Quantum is not expired initially
        this.isDone = false; // Process is not done initially
        this.thread = new Thread(this); // Create a new thread for this process
    }

    // Request the process to stop (quantum expired)
    public void requestStop() {
        this.quantumExpired = true; // Set the quantum expired flag
    }

    // Abstract method to be implemented by subclasses
    public abstract void main();

    // Check if the process is stopped (semaphore has 0 permits)
    public boolean isStopped() {
        return semaphore.availablePermits() == 0; // True if no permits are available
    }

    // Check if the process is done (thread is not alive)
    public boolean isDone() {
        return !thread.isAlive(); // True if the thread is not running
    }

    // Start the process (release the semaphore and start the thread)
    public void start() {
        semaphore.release(); // Increment the semaphore to allow the process to run
        thread.start(); // Start the thread
    }

    // Stop the process (acquire the semaphore to block the thread)
    public void stop() {
        try {
            semaphore.acquire(); // Decrement the semaphore to block the process
        } catch (InterruptedException e) {
            e.printStackTrace(); // Handle interruption
        }
    }

    // Run method called by the thread
    @Override
    public void run() {
        try {
            semaphore.acquire(); // Wait for the semaphore to be released
            main(); // Call the main method of the process
        } catch (InterruptedException e) {
            e.printStackTrace(); // Handle interruption
        } finally {
            isDone = true; // Mark the process as done
        }
    }

    // Cooperate with the OS to switch processes if the quantum has expired
    public void cooperate() {
        if (quantumExpired) { // Check if the quantum has expired
            quantumExpired = false; // Reset the quantum expired flag
            OS.switchProcess(); // Call the OS to switch processes
        }
    }
}