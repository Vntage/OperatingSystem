package OperatingSystems;

import java.util.Timer;
import java.util.TimerTask;

public class RandomProcessTest extends UserlandProcess{

    private static VFS vfs = new VFS();  // Create an instance of VFS

    public void test() {
        // Step 1: Select a random process using getRandomProcess
        PCB randomProcess = Scheduler.getRandomProcess();

        if (randomProcess == null) {
            System.out.println("No process available for testing.");
            return;
        }

        System.out.println("Selected process " + randomProcess.getPid() + " for memory test.");

        // Step 2: Inspect the process's page table to find pages that need to be swapped
        VirtualToPhysicalMapping[] pageTable = randomProcess.getPageTable();
        boolean anyPagesToSwap = false;

        for (int i = 0; i < pageTable.length; i++) {
            VirtualToPhysicalMapping mapping = pageTable[i];
            if (mapping != null && mapping.getPhysicalPage() == -1) {
                System.out.println("Process " + randomProcess.getPid() + " has page " + i + " that needs to be swapped in.");

                // Step 3: Swap the page in from disk (simulate swapping in)
                byte[] pageData = vfs.readFromSwap(i);  // Use the VFS instance to read from swap
                if (pageData != null) {
                    System.out.println("Swapping in page " + i + " for process " + randomProcess.getPid());
                    // Directly update the page table of the process
                    randomProcess.updatePageTable(i, pageData);
                    anyPagesToSwap = true;
                } else {
                    // If page is not in swap file, we need to write it to swap
                    byte[] pageDataToSwap = mapping.getPageData();
                    System.out.println("Swapping out page " + i + " of process " + randomProcess.getPid() + " to disk.");
                    // Write to swap and update the page table directly
                    vfs.writeToSwap(pageDataToSwap);  // Use the VFS instance to write to swap
                    randomProcess.updatePageTable(i, pageDataToSwap); // Update page table directly
                    anyPagesToSwap = true;
                }
            }
        }

        // If no pages needed swapping, print that out
        if (!anyPagesToSwap) {
            System.out.println("Process " + randomProcess.getPid() + " does not require any pages to be swapped.");
        }
    }

    // Run the memory test periodically (for example, every 2 seconds)
    public void main() {
        Timer memoryTestTimer = new Timer();
        memoryTestTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                test(); // Perform memory test every 2 seconds
            }
        }, 0, 2000); // 2 seconds interval
    }

    @Override
    public void run() {
        main();
    }
}
