package OperatingSystems;

import java.util.HashMap;
import java.util.Map;

public class VFS implements Device {
    private final Map<Integer, Device> devices = new HashMap<>();
    private final Map<Integer, Integer> deviceIds = new HashMap<>();
    private int nextId = 0;

    private final RandomDevice randomDevice = new RandomDevice(); // Singleton RandomDevice
    private final FakeFileSystem fakeFileSystem = new FakeFileSystem(); // Singleton FFS

    private int swapFileId = -1; // VFS ID for swap file
    private int nextSwapPage = 0; // Tracks next page to write (page = 1024 bytes)

    public VFS() {
        initializeSwapFile("swapfile.ffs");  // Initialize the swap file during system startup
    }

    public void initializeSwapFile(String filename) {
        int ffsId = fakeFileSystem.open(filename);
        if (ffsId == -1) {
            throw new RuntimeException("Failed to open swap file: " + filename);
        }
        swapFileId = nextId++;
        devices.put(swapFileId, fakeFileSystem);
        deviceIds.put(swapFileId, ffsId);
    }

    public int writeToSwap(byte[] pageData) {
        if (pageData.length != 1024) {
            throw new IllegalArgumentException("Page size must be exactly 1024 bytes");
        }
        int offset = nextSwapPage * 1024;
        seek(swapFileId, offset);
        write(swapFileId, pageData);
        return nextSwapPage++;
    }

    public byte[] readFromSwap(int pageNumber) {
        int offset = pageNumber * 1024;
        seek(swapFileId, offset);
        return read(swapFileId, 1024);
    }

    public int open(String s) {
        String[] parts = s.split(" ", 2);
        String deviceType = parts[0];
        String params = parts.length > 1 ? parts[1] : "";

        Device device;
        int deviceId;

        switch (deviceType) {
            case "random" -> {
                device = randomDevice;
                deviceId = randomDevice.open(params);
            }
            case "file" -> {
                device = fakeFileSystem;
                deviceId = device.open(params);
            }
            default -> throw new IllegalArgumentException("Unknown device type");
        }

        if (deviceId == -1) return -1;

        int vfsId = nextId++;
        devices.put(vfsId, device);
        deviceIds.put(vfsId, deviceId);
        return vfsId;
    }

    @Override
    public void close(int id) {
        if (devices.containsKey(id)) {
            devices.get(id).close(deviceIds.get(id));
            devices.remove(id);
            deviceIds.remove(id);
        }
    }

    @Override
    public byte[] read(int id, int size) {
        return devices.containsKey(id) ? devices.get(id).read(deviceIds.get(id), size) : null;
    }

    @Override
    public void seek(int id, int to) {
        if (devices.containsKey(id)) {
            devices.get(id).seek(deviceIds.get(id), to);
        }
    }

    @Override
    public int write(int id, byte[] data) {
        return devices.containsKey(id) ? devices.get(id).write(deviceIds.get(id), data) : 0;
    }
}
