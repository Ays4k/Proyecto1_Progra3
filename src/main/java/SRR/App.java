package SRR;
import SRR.utilities.MasterStageController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/Prueba.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("My App");
        primaryStage.setScene(scene);
        MasterStageController.getInstance(primaryStage).cambiarEscena(scene);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
