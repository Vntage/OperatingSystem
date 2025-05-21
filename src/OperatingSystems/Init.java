package OperatingSystems;

public class Init extends UserlandProcess {
    @Override
    public void main() {
        OS.CreateProcess(new HelloWorld(), OS.PriorityType.realtime);
        OS.CreateProcess(new GoodbyeWorld(), OS.PriorityType.realtime);
        OS.CreateProcess(new Ping(), OS.PriorityType.realtime);
        OS.CreateProcess(new Pong(), OS.PriorityType.realtime);
        OS.CreateProcess(new TestProcess(), OS.PriorityType.interactive); // Testing Virtual Memory
        OS.CreateProcess(new VFSTest(), OS.PriorityType.interactive); // Testing VFS
        OS.CreateProcess(new RandomProcessTest(), OS.PriorityType.interactive); // Testing RandomProcess

        // Keep Init alive to prevent early termination
        while (true) {
            cooperate();
        }
    }

    @Override
    public void run() {
        main();
    }
}
