package memory;

public class CacheSet {

    //line size 16 * 4B = 64B
    //set size 4 * 64 = 256B
    private int[] tags;
    private int[][] data;
    private boolean[] dirty;
    private int[] lru;

    public CacheSet(int ways){
        this.tags = new int[ways];
        this.data = new int[ways][];
        this.dirty = new boolean[ways];
        this.lru = new int[ways];
        init();
    }

    private void init(){
        for (int i=0; i < tags.length; i++){
            this.tags[i] = -1;
            this.data[i] = new int[16];
            this.dirty[i] = false;
            this.lru[i] = 0;
        }
    }

    public int[][] getData() {
        return data;
    }

    public int[] getTags() {
        return tags;
    }

    public void setData(int[][] data) {
        this.data = data;
    }

    public void setTags(int[] tags) {
        this.tags = tags;
    }

    public int[] getLru() {
        return lru;
    }

    public void setLru(int[] lru) {
        this.lru = lru;
    }
}
