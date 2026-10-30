package SRR.Controlador;

import SRR.DTO.AlertaReservaDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AlertasController {

    @FXML private TableView<AlertaReservaDTO> tablaAlertas;
    @FXML private TableColumn<AlertaReservaDTO, String> colActividad;
    @FXML private TableColumn<AlertaReservaDTO, Void> colVer;
    @FXML private TableColumn<AlertaReservaDTO, Void> colDescartar;

    @FXML
    public void initialize() {
        // Por ahora vacío: en un paso siguiente llenamos la tabla
    }
}