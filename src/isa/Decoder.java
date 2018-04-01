package isa;

import memory.CacheObject;
import memory.RWMemoryObject;

public class Decoder {

    private Registers registers;
    private CacheObject memory;

    public Decoder(Registers registers, CacheObject memory){
        this.registers = registers;
        this.memory = memory;
    }

    public InstructionResult decode(int instruction){
        int firstThree = (int)(instruction >>> 29);
        System.out.println(firstThree);
        switch(firstThree){
            case 1:
                // ALU operations
                return ALU_decode(instruction);
            case 2:
                // branch operations
                return branch_decode(instruction);
            case 3:
                // comparisions
                return cmp_decode(instruction);
            case 4:
                // memory operations
                return mem_decode(instruction);
            default:
                System.out.println("unsupported operation or not an operation");
                return null;
        }
    }

    //index start from 0
    private static int getIntInRange(int bits, int start, int length){
        int mask = (1 << length) -1;
        int shiftAmount = 32 - (start + length);
        return (bits >>> shiftAmount) & mask;

    }

    private InstructionResult ALU_decode(int instruction){
        int operation = (instruction >>> 24) & 0b00011111;
        int rd = 0;
        int rm = 0;
        int rn = 0;
        int result = 0;
        switch(operation){
            case 0:
                //MOV
                rd = getIntInRange(instruction, 8, 5);
                result  = getIntInRange(instruction, 13, 14) << getIntInRange(instruction, 27, 5);
                return new InstructionResult(rd, result);
            case 1:
                //ADD
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                if (getIntInRange(instruction, 18, 1) == 1){
                    result = registers.getContent(rm) + getIntInRange(instruction, 19, 13);
                    return new InstructionResult(rd, result);
                }else{
                    result = registers.getContent(rm) + registers.getContent(getIntInRange(instruction, 19,5));
                    return new InstructionResult(rd, result);
                }
            case 2:
                //SUB
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                if (getIntInRange(instruction, 18, 1) == 1){
                    result = registers.getContent(rm) - getIntInRange(instruction, 19, 13);
                    return new InstructionResult(rd, result);
                }else{
                    result = registers.getContent(rm) - registers.getContent(getIntInRange(instruction, 19,5));
                    return new InstructionResult(rd, result);
                }
            case 3:
                //MUL
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                if (getIntInRange(instruction, 18, 1) == 1){
                    result = registers.getContent(rm) * getIntInRange(instruction, 19, 13);
                    return new InstructionResult(rd, result);
                }else{
                    result = registers.getContent(rm) * registers.getContent(getIntInRange(instruction, 19,5));
                    return new InstructionResult(rd, result);
                }
            case 4:
                //DIV
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                if (getIntInRange(instruction, 18, 1) == 1){
                    result = registers.getContent(rm) / getIntInRange(instruction, 19, 13);
                    return new InstructionResult(rd, result);
                }else{
                    result = registers.getContent(rm) / registers.getContent(getIntInRange(instruction, 19,5));
                    return new InstructionResult(rd, result);
                }
            case 5:
                //MOD
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                if (getIntInRange(instruction, 18, 1) == 1){
                    result = registers.getContent(rm) % getIntInRange(instruction, 19, 13);
                    return new InstructionResult(rd, result);
                }else{
                    result = registers.getContent(rm) % registers.getContent(getIntInRange(instruction, 19,5));
                    return new InstructionResult(rd, result);
                }
            case 6:
                //REM
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                if (getIntInRange(instruction, 18, 1) == 1){
                    result = registers.getContent(rm) % getIntInRange(instruction, 19, 13);
                    return new InstructionResult(rd, result);
                }else{
                    result = registers.getContent(rm) % registers.getContent(getIntInRange(instruction, 19,5));
                    return new InstructionResult(rd, result);
                }
            case 7:
                //SHF types: 00-logical, 01-arithmetic 10-rotate
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                int i = getIntInRange(instruction, 25,1);
                switch(getIntInRange(instruction, 23, 2)){
                    case 0:
                        if (i == 1) {
                            result = registers.getContent(rm) >>> getIntInRange(instruction, 26,6);
                        }else{
                            result = registers.getContent(rm) << getIntInRange(instruction, 26, 6);
                        }
                        break;
                    case 1:
                        if (i == 1) {
                            result = registers.getContent(rm) >> getIntInRange(instruction, 26,6);
                        }else{
                            result = registers.getContent(rm) << getIntInRange(instruction, 26, 6);
                        }
                        break;
                    case 2:
                        if (i == 1){
                            result = Integer.rotateRight(rm, getIntInRange(instruction, 26, 6));
                        }else{
                            result = Integer.rotateLeft(rm, getIntInRange(instruction, 26,6));
                        }
                        break;
                }
                return new InstructionResult(rd, result);
            case 8:
                //AND
                rd = getIntInRange(instruction, 8, 5);
                rm = registers.getContent(getIntInRange(instruction, 13, 5));
                if (getIntInRange(instruction,18,1)==1){
                    rn = getIntInRange(instruction, 19, 13);
                }else{
                    rn = registers.getContent(getIntInRange(instruction, 19,5));
                }
                return new InstructionResult(rd, rm & rn);
            case 9:
                //OR
                rd = getIntInRange(instruction, 8, 5);
                rm = registers.getContent(getIntInRange(instruction, 13, 5));
                if (getIntInRange(instruction,18,1)==1){
                    rn = getIntInRange(instruction, 19, 13);
                }else{
                    rn = registers.getContent(getIntInRange(instruction, 19,5));
                }
                return new InstructionResult(rd, rm | rn);
            case 10:
                //XOR
                rd = getIntInRange(instruction, 8, 5);
                rm = registers.getContent(getIntInRange(instruction, 13, 5));
                if (getIntInRange(instruction,18,1)==1){
                    rn = getIntInRange(instruction, 19, 13);
                }else{
                    rn = registers.getContent(getIntInRange(instruction, 19,5));
                }
                return new InstructionResult(rd, rm ^ rn);
            case 11:
                //NOT
                rd = getIntInRange(instruction,8,5);
                rm = registers.getContent(getIntInRange(instruction,13,5));
                return new InstructionResult(rd, ~rm);
            default:
                System.out.println("alu operation not recognized");
                return null;
        }
    }

