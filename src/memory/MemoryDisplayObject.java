package memory;

public class MemoryDisplayObject {

    private int address;
    private String content;

    public MemoryDisplayObject(int address, String content){
        this.address = address;
        this.content = content;
    }

    public int getAddress() {
        return address;
    }

    public String getContent() {
        return content;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
