package OperatingSystems;

public class KernelMessage {
    private int senderPid;
    private int targetPid;
    private int what;
    private byte[] data;

    // Default constructor
    public KernelMessage() {
    }

    // Copy constructor for message isolation
    public KernelMessage(KernelMessage original) {
        this.senderPid = original.senderPid;
        this.targetPid = original.targetPid;
        this.what = original.what;
        this.data = (original.data != null) ? original.data.clone() : null;
    }

    public void setSenderPid(int senderPid) {
        this.senderPid = senderPid;
    }

    public void setTargetPid(int targetPid) {
        this.targetPid = targetPid;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public void setData(byte[] data) {
        this.data = (data != null) ? data.clone() : null;
    }

    public int getSenderPid() {
        return senderPid;
    }

    public int getTargetPid() {
        return targetPid;
    }

    public int getWhat() {
        return what;
    }

    public byte[] getData() {
        return (data != null) ? data.clone() : null;
    }
}
