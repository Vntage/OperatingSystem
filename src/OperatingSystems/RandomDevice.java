package OperatingSystems;

import java.util.Random;

public class RandomDevice implements Device {
    private Random[] randomDevices = new Random[10];  // Array to store up to 10 random device instances

    @Override
    public int open(String s) {
        // Try to find an available slot in the array to open a new random device
        for (int i = 0; i < randomDevices.length; i++) {
            if (randomDevices[i] == null) {
                // If no seed is provided, create a Random with a default seed
                randomDevices[i] = (s == null || s.isEmpty()) ? new Random() : new Random(Integer.parseInt(s));
                return i;  // Return the index where the device is opened
            }
        }
        return -1;  // Return -1 if no slot is available
    }

    @Override
    public void close(int id) {
        // Close the device at the given index by setting it to null
        if (id >= 0 && id < randomDevices.length) {
            randomDevices[id] = null;
        }
    }

    @Override
    public byte[] read(int id, int size) {
        // If the device is valid, generate and return random bytes
        if (id >= 0 && id < randomDevices.length && randomDevices[id] != null) {
            byte[] data = new byte[size];
            randomDevices[id].nextBytes(data);
            return data;
        }
        return null;  // Return null if the device is invalid or not initialized
    }

    @Override
    public void seek(int id, int to) {
        if (id >= 0 && id < randomDevices.length && randomDevices[id] != null) {
            randomDevices[id].setSeed(to);  // Reset the seed to "to"
        }
    }

    @Override
    public int write(int id, byte[] data) {
        if (id >= 0 && id < randomDevices.length && randomDevices[id] != null && data.length >= 4) {
            // Convert first 4 bytes of data into an integer seed
            int seed = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
            randomDevices[id].setSeed(seed);
            return 4;  // Indicate 4 bytes were "written"
        }
        return 0;  // If not enough data, do nothing
    }
}
