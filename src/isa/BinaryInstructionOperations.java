package isa;


public class BinaryInstructionOperations {

    public BinaryInstructionOperations(){}

    public static String decode(int instruction){
        int firstThree = (int)(instruction >>> 29);
        switch(firstThree){
            case 1:
                // ALU operations
                return ALU_decode(instruction);
            case 2:
                // branch operations
                return branch_decode(instruction);
            case 3:
                // comparisons
                return cmp_decode(instruction);
            case 4:
                // memory operations
                return mem_decode(instruction);
            case 0b111:
                return "HLT";
            default:
                //System.out.println("unsupported operation or not an operation");
                return Integer.toString(instruction);
        }
    }

    //index start from 0
    private static int getIntInRange(int bits, int start, int length){
        int mask = (1 << length) -1;
        int shiftAmount = 32 - (start + length);
        return (bits >>> shiftAmount) & mask;

    }

    private static String cmp_decode(int instruction){
        int operation = (instruction >>> 24) & 0b00011111;
        int rd = getIntInRange(instruction, 8, 5);
        int rm = getIntInRange(instruction, 13, 5);
        int rn = getIntInRange(instruction, 19, 5);
        int i = getIntInRange(instruction, 18, 1);
        int imm = getIntInRange(instruction, 19, 13);
        switch(operation){
            case 1:
                // EQ
                if (i==1){
                    return "EQ R" + rd + " R" + rm + " " + imm;
                }else{
                    return "EQ R" + rd + " R" + rm + " R" + rn;
                }
            case 2:
                // LS
                if (i==1){
                    return "LS R" + rd + " R" + rm + " " + imm;
                }else{
                    return "LS R" + rd + " R" + rm + " R" + rn;
                }
            case 3:
                //GT
                if (i==1){
                    return "GT R" + rd + " R" + rm + " " + imm;
                }else{
                    return "GT R" + rd + " R" + rm + " R" + rn;
                }
            default:
                System.out.println("unknown cmp operation");
                return null;
        }
    }

    private static String ALU_decode(int instruction){
        int operation = (instruction >>> 24) & 0b00011111;
        int rd = 0;
        int rm = 0;
        int rn = 0;
        int imm=0;
        switch(operation){
            case 0:
                //MOV
                rd = getIntInRange(instruction, 8, 5);
                imm = getIntInRange(instruction, 13,14);
                if (getIntInRange(instruction, 27,5)==0){
                    return "MOV R" + rd + " " + imm;
                }else{
                    return "MOV R" + rd + " " + imm + " " + getIntInRange(instruction,27,5);
                }
            case 1:
                //ADD
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "ADD R" + rd + " R" + rm + " " + imm;
                }else{
                    return "ADD R" + rd + " R" + rm + " R" + rn;
                }
            case 2:
                //SUB
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "SUB R" + rd + " R" + rm + " " + imm;
                }else{
                    return "SUB R" + rd + " R" + rm + " R" + rn;
                }
            case 3:
                //MUL
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "MUL R" + rd + " R" + rm + " " + imm;
                }else{
                    return "MUL R" + rd + " R" + rm + " R" + rn;
                }
            case 4:
                //DIV
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "DIV R" + rd + " R" + rm + " " + imm;
                }else{
                    return "DIV R" + rd + " R" + rm + " R" + rn;
                }
            case 5:
                //MOD
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "MOD R" + rd + " R" + rm + " " + imm;
                }else{
                    return "MOD R" + rd + " R" + rm + " R" + rn;
                }
            case 6:
                //REM
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "REM R" + rd + " R" + rm + " " + imm;
                }else{
                    return "REM R" + rd + " R" + rm + " R" + rn;
                }
            case 7:
                //SHF types: 00-logical, 01-arithmetic 10-rotate
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                int type = getIntInRange(instruction, 23,2);
                int i = getIntInRange(instruction, 25,1);
                imm = getIntInRange(instruction,26,6);
                return "SHF R" + rd + " R" + rm + " " + type + " " + i + " " + imm;
            case 8:
                //AND
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "AND R" + rd + " R" + rm + " " + imm;
                }else{
                    return "AND R" + rd + " R" + rm + " R" + rn;
                }
            case 9:
                //OR
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "OR R" + rd + " R" + rm + " " + imm;
                }else{
                    return "OR R" + rd + " R" + rm + " R" + rn;
                }
            case 10:
                //XOR
                rd = getIntInRange(instruction, 8, 5);
                rm = getIntInRange(instruction, 13, 5);
                rn = getIntInRange(instruction, 19,5);
                imm = getIntInRange(instruction, 19, 13);
                if (getIntInRange(instruction, 18, 1) == 1){
                    return "XOR R" + rd + " R" + rm + " " + imm;
                }else{
                    return "XOR R" + rd + " R" + rm + " R" + rn;
                }
            case 11:
                //NOT
                rd = getIntInRange(instruction,8,5);
                rm = getIntInRange(instruction,13,5);
                return "NOT R" + rd + " R" + rm;
            default:
                //System.out.println("alu operation not recognized");
                return null;
        }
    }

    private static String branch_decode(int instruction){
        int operation = (instruction >>> 24) & 0b00011111;
        int rd = 0;
        int rn = 0;
        int i = 0;
        int imm = 0;
        switch(operation){
            case 1:
                //JMP
                rd = getIntInRange(instruction, 8, 5);
                i = getIntInRange(instruction, 16,1);
                imm = getIntInRange(instruction, 17, 15);
                if (i==1){
                    return "JMP R" + rd + " " + imm;
                }else {
                    return "JMP R" + rd;
                }
            case 2:
                //JCD
                rd = getIntInRange(instruction, 8,5);
                rn = getIntInRange(instruction, 13,5);
                i = getIntInRange(instruction, 18,1);
                imm = getIntInRange(instruction, 19,13);
                if (i==1){
                    return "JCD R" + rd + " R" + rn + " " + imm;
                }else{
                    return "JCD R" + rd + " R" + rn;
                }
            case 3:
                //JSR TODO
            case 4:
                //RET TODO
            default:
                System.out.println("branch operation not recognized");

        }
        return null;
    }

    private static String mem_decode(int instruction){
        int operation = (instruction >>> 26) & 0b000111;
        int rd = 0;
        int rn = 0;
        int imm = 0;
        int i=0;
        int result = 0;
        switch(operation){
            case 1:
                //LOD
                rd = getIntInRange(instruction, 7, 5);
                rn = getIntInRange(instruction, 12, 5);
                imm = getIntInRange(instruction, 24,8);
                i = getIntInRange(instruction, 6,1);
                if (i==1){
                    return "LOD R" + rd + " R" + rn + " " + imm;
                }else{
                    return "LOD R" + rd + " R" + rn;
                }
            case 2:
                //STR
                rd = getIntInRange(instruction, 7, 5);
                rn = getIntInRange(instruction, 12, 5);
                imm = getIntInRange(instruction, 24,8);
                i = getIntInRange(instruction, 6,1);
                if (i==1){
                    return "STR R" + rd + " R" + rn + " " + imm;
                }else{
                    return "STR R" + rd + " R" + rn;
                }
            default:
                //System.out.println("memory operation not recognized");
                return null;
        }
    }
}
