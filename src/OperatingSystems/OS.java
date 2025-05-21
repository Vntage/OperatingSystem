package OperatingSystems;

import java.util.ArrayList;
import java.util.List;

public class OS {
    private static Kernel ki;

    private static Init Init;

    // List that holds parameters passed to the system calls for kernel execution
    public static List<Object> parameters = new ArrayList<>();

    // Return value of the kernel's operations (e.g., process ID, result)
    public static Object retVal = 1;

    // Enum for system call types, representing different actions the OS can take
    public enum CallType {
        SwitchProcess, SendMessage, Open, Close, Read, Seek, Write, GetMapping, CreateProcess,
        Sleep, GetPID, AllocateMemory, FreeMemory, GetPIDByName, WaitForMessage, Exit
    }

    // Variable to store the current system call type (e.g., CreateProcess, Exit)
    public static CallType currentCall;

    // Initializes the kernel, stops the currently running process, and then starts the kernel
    private static void startTheKernel() {
        if (ki == null) {
            ki = new Kernel();
            Thread kernelThread = new Thread(() -> ki.main());
            kernelThread.start();
        }

        // Busy-wait until the kernel sets retVal
        while (retVal == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Kernel returned value: " + retVal);
    }

    // Switches to a new process by clearing parameters and invoking the kernel
    public static void switchProcess() {
        parameters.clear(); // Clear any existing parameters
        currentCall = CallType.SwitchProcess; // Set the system call type to SwitchProcess
        startTheKernel(); // Invoke the kernel to perform the switch
    }

    // Initializes the kernel and sets up the first processes (e.g., Init, Idle, TestProcess)
    public static void Startup(UserlandProcess init) {
        ki = new Kernel(); // Initialize the kernel
        CreateProcess(new Init(), PriorityType.realtime); // Create the Init process with realtime priority
        CreateProcess(new IdleProcess(), PriorityType.background); // Create the Idle process with background priority
    }

    // Enum to define different priority levels for processes
    public enum PriorityType {
        realtime, interactive, background
    }

    // Creates a new process with the default priority (interactive)
    public static int CreateProcess(UserlandProcess up) {
        return CreateProcess(up, PriorityType.interactive); // Default to interactive priority
    }

    // Creates a new process with a specified priority
    public static int CreateProcess(UserlandProcess up, PriorityType priority) {
        parameters.clear();
        parameters.add(up);
        parameters.add(priority);
        currentCall = CallType.CreateProcess;
        startTheKernel();  // Start the kernel

        int pid = (int) retVal;  // Make sure retVal contains the process ID
        System.out.println("Process created with PID: " + pid);
        return pid;  // Return the process ID
    }

    // Get the process ID (pid) of the current process
    public static int GetPID() {
        parameters.clear();
        currentCall = CallType.GetPID; // Set the system call type to GetPID
        startTheKernel(); // Invoke the kernel to get the process ID
        return (int) retVal; // Return the process ID (pid) from the kernel's result
    }

    // Exit the current process by invoking the Exit system call
    public static void Exit() {
        parameters.clear();
        currentCall = CallType.Exit; // Set the system call type to Exit
        startTheKernel();
    }

    // Puts the current process to sleep for the specified number of milliseconds
    public static void Sleep(int mills) {
        parameters.clear();
        parameters.add(mills); // Add the sleep duration (in milliseconds) to the parameters list
        currentCall = CallType.Sleep; // Set the system call type to Sleep
        startTheKernel();
    }

    // Opens a device (e.g., a file or resource) and returns a device identifier
    public static int Open(String s) {
        parameters.clear();
        parameters.add(s); // Add the device name to the parameters list
        currentCall = CallType.Open; // Set the system call type to Open
        startTheKernel(); // Invoke the kernel to handle the open operation
        return (int) retVal;
    }

    // Closes a device specified by its identifier
    public static void Close(int id) {
        parameters.clear();
        parameters.add(id); // Add the device identifier to the parameters list
        currentCall = CallType.Close; // Set the system call type to Close
        startTheKernel();
    }

    // Reads data from a device specified by its identifier and returns the data
    public static byte[] Read(int id, int size) {
        parameters.clear();
        parameters.add(id); // Add the device identifier to the parameters list
        parameters.add(size); // Add the amount of data to read to the parameters list
        currentCall = CallType.Read; // Set the system call type to Read
        startTheKernel();
        return (byte[]) retVal;
    }

    // Seeks to a specific position in a device (e.g., file) based on the given identifier
    public static void Seek(int id, int to) {
        parameters.clear();
        parameters.add(id); // Add the device identifier to the parameters list
        parameters.add(to); // Add the position to seek to the parameters list
        currentCall = CallType.Seek; // Set the system call type to Seek
        startTheKernel();
    }

    // Writes data to a device specified by its identifier
    public static int Write(int id, byte[] data) {
        parameters.clear();
        parameters.add(id); // Add the device identifier to the parameters list
        parameters.add(data); // Add the data to write to the parameters list
        currentCall = CallType.Write; // Set the system call type to Write
        startTheKernel();
        return (int) retVal;
    }

    public static void SendMessage(KernelMessage km) {
        parameters.clear();
        parameters.add(km); // Add the message to the parameters list
        currentCall = CallType.SendMessage; // Set the system call type to SendMessage
        startTheKernel(); // Invoke the kernel to send the message
    }

    public static KernelMessage WaitForMessage() {
        parameters.clear();
        currentCall = CallType.WaitForMessage; // Set the system call type to WaitForMessage
        startTheKernel(); // Invoke the kernel to retrieve a message
        return (KernelMessage) retVal; // Return the received message from the kernel
    }

    public static int GetPidByName(String name) {
        parameters.clear();
        parameters.add(name); // Add the process name to the parameters list
        currentCall = CallType.GetPIDByName; // Set the system call type to GetPIDByName
        startTheKernel(); // Invoke the kernel to get the PID
        return (int) retVal; // Return the process ID (PID) found by the kernel
    }

    public static VirtualToPhysicalMapping GetMapping(int virtualPage) {
        if (Scheduler.currentlyRunning == null) return null;

        VirtualToPhysicalMapping[] pageTable = Scheduler.currentlyRunning.getPageTable();

        if (virtualPage < 0 || virtualPage >= pageTable.length) {
            System.out.println("Invalid virtual page: " + virtualPage);
            return null;
        }

        VirtualToPhysicalMapping mapping = pageTable[virtualPage];

        if (!mapping.isMapped()) {
            System.out.println("Page fault: mapping virtual page " + virtualPage);

            // Delegate to kernel to handle the fault
            ki.handlePageFault(virtualPage, Scheduler.currentlyRunning);

            // After kernel handles it, update TLB again just to be safe
            mapping = pageTable[virtualPage];
            if (mapping.isMapped()) {
                Hardware.updateTLB(virtualPage, mapping.getPhysicalPage());
            } else {
                return null;
            }
        }

        return mapping;
    }

    public static int AllocateMemory(int size) {
        parameters.clear();
        parameters.add(size); // Add size of memory to allocate
        currentCall = CallType.AllocateMemory; // Set system call type
        startTheKernel(); // Ask the kernel to allocate memory
        return (int) retVal; // Return the pointer/address
    }

    public static boolean FreeMemory(int pointer, int size) {
        parameters.clear();
        parameters.add(pointer); // Add pointer of memory to free
        parameters.add(size); // Add size of the block to free
        currentCall = CallType.FreeMemory; // Set system call type
        startTheKernel(); // Ask the kernel to free memory
        return (boolean) retVal; // Return success or failure
    }


}
