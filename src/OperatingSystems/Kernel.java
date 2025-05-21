package OperatingSystems;

import java.util.HashMap;

public class Kernel extends Process {
    private VFS vfs = new VFS();
    private KernelMessage km = new KernelMessage();
    public static HashMap<Integer, PCB> pcbTable = new HashMap<>();


    public Kernel() {
        System.out.println("Initializing Kernel...");
        vfs = new VFS();  // Initialize virtual file system
        km = new KernelMessage();  // Initialize KernelMessage handling
        pcbTable = new HashMap<>();  // Initialize process control block table

        // Start timer interrupt every 1000ms (1 second) to check sleeping and context switch
        Hardware.startTimer(() -> {
            Scheduler.checkSleepingProcesses();

            // Simulate time-slice preemption
            if (Scheduler.currentlyRunning != null) {
                Scheduler.currentlyRunning.requestStop();  // Signal process to stop
            }

            Scheduler.SwitchProcess();
        }, 1000);

        System.out.println("Kernel Initialized.");
    }


    @Override
    public void main() {
        while (true) { // Warning on infinite loop is OK...
            // Check that parameters are available before accessing them
            if (OS.parameters.size() >= 2) {
                System.out.println("hhe");
                switch (OS.currentCall) { // get a job from OS, do it
                    case CreateProcess ->
                        // Note how we get parameters from OS and set the return value
                        OS.retVal = CreateProcess((UserlandProcess) OS.parameters.get(0), (OS.PriorityType) OS.parameters.get(1));
                    case SwitchProcess -> SwitchProcess();


                    // Priority Scheduler
                    case Sleep -> Sleep((int) OS.parameters.get(0));
                    case GetPID -> OS.retVal = GetPid();
                    case Exit -> Exit();

                    // Devices
                    case Open -> Open((String) OS.parameters.get(0));
                    case Close -> Close((int) OS.parameters.get(0));
                    case Read -> Read((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    case Seek -> Seek((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    case Write -> Write((int) OS.parameters.get(0), (byte[]) OS.parameters.get(1));

                    // Messages
                    case GetPIDByName -> OS.retVal = GetPidByName((String) OS.parameters.get(0));
                    case SendMessage -> SendMessage(km);
                    case WaitForMessage -> OS.retVal = WaitForMessage();

                    // Memory
                    case GetMapping -> GetMapping((int) OS.parameters.get(0));
                    case AllocateMemory -> OS.retVal = AllocateMemory((int) OS.parameters.get(0));
                    case FreeMemory -> FreeMemory((int) OS.parameters.get(0), (int) OS.parameters.get(1));

                }
            } else {
                System.out.println("Error: Missing parameters for current system call");
            }
        }
    }

    private void SwitchProcess() {
        System.out.println("OS.currentCall: " + OS.currentCall);
        // Check if there are processes in the scheduler
        if (Scheduler.processList.isEmpty()) {
            return;  // No processes to switch to
        }

        // If there is a currently running process, stop it
        if (Scheduler.currentlyRunning != null) {
            Scheduler.currentlyRunning.stop();  // Stop the currently running process
        }

        // Set the next process as the currently running one
        Scheduler.currentlyRunning = Scheduler.processList.removeFirst();

        // Start the next process
        Scheduler.currentlyRunning.start();  // Start the new process
    }


    // For assignment 1, you can ignore the priority. We will use that in assignment 2
    private int CreateProcess(UserlandProcess up, OS.PriorityType priority) {

        // Create a new PCB (Process Control Block) for the process
        PCB pcb = new PCB(up, priority);

        // Add the PCB to the scheduler
        Scheduler.addProcess(pcb);

        // Return the PID of the created process
        return pcb.getPid();
    }


    private void Sleep(int milliseconds) {
        Scheduler.Sleep(milliseconds);
    }

    private void Exit() {
        if (Scheduler.currentlyRunning != null) {
            System.out.println("Process " + Scheduler.currentlyRunning.getPid() + " is exiting.");
            FreeAllMemory(Scheduler.currentlyRunning);
            Scheduler.removeProcess(Scheduler.currentlyRunning);
            Scheduler.SwitchProcess(); // Choose another process to run
        }
    }

    private int GetPid() {
        return (Scheduler.currentlyRunning != null) ? Scheduler.currentlyRunning.getPid() : -1;
    }


    private int Open(String s) {
        return vfs.open(s);
    }

    private void Close(int id) {
        vfs.close(id);
    }

    private byte[] Read(int id, int size) {
        return vfs.read(id, size);
    }

    private void Seek(int id, int to) {
      vfs.seek(id, to);
    }

    private int Write(int id, byte[] data) {
        return vfs.write(id, data);
    }

    private void SendMessage(KernelMessage km) {
        if (Scheduler.currentlyRunning == null) return;

        KernelMessage messageCopy = new KernelMessage(km); // Use copy constructor
        messageCopy.setSenderPid(Scheduler.currentlyRunning.getPid());

        PCB target = Scheduler.pcbTable.get(messageCopy.getTargetPid());

        if (target == null) {
            System.out.println("Error: Target PID " + messageCopy.getTargetPid() + " not found.");
            return;
        }

        target.addMessage(messageCopy);

        if (Scheduler.waitingForMessages.contains(target.getPid())) {
            Scheduler.waitingForMessages.remove(target.getPid());
            Scheduler.addProcess(target);
        }
    }

    private KernelMessage WaitForMessage() {
        if (Scheduler.currentlyRunning == null) return null;

        KernelMessage msg = Scheduler.currentlyRunning.getNextMessage();

        if (msg != null) {
            return msg;
        } else {
            Scheduler.waitingForMessages.add(Scheduler.currentlyRunning.getPid());
            Scheduler.SwitchProcess();
            return null;
        }
    }

    private int GetPidByName(String name) {
        for (PCB process : Scheduler.processList) {
            if (process.getName().equals(name)) {
                return process.getPid();  // Return the PID of the matching process
            }
        }
        return -1;  // Return -1 if no process with that name is found
    }

    private void GetMapping(int virtualPage) {
        if (Scheduler.currentlyRunning == null) return;

        VirtualToPhysicalMapping[] pageTable = Scheduler.currentlyRunning.getPageTable();
        if (virtualPage < 0 || virtualPage >= pageTable.length) {
            System.out.println("Invalid virtual page number: " + virtualPage);
            return;
        }

        VirtualToPhysicalMapping mapping = pageTable[virtualPage];
        if (!mapping.isMapped()) {
            System.out.println("Virtual page " + virtualPage + " is not mapped.");
        } else {
            int physicalPage = mapping.physicalPageNumber;
            System.out.println("Virtual page " + virtualPage + " is mapped to physical page " + physicalPage);
            // Update the TLB
            Hardware.updateTLB(virtualPage, physicalPage);
        }
    }

    public void handlePageFault(int virtualPage, PCB current) {
        int physicalPage = Hardware.allocatePage();

        if (physicalPage == -1) {
            // Memory full, need to evict a page from another process
            PCB victim = Scheduler.getRandomProcess();

            if (victim != null && victim != current) {
                evictPageFrom(victim);
                physicalPage = Hardware.allocatePage(); // Try again
            }

            if (physicalPage == -1) {
                System.out.println("Still no free memory, exiting...");
                OS.Exit();
                return;
            }
        }

        current.getPageTable()[virtualPage].mapTo(virtualPage, physicalPage);
        Hardware.updateTLB(virtualPage, physicalPage);
        System.out.println("Mapped virtual page " + virtualPage + " to physical page " + physicalPage);
    }

    private void evictPageFrom(PCB victim) {
        VirtualToPhysicalMapping[] pageTable = victim.getPageTable();
        for (int i = 0; i < pageTable.length; i++) {
            VirtualToPhysicalMapping mapping = pageTable[i];
            if (mapping != null && mapping.isMapped()) {
                Hardware.freePage(mapping.getPhysicalPage());
                mapping.unmap();
                System.out.println("Evicted page " + i + " from process " + victim.pid);
                return;
            }
        }
    }

    private int AllocateMemory(int size) {
        if (Scheduler.currentlyRunning == null) return -1;

        VirtualToPhysicalMapping[] pageTable = Scheduler.currentlyRunning.getPageTable();
        int pagesNeeded = (int) Math.ceil((double) size / Hardware.PAGE_SIZE);

        int baseVirtualAddress = -1;
        int contiguousFree = 0;

        // Search for contiguous free slots in page table
        for (int i = 0; i < pageTable.length; i++) {
            if (!pageTable[i].isMapped()) {
                if (contiguousFree == 0) baseVirtualAddress = i * Hardware.PAGE_SIZE;
                contiguousFree++;
                if (contiguousFree == pagesNeeded) break;
            } else {
                contiguousFree = 0;
                baseVirtualAddress = -1;
            }
        }

        if (contiguousFree < pagesNeeded) {
            System.out.println("Not enough virtual memory available.");
            return -1;
        }

        // Allocate physical pages and map them
        int startPage = baseVirtualAddress / Hardware.PAGE_SIZE;
        for (int i = 0; i < pagesNeeded; i++) {
            int virtualPage = startPage + i;
            int physicalPage = Hardware.allocatePage(); // from memory simulator
            if (physicalPage == -1) {
                System.out.println("Out of physical memory. Cleaning up...");
                for (int j = 0; j < i; j++) {
                    Hardware.freePage(pageTable[startPage + j].physicalPageNumber);
                    pageTable[startPage + j].unmap();
                }
                return -1;
            }
            pageTable[virtualPage].mapTo(virtualPage, physicalPage);
        }

        return baseVirtualAddress;
    }

    private void FreeMemory(int pointer, int size) {
        if (Scheduler.currentlyRunning == null) return;

        VirtualToPhysicalMapping[] pageTable = Scheduler.currentlyRunning.getPageTable();
        int startPage = pointer / Hardware.PAGE_SIZE;
        int pagesToFree = (int) Math.ceil((double) size / Hardware.PAGE_SIZE);

        for (int i = 0; i < pagesToFree; i++) {
            int virtualPage = startPage + i;
            if (pageTable[virtualPage].isMapped()) {
                Hardware.freePage(pageTable[virtualPage].physicalPageNumber);
                pageTable[virtualPage].unmap();
            }
        }
    }


    private void FreeAllMemory(PCB process) {
        if (process == null) return;

        VirtualToPhysicalMapping[] pageTable = process.getPageTable();
        for (int i = 0; i < pageTable.length; i++) {
            for (VirtualToPhysicalMapping mapping : pageTable) {
                if (mapping.isMapped()) {
                    Hardware.freePage(mapping.physicalPageNumber);
                    mapping.unmap();
                }
            }
        }
    }

}
