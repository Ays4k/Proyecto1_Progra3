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
                MasterStageController.getInstance().cambiarEscena("/ui/Pantalla2.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
