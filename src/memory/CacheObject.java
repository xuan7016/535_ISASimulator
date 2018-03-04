package memory;

import java.util.Arrays;

public class CacheObject {

    // 4-way 8KB  last 9 bit as index L1
    // 4 way 128KB  L2
    //1KB / 4B = 256
    //8KB = 2048
    //1MB / 4B(a word) = 262144
    //16MB = 4194304 =  0b10000000000000000000000 22 0's


    private CacheSet cache[];
    private CacheObject higherLevel;
    private CacheObject lowerLevel;
    private int latencyTime;
    private int mask;
    private int maskLength;

    public CacheObject(int size, int latencyTime){
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.higherLevel = null;
        this.lowerLevel = null;
    }

    public CacheObject(int size, int latencyTime, CacheObject lowerLevel){
        this.cache = new CacheSet[size];
        this.latencyTime = latencyTime;
        this.higherLevel = null;
        this.lowerLevel = lowerLevel;
    }

    public void setMask(int mask){
        this.mask = mask;
        this.maskLength = 32 - Integer.numberOfLeadingZeros(mask);
    }

    public RWMemoryObject read(int address){
        int index = address & mask;
        int tag = address >> maskLength;
        CacheSet set = cache[index];
        int[] tags = set.getTags();
        if (Arrays.asList(tags).contains(tag)){
            // hit
            return new RWMemoryObject(set.getData()[Arrays.asList(tags).indexOf(tag)], latencyTime);
        }else{
            // miss
            RWMemoryObject nextLevelRead = lowerLevel.read(address);
            return new RWMemoryObject(nextLevelRead.getWord(), nextLevelRead.getWait()+latencyTime);
        }

    }
}
