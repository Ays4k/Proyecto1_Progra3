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
        primaryStage.setTitle("Sistema de Reservas de Recursos");
        MasterStageController.getInstance(primaryStage).cambiarEscena("/ui/Login.fxml");

    }

    public static void main(String[] args) {
        launch(args);
    }
}