    private InstructionResult branch_decode(int instruction){
        int operation = (instruction >>> 24) & 0b00011111;
        int rd = 0;
        int rn = 0;
        switch(operation){
            case 1:
                //JMP
                if (getIntInRange(instruction, 16,1)==1){
                    registers.setPC(getIntInRange(instruction,17,15));
                }else{
                    registers.setPC(registers.getContent(getIntInRange(instruction, 8,5)));
                }
                return null;
            case 2:
                //JCD
                rd = getIntInRange(instruction, 8,5);
                rn = getIntInRange(instruction, 13,5);
                if (registers.getContent(rn)==1) {
                    if (getIntInRange(instruction, 18, 1) == 1) {
                        registers.setPC(getIntInRange(instruction, 19, 13));
                    } else {
                        registers.setPC(registers.getContent(rd));
                    }
                }
                return null;
            case 3:
                // JSR
                rd = getIntInRange(instruction, 8, 5);
                registers.setRet(registers.getPC());
                if (getIntInRange(instruction, 15, 1)==1){
                    registers.setPC(getIntInRange(instruction, 16,16));
                }else{
                    registers.setPC(registers.getContent(rd));
                }
            case 4:
                //RET
                registers.setPC(registers.getRet());
            default:
                System.out.println("branch operation not recognized");

        }
        return null;
    }

    private InstructionResult cmp_decode(int instruction){
        int operation = (instruction >>> 24) & 0b00011111;
        int rd = getIntInRange(instruction, 8, 5);
        int rm = getIntInRange(instruction, 13, 5);
        int rn = getIntInRange(instruction, 19, 5);
        int i = getIntInRange(instruction, 18, 1);
        switch(operation){
            case 1:
                //EQ
                if (i == 1){
                    if (getIntInRange(instruction, 19, 13) == registers.getContent(rm)){
                        return new InstructionResult(rd, 1);
                    }else{
                        return new InstructionResult(rd, 0);
                    }
                }else{
                    if (registers.getContent(rm) == registers.getContent(rn)){
                        return new InstructionResult(rd, 1);
                    }else{
                        return new InstructionResult(rd, 0);
                    }
                }
            case 2:
                // LS
                if (i == 1){
                    if (getIntInRange(instruction, 19, 13) > registers.getContent(rm)){
                        return new InstructionResult(rd, 1);
                    }else{
                        return new InstructionResult(rd, 0);
                    }
                }else{
                    if (registers.getContent(rm) < registers.getContent(rn)){
                        return new InstructionResult(rd, 1);
                    }else{
                        return new InstructionResult(rd, 0);
                    }
                }
            case 3:
                if (i == 1){
                    if (getIntInRange(instruction, 19, 13) < registers.getContent(rm)){
                        return new InstructionResult(rd, 1);
                    }else{
                        return new InstructionResult(rd, 0);
                    }
                }else{
                    if (registers.getContent(rm) > registers.getContent(rn)){
                        return new InstructionResult(rd, 1);
                    }else{
                        return new InstructionResult(rd, 0);
                    }
                }
            default:
                System.out.println("unsupported comparision operation");
                return null;
        }
    }

    private InstructionResult mem_decode(int instruction){
        int operation = (instruction >>> 26) & 0b000111;
        int rd = 0;
        int rn = 0;
        int result = 0;
        switch(operation){
            case 1:
                //LOD
                rd = getIntInRange(instruction, 7, 5);
                rn = getIntInRange(instruction, 12, 5);
                if (getIntInRange(instruction, 6,1)==1){
                    result = registers.getContent(rn) + getIntInRange(instruction, 24,8);
                }else{
                    result = registers.getContent(rn);
                }
                return new InstructionResult(rd, memory.read(result).getWord());
            case 2:
                //STR
                System.out.println("in store");
                rd = getIntInRange(instruction, 7, 5);
                rn = getIntInRange(instruction, 12, 5);
                if (getIntInRange(instruction, 6,1)==1){
                    result = registers.getContent(rn) + getIntInRange(instruction, 24,8);
                }else{
                    result = registers.getContent(rn);
                }
                memory.write(registers.getContent(rd), new RWMemoryObject(result,0));
                return null;
            default:
                System.out.println("memory operation not recognized");
                return null;
        }
    }

    public static void main(String[] args){
        System.out.println(getIntInRange(0b1011110001, 2, 1));
        System.out.println(0b011110);
    }
}
