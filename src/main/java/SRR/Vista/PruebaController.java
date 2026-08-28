package SRR.Vista;
import SRR.Singleton.MasterStageController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PruebaController {
    @FXML private Button btnCambioEscena;

    @FXML
    private void initialize() {
        btnCambioEscena.setOnAction(event -> {
            try {
                MasterStageController.getInstance().cambiarEscena("/Escenas/Pantalla2.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
