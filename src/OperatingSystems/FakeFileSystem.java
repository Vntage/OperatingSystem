package OperatingSystems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device {
    private RandomAccessFile[] files = new RandomAccessFile[10];  // Array to hold up to 10 file instances

    @Override
    public int open(String s) {
        // Ensure the filename is not null or empty
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty or null");
        }
        try {
            // Try to find an available slot in the array to open the file
            for (int i = 0; i < files.length; i++) {
                if (files[i] == null) {
                    files[i] = new RandomAccessFile(s, "rw");  // Open file in read-write mode
                    return i;  // Return the index where the file was opened
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  // Handle exception if file is not found
        }
        return -1;  // Return -1 if no slot is available
    }

    @Override
    public void close(int id) {
        // Try to close the file at the given index
        try {
            if (id >= 0 && id < files.length && files[id] != null) {
                files[id].close();  // Close the file
                files[id] = null;   // Set the slot to null to indicate the file is closed
            }
        } catch (IOException e) {
            e.printStackTrace();  // Handle IO exception during file close
        }
    }

    @Override
    public byte[] read(int id, int size) {
        // If the file at the given index is valid, read data from it
        if (id >= 0 && id < files.length && files[id] != null) {
            byte[] data = new byte[size];
            try {
                files[id].readFully(data);  // Read the specified number of bytes
                return data;
            } catch (IOException e) {
                e.printStackTrace();  // Handle exception during read
            }
        }
        return null;  // Return null if the file is invalid or an error occurred
    }

    @Override
    public void seek(int id, int to) {
        // Try to seek to the specified position in the file
        try {
            if (id >= 0 && id < files.length && files[id] != null) {
                files[id].seek(to);  // Move the file pointer to the specified position
            }
        } catch (IOException e) {
            e.printStackTrace();  // Handle exception during seek
        }
    }

    @Override
    public int write(int id, byte[] data) {
        // If the file is valid, write data to it
        try {
            if (id >= 0 && id < files.length && files[id] != null) {
                files[id].write(data);  // Write the provided data to the file
                return data.length;  // Return the number of bytes written
            }
        } catch (IOException e) {
            e.printStackTrace();  // Handle exception during write
        }
        return 0;  // Return 0 if the write operation failed
    }
}
