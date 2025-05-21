package OperatingSystems;

import java.util.Arrays;
import java.util.LinkedList;

public class PCB {
    private static int nextPid = 1;  // Static variable to keep track of the next process ID
    public int pid;  // Process ID
    private OS.PriorityType priority;  // Priority level of the process
    private UserlandProcess userlandProcess;  // The userland process associated with this PCB
    private int executionTime = 0;  // Time the process has been executing
    private int[] openFiles = new int[10];  // Array to track open files (max 10 files)
    private LinkedList<KernelMessage> messageQueue = new LinkedList<>();
    private VirtualToPhysicalMapping[] pageTable;

    // Constructor initializes the PCB with a userland process and priority
    public PCB(UserlandProcess up, OS.PriorityType priority) {
        this.pid = nextPid++;  // Assign a unique PID to the process
        this.userlandProcess = up;  // Set the userland process
        this.priority = priority;  // Set the priority

        // Initialize openFiles array with -1 (indicating no files are open)
        Arrays.fill(openFiles, -1);

        // Initialize the page table with -1 (indicating no mappings)
        this.pageTable = new VirtualToPhysicalMapping[256];  // Assuming 256 virtual pages
        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = new VirtualToPhysicalMapping();  // -1 means no physical page is mapped
        }
    }

    // Starts the userland process
    public void start() {
        userlandProcess.start();
    }

    // Checks if the userland process is done
    public boolean isDone() {
        return userlandProcess.isDone();
    }

    // Sets a new priority for the process
    public void setPriority(OS.PriorityType newPriority) {
        priority = newPriority;
    }

    // Requests the userland process to stop
    public void requestStop() {
        userlandProcess.requestStop();
    }

    // Returns the name of the userland process class
    public String getName() {
        return userlandProcess.getClass().getSimpleName();
    }

    // Stop the process (currently empty, may be expanded)
    public void stop() {
        // Remove the process from the scheduler (if needed)
        Scheduler.removeProcess(this);

        // Log the termination (for debugging)
        System.out.println("Process " + pid + " has been stopped and terminated.");
    }

    // Returns the process ID
    public int getPid() {
        return pid;
    }

    // Increment the execution time of the process and check if priority should be demoted
    public void incrementExecutionTime(int millis) {
        executionTime += millis;
        if (executionTime > 5000) {  // Example: If process runs for more than 5 seconds
            demotePriority();  // Demote the priority
            executionTime = 0;  // Reset execution time
        }
    }

    // Demotes the priority of the process based on its current priority
    public void demotePriority() {
        if (priority == OS.PriorityType.realtime) {
            priority = OS.PriorityType.interactive;
        } else if (priority == OS.PriorityType.interactive) {
            priority = OS.PriorityType.background;
        }
        System.out.println("Process " + pid + " demoted to " + priority);  // Output the new priority
    }

    // Accessor method to get the open files array
    public int[] getOpenFiles() {
        return openFiles;
    }

    public KernelMessage getNextMessage() {
        return messageQueue.isEmpty() ? null : messageQueue.removeFirst();
    }

    public void addMessage(KernelMessage km) {
        messageQueue.add(km);
    }

    public VirtualToPhysicalMapping[] getPageTable() {
        return pageTable;
    }

    // This method updates the page table with the data for the given page
    public void updatePageTable(int pageIndex, byte[] pageData) {
        if (pageTable[pageIndex] == null) {
            pageTable[pageIndex] = new VirtualToPhysicalMapping(pageIndex, 0);
        }

        pageTable[pageIndex].setPageData(pageData);
    }
}
