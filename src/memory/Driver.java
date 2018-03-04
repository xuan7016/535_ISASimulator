package memory;

import java.util.Arrays;
import java.util.Collections;

public class Driver {

    public static void main(String args[]){

        MemoryObject memory = new MemoryObject(4194304);
        CacheObject level1 = new CacheObject(2048, 4, memory);
        level1.setMask(0b1111111);
        memory.write(16, new RWMemoryObject(65535, 0));
        memory.write(20, new RWMemoryObject(1234, 0));
        RWMemoryObject temp = level1.read(16);
        System.out.println(temp.getWait());
        System.out.println(temp.getWord());
        temp = level1.read(20);
        System.out.println(temp.getWait());
        System.out.println(temp.getWord());
    }
}
