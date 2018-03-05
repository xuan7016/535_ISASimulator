package memory;

import com.sun.org.apache.bcel.internal.generic.L2D;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.lang.reflect.Array;
import java.util.Arrays;

public class MemoryDemoController {

    private CacheObject level1;
    private CacheObject level2;
    private CacheSet[] level1Sets;
    private CacheSet[] level2Sets;
    private MemoryObject memory;
    @FXML private TextField addressTextField;
    @FXML private TextField contentTextField;
    @FXML private ListView memoryList;
    @FXML private ListView L1Sets;
    @FXML private ListView L2Sets;
    @FXML private ListView L1Tags;
    @FXML private ListView L2Tags;
    @FXML private Text readText;
    @FXML private Text latencyText;
    @FXML private Label L1Data;
    @FXML private Label L2Data;

    public MemoryDemoController(){
        this.memory = new MemoryObject(4194304);
        this.level2 = new CacheObject(512, 11, memory);
        this.level1 = new CacheObject(64, 4, level2);
        this.level1Sets = level1.getCache();
        this.level2Sets = level2.getCache();
    }

    @FXML
    public void initialize(){
        ObservableList<Integer> items = FXCollections.observableArrayList(Arrays.stream(memory.getMemory()).boxed().toArray(Integer[]::new));
        memoryList.setItems(items);
        Integer[] l1SetIndex = new Integer[level1Sets.length];
        Integer[] l2SetIndex = new Integer[level2Sets.length];
        for (int i=0;i<l1SetIndex.length;i++){
            l1SetIndex[i]=i;
        }
        for (int i=0;i<l2SetIndex.length;i++){
            l2SetIndex[i]=i;
        }
        ObservableList<Integer> l1SetIndexItems = FXCollections.observableArrayList(l1SetIndex);
        ObservableList<Integer> l2SetIndexItems = FXCollections.observableArrayList(l2SetIndex);
        L1Sets.setItems(l1SetIndexItems);
        L2Sets.setItems(l2SetIndexItems);
        L1Data.setText("");
        L2Data.setText("");
    }

    @FXML
    public void write_button(){
        int address = Integer.parseInt(this.addressTextField.getText());
        int content = Integer.parseInt(this.contentTextField.getText());
        int latency = level1.write(address, new RWMemoryObject(content, 0));
        readText.setText("");
        latencyText.setText("Latency: " + latency);
        ObservableList<Integer> items = FXCollections.observableArrayList(Arrays.stream(memory.getMemory()).boxed().toArray(Integer[]::new));
        memoryList.setItems(items);
    }

    @FXML
    public void read_button(){
        int address = Integer.parseInt(this.addressTextField.getText());
        RWMemoryObject readContent = level1.read(address);
        readText.setText("Memory Content: " + readContent.getWord());
        latencyText.setText("Latency: " + readContent.getWait());
    }

    @FXML
    public void L1Set_clicked(MouseEvent arg0){
        System.out.println("L1Set clicked on " + L1Sets.getSelectionModel().getSelectedIndex());
        int clickedIndex = L1Sets.getSelectionModel().getSelectedIndex();
        CacheSet set = level1Sets[clickedIndex];
        int[] tags = set.getTags();
        String[] tagsToDisplay = new String[tags.length];
        for (int i=0;i<tagsToDisplay.length;i++){
            if (tags[i] == -1){
                tagsToDisplay[i]="Empty";
            }else{
                tagsToDisplay[i]=Integer.toString(tags[i]);
            }
        }
        ObservableList<String> items = FXCollections.observableArrayList(tagsToDisplay);
        L1Tags.setItems(items);
    }

    @FXML
    public void L2Set_clicked(MouseEvent arg0){
        System.out.println("L2Set clicked on " + L2Sets.getSelectionModel().getSelectedIndex());
        int clickedIndex = L2Sets.getSelectionModel().getSelectedIndex();
        CacheSet set = level2Sets[clickedIndex];
        int[] tags = set.getTags();
        String[] tagsToDisplay = new String[tags.length];
        for (int i=0;i<tagsToDisplay.length;i++){
            if (tags[i] == -1){
                tagsToDisplay[i]="Empty";
            }else{
                tagsToDisplay[i]=Integer.toString(tags[i]);
            }
        }
        ObservableList<String> items = FXCollections.observableArrayList(tagsToDisplay);
        L2Tags.setItems(items);
    }

    @FXML
    public void L1Tags_clicked(MouseEvent arg0){
        System.out.println("L1Tags clicked on " + L1Tags.getSelectionModel().getSelectedIndex());
        int clickedIndex = L1Tags.getSelectionModel().getSelectedIndex();
        CacheSet set = level1Sets[L1Sets.getSelectionModel().getSelectedIndex()];
        int tag = set.getTags()[clickedIndex];
        if (tag!=-1){
            String dataDisplay = "";
            for (int i=0;i<16;i++) {
                dataDisplay = dataDisplay + set.getData()[clickedIndex][i] + "\n";
            }
            L1Data.setText(dataDisplay);
        }else{
            L1Data.setText("");
        }
    }

    @FXML
    public void L2Tags_clicked(MouseEvent arg0){
        System.out.println("L2Tags clicked on " + L2Tags.getSelectionModel().getSelectedIndex());
        int clickedIndex = L2Tags.getSelectionModel().getSelectedIndex();
        CacheSet set = level2Sets[L2Sets.getSelectionModel().getSelectedIndex()];
        int tag = set.getTags()[clickedIndex];
        if (tag!=-1){
            String dataDisplay = "";
            for (int i=0;i<16;i++){
                dataDisplay = dataDisplay + set.getData()[clickedIndex][i] + "\n";
            }
            L2Data.setText(dataDisplay);
        }else{
            L2Data.setText("");
        }
    }

}
