package memory;

import java.lang.reflect.Array;
import java.util.Arrays;

public class CacheObject {

    // 4-way 8KB last 4 bit as data index,  second last 7 bit as index L1, rest as tag
    // 4 way 128KB  L2
    //1KB / 4B = 256
    //8KB = 2048 L1
    //1MB / 4B(a word) = 262144
    //16MB = 4194304 =  22 bits


    private CacheSet cache[];
    private CacheObject higherLevel;
    private CacheObject lowerLevel;
    private MemoryObject memory;
    private int latencyTime;
    private int mask;
    private int maskLength;

    public CacheObject(int size, int latencyTime){
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.higherLevel = null;
        this.lowerLevel = null;
        this.memory = null;
        initCache();
    }

    public CacheObject(int size, int latencyTime, CacheObject lowerLevel){
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.higherLevel = null;
        this.lowerLevel = lowerLevel;
        this.memory = null;
        initCache();
    }

    public CacheObject(int size, int latencyTime, MemoryObject memory) {
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.memory = memory;
        this.higherLevel = null;
        this.lowerLevel = null;
        initCache();
    }

    private void initCache(){
        for (int i=0; i<cache.length; i++){
            cache[i] = new CacheSet(4);
        }
    }

    public void setMask(int mask){
        this.mask = mask;
        this.maskLength = 32 - Integer.numberOfLeadingZeros(mask);
    }

    public RWMemoryObject read(int address){
        int dataIndex = address & 0b1111;
        int setIndex = (address >> 4) & mask;
        int tag = (address >> (4+maskLength));
        CacheSet set = cache[setIndex];
        int[] tags = set.getTags();
        int latency = 0;
        if (Arrays.asList(Arrays.stream(tags).boxed().toArray(Integer[]::new)).contains(tag)){
            //hit
            System.out.println("hit");
            int index = Arrays.asList(Arrays.stream(tags).boxed().toArray(Integer[]::new)).indexOf(tag);
            //modify LRU
            int[] lru = set.getLru();
            int accessed_lru = lru[index];
            for (int i=0; i<4; i++){
                if (i==index) {
                    lru[i]=0;
                }else{
                    if (lru[i] < accessed_lru) lru[i] = lru[i]+1;
                }
            }
            set.setLru(lru);
        }else{
            //miss
            System.out.println("miss");
            int index = -1;
            // check for empty space
            for (int i=0; i<4; i++){
                if (tags[i]==-1){
                    index = i;
                    break;
                }
            }
            //if no empty space evict largest LRU
            if (index == -1) {
                // find largest LRU index
                int[] lru = set.getLru();
                int largest_lru_index = -1;
                int largest_lru = -1;
                for (int i = 0; i < 4; i++) {
                    if (lru[i] > largest_lru) {
                        largest_lru = lru[i];
                        largest_lru_index = i;
                    }
                }
                index = largest_lru_index;
            }
            // now we have space to put data
            tags[index] = tag;
            set.setTags(tags);
            int[][] setData = set.getData();
            int[] data = set.getData()[index];
            for (int i=0; i<16; i++){
                if (memory != null){
                    RWMemoryObject nextLevelRead = memory.read((address >> 4 << 4) + i);
                    data[i] = nextLevelRead.getWord();
                    latency = nextLevelRead.getWait();
                }
            }
            setData[index] = data;
            set.setData(setData);
        }
        Integer[] temp = Arrays.stream(tags).boxed().toArray(Integer[]::new);
        return new RWMemoryObject(set.getData()[Arrays.asList(temp).indexOf(tag)][dataIndex], latencyTime + latency);
    }
}
