package OperatingSystems;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Hardware {
    public static final int PAGE_SIZE = 4096;
    public static final int TOTAL_MEMORY = 1024 * 1024; // 1MB
    private static byte[] memory = new byte[TOTAL_MEMORY];

    private static Timer timer = new Timer("Hardware-Timer");
    private static LinkedList<Integer> freePages = new LinkedList<>();

    // TLB: [0][i] = virtual page, [1][i] = physical page
    private static int[][] tlb = new int[2][2];

    static {
        // Initialize free page list
        for (int i = 0; i < 256; i++) { // Up to 256 pages of 4KB = 1MB
            freePages.add(i);
        }

        // Initialize TLB entries to -1 (invalid)
        for (int i = 0; i < 2; i++) {
            tlb[0][i] = -1;
            tlb[1][i] = -1;
        }
    }

    // Timer handling
    public static void startTimer(Runnable interruptHandler, long intervalMs) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                interruptHandler.run();
            }
        }, intervalMs, intervalMs);
    }

    public static void stopTimer() {
        timer.cancel();
        timer = new Timer("Hardware-Timer");
    }

    // Simulated read (userland call)
    public static byte readMemory(int virtualAddress) {
        int virtualPage = virtualAddress / PAGE_SIZE;
        int offset = virtualAddress % PAGE_SIZE;

        int physicalPage = lookupTLB(virtualPage);
        if (physicalPage == -1) {
            OS.GetMapping(virtualPage);
            physicalPage = lookupTLB(virtualPage);

            if (physicalPage == -1) {
                System.out.println("Segmentation fault on READ");
                OS.Exit();
                return 0;
            }
        }

        int physicalAddress = physicalPage * PAGE_SIZE + offset;
        return memory[physicalAddress];
    }

    // Simulated write (userland call)
    public static void writeMemory(int virtualAddress, byte value) {
        int virtualPage = virtualAddress / PAGE_SIZE;
        int offset = virtualAddress % PAGE_SIZE;

        int physicalPage = lookupTLB(virtualPage);
        if (physicalPage == -1) {
            OS.GetMapping(virtualPage);
            physicalPage = lookupTLB(virtualPage);

            if (physicalPage == -1) {
                System.out.println("Segmentation fault on WRITE");
                OS.Exit();
                return;
            }
        }

        int physicalAddress = physicalPage * PAGE_SIZE + offset;
        memory[physicalAddress] = value;
    }

    // TLB lookup
    private static int lookupTLB(int virtualPage) {
        for (int i = 0; i < 2; i++) {
            if (tlb[0][i] == virtualPage) {
                return tlb[1][i];
            }
        }
        return -1;
    }

    // TLB update (random replacement)
    public static void updateTLB(int virtualPage, int physicalPage) {
        int index = (int) (Math.random() * 2);
        tlb[0][index] = virtualPage;
        tlb[1][index] = physicalPage;
    }

    // TLB clear on context switch
    public static void clearTLB() {
        for (int i = 0; i < 2; i++) {
            tlb[0][i] = -1;
            tlb[1][i] = -1;
        }
    }

    // Physical page allocation
    public static int allocatePage() {
        if (freePages.isEmpty()) {
            System.out.println("Error: No free pages available.");
            return -1;
        }

        int allocatedPage = freePages.removeFirst();
        System.out.println("Allocated page: " + allocatedPage);
        return allocatedPage;
    }

    // Physical page deallocation
    public static void freePage(int pageNumber) {
        if (!freePages.contains(pageNumber)) {
            freePages.add(pageNumber);
            System.out.println("Freed page: " + pageNumber);
        }
    }
}
