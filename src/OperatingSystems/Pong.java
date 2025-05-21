package OperatingSystems;

public class Pong extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            KernelMessage msg = OS.WaitForMessage(); // Wait for message
            if (msg != null) {
                System.out.println("Pong received: " + new String(msg.getData()));

                // Create response message
                KernelMessage reply = new KernelMessage();
                reply.setSenderPid(OS.GetPID()); // Set sender as Pong
                reply.setTargetPid(msg.getSenderPid()); // Reply to Ping
                reply.setWhat(msg.getWhat()); // Keep the same message number
                reply.setData(("Pong " + msg.getWhat()).getBytes()); // "Pong i"

                OS.SendMessage(reply); // Send response
                System.out.println("Pong sent: Pong " + msg.getWhat());
            }
        }
    }

    @Override
    public void run() {
        main();
    }
}
