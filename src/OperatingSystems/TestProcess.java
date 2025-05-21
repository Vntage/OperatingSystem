package OperatingSystems;

import static java.lang.System.exit;

public class TestProcess extends UserlandProcess {

    public TestProcess() {}

    @Override
    public void main() {
        System.out.println("MemoryTestProcess: Allocating memory...");

        int ptr = OS.AllocateMemory(128); // Allocate 128 bytes

        System.out.println("MemoryTestProcess: Writing to memory...");
        for (int i = 0; i < 128; i++) {
            Hardware.writeMemory(ptr + i, (byte) (i % 256));
        }

        System.out.println("MemoryTestProcess: Reading from memory...");
        for (int i = 0; i < 128; i++) {
            byte val = Hardware.readMemory(ptr + i);
            System.out.println("Value at " + (ptr + i) + " = " + val);
        }

        System.out.println("MemoryTestProcess: Freeing memory...");
        boolean success = OS.FreeMemory(ptr, 128);
        System.out.println("MemoryTestProcess: Memory free success: " + success);

        OS.Exit(); // Exit process
    }

    @Override
    public void run() {
        long pid = ProcessHandle.current().pid();
        System.out.println("TestProcess started. PID: " + pid);

        System.out.println("TestProcess sleeping for 2 seconds...");
        try {
            Thread.sleep(2000); // Sleep for 2000 milliseconds (2 seconds)
        } catch (InterruptedException e) {
            System.err.println("Sleep interrupted: " + e.getMessage());
        }

        System.out.println("TestProcess exiting.");
        exit(0);
    }
}
