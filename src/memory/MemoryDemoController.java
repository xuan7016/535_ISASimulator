package memory;

import isa.BinaryInstructionOperations;
import isa.Decoder;
import isa.InstructionResult;
import isa.Registers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class MemoryDemoController {

    private Registers registers;
    private Decoder decoder;
    private CacheObject level1;
    private CacheObject level2;
    private CacheSet[] level1Sets;
    private CacheSet[] level2Sets;
    private MemoryObject memory;
    private ArrayList<Integer> breakpoints;
    @FXML private TextField addressTextField;
    @FXML private TextField memoryDisplayRangeStart;
    @FXML private TextField memoryDisplayRangeEnd;
    @FXML private TextField PC_field;
    @FXML private ComboBox<String> memoryCacheChooser;
    @FXML private ComboBox<String> displayType;
    @FXML private TableView<MemoryDisplayObject> memoryTable;
    @FXML private TableColumn<MemoryDisplayObject, String> memoryTable_address;
    @FXML private TableColumn<MemoryDisplayObject, String> memoryTable_content;
    @FXML private TableView<MemoryDisplayObject> registersTableView;
    @FXML private TableColumn<MemoryDisplayObject, String> registersIndexColumn;
    @FXML private TableColumn<MemoryDisplayObject, String> registersContentColumn;
    @FXML private Text cycle_text;

    public MemoryDemoController(){
        this.memory = new MemoryObject(4194304);
        this.level2 = new CacheObject(512, 11, memory);
        this.level1 = new CacheObject(64, 4, level2);
        this.registers = new Registers(32);
        this.decoder = new Decoder(registers, level1);
        this.level1Sets = level1.getCache();
        this.level2Sets = level2.getCache();
    }

    @FXML
    public void initialize(){
        breakpoints = new ArrayList<>();
        memoryTable_address.setCellValueFactory(new PropertyValueFactory<>("address"));
        memoryTable_content.setCellValueFactory(new PropertyValueFactory<>("content"));
        memoryTable.getItems().setAll(readyDataForDisplay());
        registersIndexColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        registersContentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        registersTableView.getItems().setAll(formatRegisters());
        Integer[] l1SetIndex = new Integer[level1Sets.length];
        Integer[] l2SetIndex = new Integer[level2Sets.length];
        for (int i=0;i<l1SetIndex.length;i++){
            l1SetIndex[i]=i;
        }
        for (int i=0;i<l2SetIndex.length;i++){
            l2SetIndex[i]=i;
        }
    }

    @FXML void step(){
        int PC = parseStringBinaryOrDecimal(PC_field.getText());
        RWMemoryObject memoryOp = level1.read(PC);
        // if HLT then stop
        if ((memoryOp.getWord() >>> 29) == 0b111) return;
        execute(memoryOp.getWord(), memoryOp.getWait());
//        int cycleCount = Integer.parseInt(cycle_text.getText());
//        int PC = parseStringBinaryOrDecimal(PC_field.getText());
//        PC += 1;
//        registers.setPC(PC);
//        InstructionResult ir = decoder.decode(memoryOp.getWord());
//        System.out.println(memoryOp.getWord());
//        cycleCount += memoryOp.getWait();
//        if (ir!=null) {
//            registers.setContent(ir.getDestination(), ir.getResult());
//        }
//        cycle_text.setText(""+(cycleCount+5));
//        PC_field.setText(""+registers.getPC());
//        updateRegisterDisplay();
//        changeMemoryDisplay_button();
    }

    @FXML void execute_all(){
        while(true){
            int PC = parseStringBinaryOrDecimal(PC_field.getText());
            if (breakpoints.contains(PC)) return;
            RWMemoryObject memoryOp = level1.read(PC);
            // if HLT then stop
            if ((memoryOp.getWord() >>> 29) == 0b111) return;
            execute(memoryOp.getWord(), memoryOp.getWait());
        }
    }

    private void execute(int instruction, int wait){
        int cycleCount = Integer.parseInt(cycle_text.getText());
        int PC = parseStringBinaryOrDecimal(PC_field.getText());
        PC += 1;
        registers.setPC(PC);
        InstructionResult ir = decoder.decode(instruction);
        cycleCount += wait;
        if (ir!=null) {
            registers.setContent(ir.getDestination(), ir.getResult());
        }
        cycle_text.setText(""+(cycleCount+5));
        PC_field.setText(""+registers.getPC());
        updateRegisterDisplay();
        changeMemoryDisplay_button();
    }


    private ObservableList<MemoryDisplayObject> formatRegisters(){
        ArrayList<MemoryDisplayObject> data = new ArrayList<>();
        for (int i=0; i<registers.getLength();i++){
            data.add(new MemoryDisplayObject(""+i, Integer.toString(registers.getContent(i))));
        }
        return FXCollections.observableArrayList(data);
    }

    private void updateRegisterDisplay(){
        registersTableView.getItems().setAll(formatRegisters());
    }

    private ObservableList<MemoryDisplayObject> readyDataForDisplay(){
        String dropdownSelection = memoryCacheChooser.getSelectionModel().getSelectedItem();
        int startLocation = parseStringBinaryOrDecimal(memoryDisplayRangeStart.getText());
        int endLocation = parseStringBinaryOrDecimal(memoryDisplayRangeEnd.getText());
        ArrayList<MemoryDisplayObject> data = new ArrayList<>();
        if (dropdownSelection.equals("Memory")) {
            for (int i = startLocation; i <= endLocation; i++) {
                switch(displayType.getSelectionModel().getSelectedItem()){
                    case "Decimal":
                        if (breakpoints.contains(i)){
                            data.add(new MemoryDisplayObject(i + "  BREAKPOINT", Integer.toString(memory.getMemory()[i])));
                        }else{
                            data.add(new MemoryDisplayObject(""+i, Integer.toString(memory.getMemory()[i])));

                        }
                        break;
                    case "Hex":
                        if (breakpoints.contains(i)) {
                            data.add(new MemoryDisplayObject(i + "  BREAKPOINT", Integer.toHexString(memory.getMemory()[i])));
                        }else{
                            data.add(new MemoryDisplayObject(""+i, Integer.toHexString(memory.getMemory()[i])));

                        }
                        break;
                    case "Binary":
                        if (breakpoints.contains(i)) {
                            data.add(new MemoryDisplayObject(i + "  BREAKPOINT", String.format("%32s", Integer.toBinaryString(memory.getMemory()[i])).replace(" ", "0")));
                        }else{
                            data.add(new MemoryDisplayObject(""+i, String.format("%32s", Integer.toBinaryString(memory.getMemory()[i])).replace(" ", "0")));
                        }
                        break;
                    case "Decoded":
                        if (breakpoints.contains(i)) {
                            data.add(new MemoryDisplayObject(i + "  BREAKPOINT", BinaryInstructionOperations.decode(memory.getMemory()[i])));
                        }else{
                            data.add(new MemoryDisplayObject(""+i, BinaryInstructionOperations.decode(memory.getMemory()[i])));
                        }
                        break;
                }
            }
        }else{
            if (dropdownSelection.equals("L1") || dropdownSelection.equals("L2")){
                CacheObject mem  = (dropdownSelection.equals("L1")) ? level1 : level2;
                for (int i = startLocation; i<= endLocation; i++){
                    switch ((displayType.getSelectionModel().getSelectedItem())){
                        case "Decimal":
                            data.add(new MemoryDisplayObject(""+i, Integer.toString(mem.directRead(i))));
                            break;
                        case "Hex":
                            data.add(new MemoryDisplayObject(""+i, Integer.toHexString(mem.directRead(i))));
                            break;
                        case "Binary":
                            data.add(new MemoryDisplayObject(""+i, String.format("%32s", Integer.toBinaryString(mem.directRead(i)).replace(" ", "0"))));
                            break;
                        case "Decoded":
                            data.add(new MemoryDisplayObject(""+i, BinaryInstructionOperations.decode(mem.directRead(i))));
                            break;
                    }
                }
            }
        }
        return FXCollections.observableArrayList(data);
    }

    private int parseStringBinaryOrDecimal(String s){
        if (s.equals("")) return 0;
        int number;
        if (s.length() > 2){
            String firstTwo = s.substring(0,2);
            if (firstTwo.equals("0b")) {
                number = Integer.parseInt(s.substring(2,s.length()),2);
            }else{
                number = Integer.parseInt(s);
            }
        }else{
            number = Integer.parseInt(s);
        }
        return number;
    }


    @FXML void load_program_button(){
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(addressTextField.getScene().getWindow());
        load_program(file);
        changeMemoryDisplay_button();
    }

    @FXML void load_and_compile(){
        isa.Compiler c = new isa.Compiler(new FileChooser().showOpenDialog(addressTextField.getScene().getWindow()));
        c.compile();
        load_program(new File("compiled.bin"));
        changeMemoryDisplay_button();
    }

    private void load_program(File file){
        int memoryLocation = parseStringBinaryOrDecimal(addressTextField.getText());
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null){
                int[] memory = this.memory.getMemory();
                memory[memoryLocation] = new BigInteger(line, 2).intValue();
                memoryLocation += 1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void changeMemoryDisplay_button(){
        memoryTable.getItems().setAll(readyDataForDisplay());
        int PC = parseStringBinaryOrDecimal(PC_field.getText());
        if (PC >= parseStringBinaryOrDecimal(memoryDisplayRangeStart.getText()) && PC <= parseStringBinaryOrDecimal(memoryDisplayRangeEnd.getText())){
            memoryTable.getSelectionModel().select(PC);
        }
    }

    @FXML void set_breakpoint(){
        int address = memoryTable.getSelectionModel().getSelectedIndex();
        breakpoints.add(address);
        changeMemoryDisplay_button();
    }

    @FXML void rangeEnter(KeyEvent e){
        if (e.getCode() == KeyCode.ENTER){
            changeMemoryDisplay_button();
        }
    }

    @FXML void displayTypeClicked(){
        changeMemoryDisplay_button();
    }

    @FXML void memoryCacheChooserClicked(){
        changeMemoryDisplay_button();
    }

}
