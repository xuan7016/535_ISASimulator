package memory;

import java.util.Arrays;

//TODO: cached write is wong, if read then write, cache is not updated.

public class CacheObject {
    // 16 word line, 64B per line
    // 4-way set, 256B per set
    // 4-way 16KB L1, 16384/256 last 4 bit as data index(16 word line), second last 6 bit as index(64 sets), rest as tag
    // 4 way 128KB L2, 131072/256 last 4 bit as data index(16 word line), second last 9 (512 sets), rest as tag
    // 16MB = 4194304 = 22 bits


    private CacheSet cache[];
    private CacheObject lowerLevel;
    private MemoryObject memory;
    private int latencyTime;
    private int mask;
    private int maskLength;

    //size in sets
    public CacheObject(int size, int latencyTime){
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.lowerLevel = null;
        this.memory = null;
        this.mask = size-1;
        this.maskLength = 32 - Integer.numberOfLeadingZeros(mask);
        init();
    }

    public CacheObject(int size, int latencyTime, CacheObject lowerLevel){
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.lowerLevel = lowerLevel;
        this.memory = null;
        this.mask = size-1;
        this.maskLength = 32 - Integer.numberOfLeadingZeros(mask);
        init();
    }

    public CacheObject(int size, int latencyTime, MemoryObject memory) {
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.memory = memory;
        this.lowerLevel = null;
        this.mask = size-1;
        this.maskLength = 32 - Integer.numberOfLeadingZeros(mask);
        init();
    }

    private void init(){
        for (int i=0; i<cache.length; i++){
            cache[i] = new CacheSet(4);
        }
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
            int index = Arrays.asList(Arrays.stream(tags).boxed().toArray(Integer[]::new)).indexOf(tag);
            //modify LRU
            int[] lru = set.getLru();
            int accessed_lru = lru[index];
            for (int i=0; i<4; i++){
                if (i==index) {
                    lru[i]=0;
                }else{
                    if (lru[i] <= accessed_lru) lru[i] = lru[i]+1;
                }
            }
            set.setLru(lru);
        }else{
            //miss
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
            //set LRU
            int[] lru = set.getLru();
            for (int i=0; i<4; i++){
                if (i==index){
                    lru[i]=0;
                }else{
                    lru[i] = lru[i] + 1;
                }
            }
            set.setLru(lru);
            // now we have space to put data
            tags[index] = tag;
            set.setTags(tags);
            int[][] setData = set.getData();
            int[] data = set.getData()[index];
            boolean latencySet = false;
            for (int i=0; i<16; i++){
                if (memory != null){
                    RWMemoryObject nextLevelRead = memory.read((address >> 4 << 4) + i);
                    data[i] = nextLevelRead.getWord();
                    if (!latencySet){
                        latency = nextLevelRead.getWait();
                        latencySet = true;
                    }
                }else{
                    RWMemoryObject nextLevelRead = lowerLevel.read((address >> 4 << 4) + i);
                    data[i] = nextLevelRead.getWord();
                    if (!latencySet){
                        latency = nextLevelRead.getWait();
                        latencySet = true;
                    }
                }
            }
            setData[index] = data;
            set.setData(setData);
        }
        Integer[] temp = Arrays.stream(tags).boxed().toArray(Integer[]::new);
        return new RWMemoryObject(set.getData()[Arrays.asList(temp).indexOf(tag)][dataIndex], latencyTime + latency);
    }

    public int write(int address, RWMemoryObject content){
        //write-through, no-allocate
        if (memory != null){
            return memory.write(address, new RWMemoryObject(content.getWord(), content.getWait()+latencyTime));
        }else{
            return lowerLevel.write(address, new RWMemoryObject(content.getWord(), content.getWait()+latencyTime));
        }
    }

    public CacheSet[] getCache() {
        return cache;
    }
}
