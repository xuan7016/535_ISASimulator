package isa;

import java.io.*;
import java.util.HashMap;

public class Compiler {

    private File file;
    private HashMap<String, Integer> operation;
    private HashMap<String, Integer> registers;

    public Compiler(){
        init_hashmap();
    }

    public Compiler (File file){
        this.file = file;
        init_hashmap();
    }

    private void init_hashmap(){
        operation = new HashMap<>();
        operation.put("MOV", 0b00100000);
        operation.put("ADD", 0b00100001);
        operation.put("SUB", 0b00100010);
        operation.put("MUL", 0b00100011);
        operation.put("DIV", 0b00100100);
        operation.put("MOD", 0b00100101);
        operation.put("REM", 0b00100110);
        operation.put("SHF", 0b00100111);
        operation.put("AND", 0b00101000);
        operation.put("OR", 0b00101001);
        operation.put("XOR", 0b00101010);
        operation.put("NOT", 0b00101011);
        operation.put("JMP", 0b01000001);
        operation.put("JCD", 0b01000010);
        operation.put("JSR", 0b01000011);
        operation.put("RET", 0b01000100);
        operation.put("LOD", 0b100001);
        operation.put("STR", 0b100010);
        operation.put("EQ", 0b01100001);
        operation.put("LS", 0b1100010);
        operation.put("GT", 0b01100011);
        operation.put("HLT", 0b11111111);
        registers = new HashMap<>();
        for (int i=0; i<32; i++){
            registers.put("R"+i, i);
        }
    }

    public void compile(){
        try{
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            PrintWriter writer = new PrintWriter("compiled.bin", "UTF-8");
            String line = null;
            int encoded = 0;
            while ((line = br.readLine()) != null) {
                encoded = encode(line);
                writer.println(String.format("%32s", Integer.toBinaryString(encoded)).replace(' ', '0'));
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int encode(String line){
        int result = 0;
        String[] tokenized = line.split(" ");
        int op = operation.get(tokenized[0]);
        System.out.println(op);
        if (tokenized[0].equals("HLT") || tokenized[0].equals("RET")) return op << 24;
        if (tokenized[0].equals("LOD") || tokenized[0].equals("STR")){
            int rd = registers.get(tokenized[1]);
            int rm = registers.get(tokenized[2]);
            result = op << 26 | rd << 20 | rm << 15;
            if (tokenized.length > 3){
                result = result | 1 << 25 | Integer.parseInt(tokenized[4]);
            }
            return result;
        }else{
            int rd = registers.get(tokenized[1]);
            int rm = 0;
            int rn = 0;
            result = op << 24 | rd << 19;
            if (tokenized[0].equals("MOV")){
                result = result | Integer.parseInt(tokenized[2]) << 5;
                if (tokenized.length > 3) result = result | Integer.parseInt(tokenized[3]);
                return result;
            }
            if (tokenized[0].equals("SHF")){
                rm = registers.get(tokenized[2]);
                result = result | rm << 14;
                result = result | Integer.parseInt(tokenized[3]) << 7 | Integer.parseInt(tokenized[4]) << 6 | Integer.parseInt(tokenized[5]);
                return result;
            }
            if (tokenized[0].equals("NOT")){
                rm = registers.get(tokenized[2]);
                result = result | rm << 14;
                return result;
            }
            if (tokenized[0].equals("JMP")){
                if (tokenized.length > 2){
                    result = result | 1 << 15 | Integer.parseInt(tokenized[3]);
                    return result;
                }else{
                    return result;
                }
            }
            if (tokenized[0].equals("JCD")){
                rm = registers.get(tokenized[2]);
                result = result | rm << 14;
                if (tokenized.length > 3){
                    result = result | 1 << 13 | Integer.parseInt(tokenized[4]);
                    return result;
                }else{
                    return result;
                }
            }
            if (tokenized[0].equals("JSR")){
                if (tokenized.length > 2){
                    result = result | 1 << 16 | Integer.parseInt(tokenized[3]);
                    return result;
                }else{
                    return result;
                }
            }
            rm = registers.get(tokenized[2]);
            rn = registers.get(tokenized[3]);
            result = result | rm << 14 | rn << 8;
            if (tokenized.length > 4) result = result | 1 << 13 | Integer.parseInt(tokenized[5]);
            return result;
        }
    }

//    public static void main(String[] args){
//        Compiler c = new Compiler();
//        System.out.println(String.format("%32s", Integer.toBinaryString(c.encode("SHF R3 R5 1 1 3"))).replace(' ', '0'));
//    }
}
