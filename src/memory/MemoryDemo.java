package memory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MemoryDemo extends Application {

    private Stage primaryStage;
    private TabPane rootLayout;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Demo");

        try{
            FXMLLoader loader = new FXMLLoader(MemoryDemo.class.getResource("MemoryDemo.fxml"));
            rootLayout = (TabPane) loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
