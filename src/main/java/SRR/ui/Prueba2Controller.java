package SRR.ui;
import SRR.utilities.MasterStageController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class Prueba2Controller {
    @FXML private Button btnVolver;

    @FXML
    private void initialize() {
        btnVolver.setOnAction(event -> {
            try {
                MasterStageController.getInstance().volverEscenaAnterior();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
