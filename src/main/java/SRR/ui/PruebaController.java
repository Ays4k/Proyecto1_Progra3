package SRR.ui;
import SRR.utilities.MasterStageController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class PruebaController {
    @FXML private Button btnCambioEscena;

    @FXML
    private void initialize() {
        btnCambioEscena.setOnAction(event -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/ui/Pantalla2.fxml"));
                Scene scene = new Scene(root);
                MasterStageController.getInstance().cambiarEscena(scene);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
