package OperatingSystems;

public class Testing {
    public static void main(String[] args) {
        // Initialize the OS and start with the first process (TestProcess)
        OS.Startup(new TestProcess());

        // Create three new processes with different priority types
        int pid1 = OS.CreateProcess(new TestProcess(), OS.PriorityType.realtime);  // Realtime process
        int pid2 = OS.CreateProcess(new TestProcess(), OS.PriorityType.interactive);  // Interactive process
        int pid3 = OS.CreateProcess(new TestProcess(), OS.PriorityType.background);  // Background process

        // Check the process IDs created for the new processes
        System.out.println("Process IDs: " + pid1 + ", " + pid2 + ", " + pid3);

        // Simulate a sleep for 50 milliseconds (time the system would be idle)
        OS.Sleep(50);

        // Test switching process
        System.out.println("Switching process...");
        OS.switchProcess();

        // Test fetching the process ID of the current running process
        int currentPID = OS.GetPID();
        System.out.println("Current PID: " + currentPID);

        // Test process exit
        System.out.println("Exiting current process...");
        OS.Exit();

        // Test creating a process with a specific priority type
        int pid4 = OS.CreateProcess(new TestProcess(), OS.PriorityType.realtime);  // Realtime process
        int pid5 = OS.CreateProcess(new TestProcess(), OS.PriorityType.background);  // Background process
        System.out.println("Created more processes with new priority types. PIDs: " + pid4 + ", " + pid5);

        // Test the process sleep for a longer period
        System.out.println("Putting the process to sleep for 200ms...");
        OS.Sleep(200);

        // Exit the system with multiple processes
        OS.Exit();
    }
}
