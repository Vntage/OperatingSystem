package OperatingSystems;

public class Ping extends UserlandProcess {

    @Override
    public void main() {
        int pongPid = OS.GetPidByName("Pong"); // Get Pong's PID
        if (pongPid == -1) {
            System.out.println("Ping: Could not find Pong process!");
            return;
        }

        for (int i = 0; i < 5; i++) { // Send 5 messages
            KernelMessage msg = new KernelMessage();
            msg.setSenderPid(OS.GetPID()); // Set sender as Ping
            msg.setTargetPid(pongPid); // Send to Pong
            msg.setWhat(i); // Message number
            msg.setData(("Ping " + i).getBytes()); // Convert "Ping i" to bytes

            OS.SendMessage(msg); // Send message
            System.out.println("Ping sent: Ping " + i);

            KernelMessage reply = OS.WaitForMessage(); // Wait for Pong’s response
            if (reply != null) {
                System.out.println("Ping received: " + new String(reply.getData()));
            }

            OS.Sleep(1000); // Simulate delay
        }

        System.out.println("Ping: Finished.");
        OS.Exit();
    }

    @Override
    public void run() {
        main();
    }
}
