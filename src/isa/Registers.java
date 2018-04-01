package isa;

public class Registers {

    private int[] registers;
    private int PC;
    private int ret;

    public Registers(int size){
        this.registers = new int[size];
    }

    public int getContent(int index){
        return this.registers[index];
    }

    public void setContent(int index, int content){
        this.registers[index] = content;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int newPC){
        this.PC = newPC;
    }

    public int getLength(){
        return registers.length;
    }

    public void setRet(int ret){
        this.ret = ret;
    }

    public int getRet(){
        return ret;
    }
}
