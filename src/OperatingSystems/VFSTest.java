package OperatingSystems;

public class VFSTest extends UserlandProcess{
    public void main() {
        VFS vfs = new VFS();  // Initialize VFS

        byte[] pageData = new byte[1024];  // (1024 bytes)
        for (int i = 0; i < pageData.length; i++) {
            pageData[i] = (byte) i;
        }

        int pageNumber = vfs.writeToSwap(pageData);  // Write the page to swap
        System.out.println("Page written with page number: " + pageNumber);

        // Reading back the page from the swap file
        byte[] readData = vfs.readFromSwap(pageNumber);
        System.out.println("Read data: " + new String(readData));
    }

    @Override
    public void run() {
        main();
    }
}
