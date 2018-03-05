package memory;

public class MemoryObject {

    //16MB
    //1KB / 4B = 256
    //1MB / 4B(a word) = 262144
    //16MB = 4194304

    private int memory[]; // word addressed
    private int latencyTime = 100;

    //size in words
    public MemoryObject(int size){
        this.memory = new int[size];
    }

    public RWMemoryObject read(int address){
        int word = memory[address];
        return new RWMemoryObject(word, this.latencyTime);
    }

    public int write(int address, RWMemoryObject content){
        memory[address] = content.getWord();
        return content.getWait() + latencyTime;
    }

    public int[] getMemory() {
        return memory;
    }
}
