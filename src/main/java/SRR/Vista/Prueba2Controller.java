package SRR.Vista;
import SRR.Singleton.MasterStageController;

import javafx.fxml.FXML;
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
