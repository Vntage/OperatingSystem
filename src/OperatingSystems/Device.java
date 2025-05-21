package OperatingSystems;

public interface Device {
    // Opens a device with the given name and returns an identifier for the device
    int open(String s);

    // Closes the device specified by the given identifier
    void close(int id);

    // Reads data from the device specified by the identifier and returns the data as a byte array
    byte[] read(int id, int size);

    // Seeks to a specific position in the device, moving the read/write pointer to the given position
    void seek(int id, int to);

    // Writes data to the device specified by the identifier and returns the result (e.g., bytes written)
    int write(int id, byte[] data);
}
