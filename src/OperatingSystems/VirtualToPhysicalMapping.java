package OperatingSystems;

public class VirtualToPhysicalMapping {
    public int physicalPageNumber;
    public int onDiskPageNumber;
    private int virtualPage;
    public boolean valid;
    private static byte[] pageData;

    public VirtualToPhysicalMapping() {
        this.physicalPageNumber = -1;  // Initialize the physical page number
        this.onDiskPageNumber = -1;    // Set onDiskPageNumber to -1 by default (could change if needed)
        this.valid = false;            // Initially, the mapping is not valid
    }

    public VirtualToPhysicalMapping(int pageIndex, int i) {
        this.virtualPage = pageIndex;  // Initialize the virtual page index
        this.physicalPageNumber = i;   // Initialize the physical page number
        this.onDiskPageNumber = -1;    // Set onDiskPageNumber to -1 by default (could change if needed)
        this.valid = false;            // Initially invalid until explicitly mapped
    }

    // Check if the mapping is valid and mapped to physical memory
    public boolean isMapped() {
        return valid && physicalPageNumber != -1; //
    }

    // Map a virtual page to a physical page and mark it valid
    public void mapTo(int virtualPage, int physicalPage) {
        this.virtualPage = virtualPage;
        this.physicalPageNumber = physicalPage;
        this.valid = true;
        pageData = null; // Clear page data when remapping
    }

    // Unmap the page from physical memory
    public void unmap() {
        this.physicalPageNumber = -1;
        this.valid = false;
    }

    // Get the current physical page number
    public int getPhysicalPage() {
        return physicalPageNumber;
    }

    // Get the virtual page number
    public int getVirtualPage() {
        return virtualPage;
    }

    public byte[] getPageData() {
        return pageData;
    }

    public void setPageData(byte[] pageData) {
        VirtualToPhysicalMapping.pageData = pageData;
    }
}
