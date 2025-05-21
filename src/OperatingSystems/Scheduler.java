package OperatingSystems;

import java.time.Instant;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Queue;
import java.util.Random;
import java.util.HashMap;
import java.util.HashSet;

public class Scheduler {
    public static HashMap<Integer, PCB> pcbTable = new HashMap<>();
    public static HashSet<Integer> waitingForMessages = new HashSet<>();
    // Queues for different priority types of processes
    private static Queue<PCB> realtimeQueue = new LinkedList<>();
    private static Queue<PCB> interactiveQueue = new LinkedList<>();
    private static Queue<PCB> backgroundQueue = new LinkedList<>();

    // List to hold all processes (PCBs)
    public static LinkedList<PCB> processList = new LinkedList<>();;

    // The process that is currently running
    public static PCB currentlyRunning;

    private static int nextPid = 1;

    // Constructor sets up a timer task to periodically check for sleeping processes
    public Scheduler() {
        processList = new LinkedList<>();

        // Set up a timer to wake up sleeping processes every 250ms
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Scheduler.checkSleepingProcesses(); // Wake up processes if needed
                if (currentlyRunning != null) {
                    currentlyRunning.requestStop(); // Request stop for the current running process
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 250); // Schedule to run every 250ms
    }

    // Priority queue to manage sleeping processes based on their wake time
    private static PriorityQueue<SleepingProcess> sleepingQueue = new PriorityQueue<>(
            (p1, p2) -> Long.compare(p1.wakeTime, p2.wakeTime) // Compare wake times for priority
    );

    // Method to put the currently running process to sleep
    public static void Sleep(int milliseconds) {
        if (currentlyRunning != null) {
            // Calculate the time when the process should wake up
            long wakeTime = Instant.now().toEpochMilli() + milliseconds;
            // Add the process to the sleeping queue
            sleepingQueue.add(new SleepingProcess(currentlyRunning, wakeTime));
            System.out.println("Process " + currentlyRunning.getPid() + " is sleeping until " + wakeTime);
            // Switch to another process
            SwitchProcess();
        }
    }

    // Check and wake up any processes whose sleep time has expired
    public static void checkSleepingProcesses() {
        long currentTime = Instant.now().toEpochMilli();
        // Wake up processes whose wake time has passed
        while (!sleepingQueue.isEmpty() && sleepingQueue.peek().wakeTime <= currentTime) {
            PCB wokenProcess = sleepingQueue.poll().process;
            processList.add(wokenProcess);
            System.out.println("Process " + wokenProcess.getPid() + " is waking up.");
        }
    }

    // Method to add a process to the scheduler
    public static void addProcess(PCB pcb) {
        // Add the process to the ready queue (processList)
        processList.add(pcb);
    }

    // Inner class to represent a sleeping process, holds the process and its wake time
    private static class SleepingProcess {
        PCB process; // The process that is sleeping
        long wakeTime; // The time when the process should wake up
        SleepingProcess(PCB process, long wakeTime) {
            this.process = process;
            this.wakeTime = wakeTime;
        }
    }

    // Randomly choose one of the process queues (realtime, interactive, or background)
    private static Queue<PCB> getRandomQueue() {
        Random rand = new Random();
        int roll = rand.nextInt(100);

        // 50% chance for realtime processes, 30% for interactive, and 20% for background
        if (roll < 50 && !realtimeQueue.isEmpty()) return realtimeQueue;
        if (roll < 80 && !interactiveQueue.isEmpty()) return interactiveQueue;
        return (!backgroundQueue.isEmpty()) ? backgroundQueue : null;
    }

    // Remove a process from the process list and check if it is the currently running process
    public static void removeProcess(PCB process) {
        processList.remove(process);
        if (currentlyRunning == process) {
            currentlyRunning = null;
        }
    }

    // Create a new process and add it to the scheduler's process list
    public void CreateProcess(UserlandProcess up, OS.PriorityType priority) {
        PCB newPCB = new PCB(up, priority);
        processList.add(newPCB);
        // If no process is running, start the newly created process
        if (currentlyRunning == null || currentlyRunning.isDone()) {
            SwitchProcess();
        }
    }

    // Switch to the next process in the appropriate queue
    public static void SwitchProcess() {
        checkSleepingProcesses(); // Wake up any sleeping processes first

        // Get a random queue based on priority
        Queue<PCB> queue = getRandomQueue();
        if (queue == null) {
            currentlyRunning = null;
            return;
        }

        // Select the next process from the chosen queue
        PCB nextProcess = queue.poll();
        currentlyRunning = nextProcess;
        assert nextProcess != null;
        nextProcess.start(); // Start the next process
    }

    // Return the currently running process
    public PCB getCurrentlyRunning() {
        return currentlyRunning;
    }

    public static PCB getRandomProcess() {
        Random rand = new Random();
        while (!processList.isEmpty()) {
            PCB randomPCB = processList.get(rand.nextInt(processList.size()));
            VirtualToPhysicalMapping[] mappings = randomPCB.getPageTable();
            for (int i = 0; i < mappings.length; i++) {
                if (mappings[i] != null && mappings[i].physicalPageNumber != -1) {
                    return randomPCB;
                }
            }
        }
        return null; // Should not happen if there are enough processes
    }

}

