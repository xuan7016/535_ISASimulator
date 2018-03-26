package isa;

public class InstructionResult{

    private int destination;
    private int result;

    public InstructionResult(int destination, int result){
        this.destination = destination;
        this.result = result;
    }

    public int getDestination() {
        return destination;
    }

    public int getResult() {
        return result;
    }
}
