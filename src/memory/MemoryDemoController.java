package memory;

import isa.*;
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
    private ArrayList<String> pipeline_using;
    private boolean count_all;
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
    @FXML private Text fetchText;
    @FXML private Text decodeText;
    @FXML private Text executeText;
    @FXML private Text memoryText;
    @FXML private Text writeText;
    @FXML private CheckBox cache_checkbox;
    @FXML private CheckBox pipeline_checkbox;

    public MemoryDemoController(){
        this.pipeline_using = new ArrayList<>();
        this.memory = new MemoryObject(4194304);
        this.level2 = new CacheObject(512, 11, memory);
        this.level1 = new CacheObject(64, 3, level2);
        this.registers = new Registers(32);
        this.decoder = new Decoder(registers, level1);
        this.level1Sets = level1.getCache();
        this.level2Sets = level2.getCache();
        this.breakpoints = new ArrayList<>();
        this.count_all = true;
    }

    @FXML
    public void initialize(){
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
        fetchText.setText("");
        decodeText.setText("");
        executeText.setText("");
        memoryText.setText("");
        writeText.setText("");
    }


    @FXML void step(){
        if (pipeline_checkbox.isSelected()){
            step_pipeline();
        }else{
            step_no_pipe();
        }
    }

    @FXML void step_pipeline(){
        int PC = parseStringBinaryOrDecimal(PC_field.getText());
        RWMemoryObject memoryOp;
        if (cache_checkbox.isSelected()){
            memoryOp = level1.read(PC);
        }else{
            memoryOp = memory.read(PC);
        }
        int instruction = memoryOp.getWord();
        // if HLT then stop
        if ((memoryOp.getWord() >>> 29) == 0b111) return;

        String[] tokenized = BinaryInstructionOperations.decode(memoryOp.getWord()).split(" ");
        int cycleCount = Integer.parseInt(cycle_text.getText());
        if (count_all){
            count_all=false;
            cycleCount += memoryOp.getWait() + 5;
            if (!pipeline_using.isEmpty()) pipeline_using.remove(0);
            pipeline_using.add(tokenized[1]);
        }else{
            if (tokenized.length > 1) {
                if (check_in_use(tokenized)){
                    cycleCount += 2;
                    if (!pipeline_using.isEmpty()) pipeline_using.remove(0);
                    pipeline_using.add(tokenized[1]);
                } else {
                    cycleCount += 1;
                    if (!pipeline_using.isEmpty()) pipeline_using.remove(0);
                    pipeline_using.add(tokenized[1]);
                }
            }else{
                cycleCount += 1;
            }
        }
        if ((memoryOp.getWord() >>> 29) == 0b010) count_all = true;

        PC += 1;
        registers.setPC(PC);
        InstructionResult ir = decoder.decode(instruction);
        if (ir!=null){
            if (ir.getDestination()==32){
                registers.setPC(ir.getResult());
            }else {
                registers.setContent(ir.getDestination(), ir.getResult());
            }
        }
        cycle_text.setText("" + cycleCount);
        PC_field.setText(""+registers.getPC());
        System.out.println(pipeline_using);
        updateRegisterDisplay();
        changeMemoryDisplay_button();
    }

    private void step_no_pipe(){
        int PC = parseStringBinaryOrDecimal(PC_field.getText());
        RWMemoryObject memoryOp;
        if (cache_checkbox.isSelected()){
            memoryOp = level1.read(PC);
        }else{
            memoryOp = memory.read(PC);
        }
        // if HLT then stop
        if ((memoryOp.getWord() >>> 29) == 0b111) return;
        execute(memoryOp.getWord(), memoryOp.getWait());
    }

    @FXML void execute_all(){
        while(true){
            int PC = parseStringBinaryOrDecimal(PC_field.getText());
            // if breakpoint then stop
            if (breakpoints.contains(PC)) return;
            RWMemoryObject memoryOp = memory.read(PC);
            // if HLT then stop
            if ((memoryOp.getWord() >>> 29) == 0b111) return;
            if (pipeline_checkbox.isSelected()){
                step_pipeline();
            }else {
                step_no_pipe();
            }
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
            if (ir.getDestination()==32){
                registers.setPC(ir.getResult());
            }else {
                registers.setContent(ir.getDestination(), ir.getResult());
            }
        }
        cycle_text.setText(""+(cycleCount+5));
        PC_field.setText(""+registers.getPC());
        updateRegisterDisplay();
        changeMemoryDisplay_button();
    }

    private boolean check_in_use(String[] tokenized){
        for (String s : tokenized){
            if (s.charAt(0)=='R'){
                if (pipeline_using.contains(s)){
                    return true;
                }
            }
        }
        return false;
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
        if (breakpoints.contains(address)){
            breakpoints.remove(new Integer(address));
        }else {
            breakpoints.add(address);
        }
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
