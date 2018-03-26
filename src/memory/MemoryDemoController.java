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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.*;

public class MemoryDemoController {

    private Registers registers;
    private Decoder decoder;
    private CacheObject level1;
    private CacheObject level2;
    private CacheSet[] level1Sets;
    private CacheSet[] level2Sets;
    private MemoryObject memory;
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
        memoryTable_address.setCellValueFactory(new PropertyValueFactory<>("address"));
        memoryTable_content.setCellValueFactory(new PropertyValueFactory<>("content"));
        ObservableList<MemoryDisplayObject> data = readyDataForDisplay();
        memoryTable.getItems().setAll(data);
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
        int cycleCount = Integer.parseInt(cycle_text.getText());
        int PC = parseStringBinaryOrDecimal(PC_field.getText());
        System.out.println(PC);
        RWMemoryObject memoryOp = level1.read(PC);
        InstructionResult ir = decoder.decode(memoryOp.getWord());
        System.out.println(memoryOp.getWord());
        cycleCount += memoryOp.getWait();
        registers.setContent(ir.getDestination(), ir.getResult());
        PC += 1;
        registers.setPC(PC);
        cycle_text.setText(""+cycleCount);
        PC_field.setText(""+PC);
        updateRegisterDisplay();
    }

    private ObservableList<MemoryDisplayObject> formatRegisters(){
        ArrayList<MemoryDisplayObject> data = new ArrayList<>();
        for (int i=0; i<registers.getLength();i++){
            data.add(new MemoryDisplayObject(i, Integer.toString(registers.getContent(i))));
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
                        data.add(new MemoryDisplayObject(i, Integer.toString(memory.getMemory()[i])));
                        break;
                    case "Hex":
                        data.add(new MemoryDisplayObject(i, "0x"+Integer.toHexString(memory.getMemory()[i])));
                        break;
                    case "Binary":
                        data.add(new MemoryDisplayObject(i, "0b"+Integer.toBinaryString(memory.getMemory()[i])));
                        break;
                    case "Decoded":
                        data.add(new MemoryDisplayObject(i, BinaryInstructionOperations.decode(memory.getMemory()[i])));
                        break;
                }
            }
        }else{
            if (dropdownSelection.equals("L1")){
                for (int i = startLocation; i <= endLocation; i++) {
                    switch(displayType.getSelectionModel().getSelectedItem()){
                        case "Decimal":
                            data.add(new MemoryDisplayObject(i, Integer.toString(level1.directRead(i))));
                            break;
                        case "Hex":
                            data.add(new MemoryDisplayObject(i, "0x"+Integer.toHexString(level1.directRead(i))));
                            break;
                        case "Binary":
                            data.add(new MemoryDisplayObject(i, "0b"+Integer.toBinaryString(level1.directRead(i))));
                            break;
                        case "Decoded":
                            data.add(new MemoryDisplayObject(i, BinaryInstructionOperations.decode(level1.directRead(i))));
                            break;
                    }
                }
            }else{
                for (int i = startLocation; i <= endLocation; i++) {
                    switch(displayType.getSelectionModel().getSelectedItem()){
                        case "Decimal":
                            data.add(new MemoryDisplayObject(i, Integer.toString(level2.directRead(i))));
                            break;
                        case "Hex":
                            data.add(new MemoryDisplayObject(i, "0x"+Integer.toHexString(level2.directRead(i))));
                            break;
                        case "Binary":
                            data.add(new MemoryDisplayObject(i, "0b"+Integer.toBinaryString(level2.directRead(i))));
                            break;
                        case "Decoded":
                            data.add(new MemoryDisplayObject(i, BinaryInstructionOperations.decode(level2.directRead(i))));
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


    @FXML void load_program(){
        FileChooser fileChooser = new FileChooser();
        int memoryLocation = parseStringBinaryOrDecimal(addressTextField.getText());
        File file = fileChooser.showOpenDialog(addressTextField.getScene().getWindow());
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null){
                int[] memory = this.memory.getMemory();
                memory[memoryLocation] = Integer.parseInt(line, 2);
                memoryLocation += 1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        changeMemoryDisplay_button();
    }

    @FXML
    public void changeMemoryDisplay_button(){
        memoryTable.getItems().setAll(readyDataForDisplay());
    }

    @FXML
    public void rangeEnter(KeyEvent e){
        if (e.getCode() == KeyCode.ENTER){
            changeMemoryDisplay_button();
        }
    }

    @FXML
    public void displayTypeClicked(){
        changeMemoryDisplay_button();
    }

    @FXML
    public void memoryCacheChooserClicked(){
        changeMemoryDisplay_button();
    }

//    @FXML
//    public void write_button(){
//        int address = parseStringBinaryOrDecimal(this.addressTextField.getText());
//        int content = parseStringBinaryOrDecimal(this.contentTextField.getText());
//        int latency = level1.write(address, new RWMemoryObject(content, 0));
//        readText.setText("");
//        latencyText.setText("Latency: " + latency);
//        changeMemoryDisplay_button();
//    }

//    @FXML
//    public void read_button(){
//        int address = parseStringBinaryOrDecimal(this.addressTextField.getText());
//        RWMemoryObject readContent = level1.read(address);
//        readText.setText("Memory Content: " + readContent.getWord());
//        latencyText.setText("Latency: " + readContent.getWait());
//    }

//    @FXML
//    public void L1Set_clicked(MouseEvent arg0){
//        System.out.println("L1Set clicked on " + L1Sets.getSelectionModel().getSelectedIndex());
//        int clickedIndex = L1Sets.getSelectionModel().getSelectedIndex();
//        CacheSet set = level1Sets[clickedIndex];
//        int[] tags = set.getTags();
//        String[] tagsToDisplay = new String[tags.length];
//        String LRUs = "";
//        for (int i=0;i<tagsToDisplay.length;i++){
//            if (tags[i] == -1){
//                tagsToDisplay[i]="Empty";
//                LRUs = LRUs+set.getLru()[i]+"\n";
//            }else{
//                tagsToDisplay[i]=Integer.toString(tags[i]);
//                LRUs = LRUs+set.getLru()[i]+"\n";
//            }
//        }
//        ObservableList<String> items = FXCollections.observableArrayList(tagsToDisplay);
//        L1LRU.setText(LRUs);
//        L1Tags.setItems(items);
//    }
//
//    @FXML
//    public void L2Set_clicked(MouseEvent arg0){
//        System.out.println("L2Set clicked on " + L2Sets.getSelectionModel().getSelectedIndex());
//        int clickedIndex = L2Sets.getSelectionModel().getSelectedIndex();
//        CacheSet set = level2Sets[clickedIndex];
//        int[] tags = set.getTags();
//        String[] tagsToDisplay = new String[tags.length];
//        String LRUs = "";
//        for (int i=0;i<tagsToDisplay.length;i++){
//            if (tags[i] == -1){
//                tagsToDisplay[i]="Empty";
//                LRUs = LRUs + set.getLru()[i] + "\n";
//            }else{
//                tagsToDisplay[i]=Integer.toString(tags[i]);
//                LRUs = LRUs + set.getLru()[i] + "\n";
//            }
//        }
//        ObservableList<String> items = FXCollections.observableArrayList(tagsToDisplay);
//        L2LRU.setText(LRUs);
//        L2Tags.setItems(items);
//    }
//
//    @FXML
//    public void L1Tags_clicked(MouseEvent arg0){
//        System.out.println("L1Tags clicked on " + L1Tags.getSelectionModel().getSelectedIndex());
//        int clickedIndex = L1Tags.getSelectionModel().getSelectedIndex();
//        CacheSet set = level1Sets[L1Sets.getSelectionModel().getSelectedIndex()];
//        int tag = set.getTags()[clickedIndex];
//        if (tag!=-1){
//            String dataDisplay = "";
//            for (int i=0;i<16;i++) {
//                dataDisplay = dataDisplay + set.getData()[clickedIndex][i] + "\n";
//            }
//            L1Data.setText(dataDisplay);
//        }else{
//            L1Data.setText("");
//        }
//    }
//
//    @FXML
//    public void L2Tags_clicked(MouseEvent arg0){
//        System.out.println("L2Tags clicked on " + L2Tags.getSelectionModel().getSelectedIndex());
//        int clickedIndex = L2Tags.getSelectionModel().getSelectedIndex();
//        CacheSet set = level2Sets[L2Sets.getSelectionModel().getSelectedIndex()];
//        int tag = set.getTags()[clickedIndex];
//        if (tag!=-1){
//            String dataDisplay = "";
//            for (int i=0;i<16;i++){
//                dataDisplay = dataDisplay + set.getData()[clickedIndex][i] + "\n";
//            }
//            L2Data.setText(dataDisplay);
//        }else{
//            L2Data.setText("");
//        }
//    }

}
