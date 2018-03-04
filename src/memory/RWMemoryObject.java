package memory;

public class RWMemoryObject {

    private int word;
    private int wait;

    public RWMemoryObject(int word, int wait){
        this.word = word;
        this.wait = wait;
    }

    public int getWait() {
        return wait;
    }

    public int getWord() {
        return word;
    }
}
