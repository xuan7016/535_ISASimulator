package memory;


public class Driver {

    public static void main(String args[]){

        MemoryObject memory = new MemoryObject(4194304);
        CacheObject level2 = new CacheObject(512, 11, memory);
        CacheObject level1 = new CacheObject(64, 4, level2);
        System.out.println(level1.write(16, new RWMemoryObject(65535, 0)));
        System.out.println(level1.write(20, new RWMemoryObject(1234, 0)));
        RWMemoryObject temp = level1.read(16);
        System.out.println(temp.getWait());
        System.out.println(temp.getWord());
        temp = level1.read(20);
        System.out.println(temp.getWait());
        System.out.println(temp.getWord());
    }
}
