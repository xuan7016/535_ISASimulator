package memory;

public class CacheSet {

    private int[] tags;
    private int[] data;

    public CacheSet(int[] ways, int[] data){
        this.tags = tags;
        this.data = data;
    }

    public int[] getData() {
        return data;
    }

    public int[] getTags() {
        return tags;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public void setTags(int[] tags) {
        this.tags = tags;
    }

}
