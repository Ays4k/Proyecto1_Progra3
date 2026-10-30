package SRR.Controlador;

import SRR.DTO.AlertaReservaDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AlertasController {

    @FXML private TableView<AlertaReservaDTO> tablaAlertas;
    @FXML private TableColumn<AlertaReservaDTO, String> colActividad;
    @FXML private TableColumn<AlertaReservaDTO, Void> colVer;
    @FXML private TableColumn<AlertaReservaDTO, Void> colDescartar;

    // La lista que la tabla muestra. Si se agrega o quita algo aquí, la tabla se actualiza sola.
    private final ObservableList<AlertaReservaDTO> alertas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tablaAlertas.setItems(alertas);
        tablaAlertas.setPlaceholder(new Label("No hay alertas"));

        // Texto de la primera columna: "Encargado - Actividad"
        colActividad.setCellValueFactory(celda -> new SimpleStringProperty(
                celda.getValue().getEncargado() + " - " + celda.getValue().getActividad()));

        // Las otras dos columnas llevan un botón en cada fila
        colVer.setCellFactory(columna -> crearCeldaConBoton("Ver", "", this::verAlerta));
        colDescartar.setCellFactory(columna -> crearCeldaConBoton("X",
                "-fx-text-fill: red; -fx-font-weight: bold;", this::descartarAlerta));

        cargarAlertasDePrueba();   // TEMPORAL: se borra cuando conectemos con el servidor real
    }

    // Agrega una alerta a la lista. Se puede llamar desde cualquier hilo:
    // Platform.runLater hace que el cambio ocurra en el hilo de la pantalla.
    public void agregarAlerta(AlertaReservaDTO alerta) {
        Platform.runLater(() -> alertas.add(alerta));
    }

    // Crea una celda que muestra un botón y, al pulsarlo, ejecuta la acción sobre la alerta de esa fila
    private TableCell<AlertaReservaDTO, Void> crearCeldaConBoton(String texto, String estilo,
                                                                 Consumer<AlertaReservaDTO> accion) {
        return new TableCell<>() {
            private final Button boton = new Button(texto);

            {
                boton.setStyle("-fx-padding: 2 6 2 6; " + estilo);
                boton.setOnAction(e -> accion.accept(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vacia) {
                super.updateItem(item, vacia);
                setGraphic(vacia ? null : boton);
            }
        };
    }

    // "Ver": ventana emergente con el detalle de la alerta
    private void verAlerta(AlertaReservaDTO alerta) {
        StringBuilder detalle = new StringBuilder();
        detalle.append("Encargado: ").append(alerta.getEncargado()).append("\n");
        detalle.append("Fecha: ").append(alerta.getFecha()).append("\n");
        detalle.append("Desde: ").append(alerta.getHoraInicio()).append("\n");
        detalle.append("Hasta: ").append(alerta.getHoraFin()).append("\n");
        detalle.append("Recursos:\n");
        if (alerta.getNombresRecursos() != null) {
            for (String recurso : alerta.getNombresRecursos()) {
                detalle.append("   - ").append(recurso).append("\n");
            }
        }

        Alert ventana = new Alert(Alert.AlertType.INFORMATION, detalle.toString());
        ventana.setTitle(alerta.getActividad());
        ventana.setHeaderText(null);
        ventana.showAndWait();
    }

    // "Descartar": pregunta y, si dice que sí, quita la alerta SOLO de esta lista (no se avisa al servidor)
    private void descartarAlerta(AlertaReservaDTO alerta) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, alerta.getActividad(),
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Descartar?");
        confirmacion.setHeaderText("¿Desea descartar esta alerta?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            alertas.remove(alerta);
        }
    }

    // TEMPORAL: alertas falsas para probar el panel
    private void cargarAlertasDePrueba() {
        agregarAlerta(new AlertaReservaDTO("RES-000101", "Juan Mora", "Reunion clientes",
                "2026-10-31", "08:00", "10:00",
                List.of("Sala de reuniones - Piso 1", "Laptop Dell Latitude #1")));
        agregarAlerta(new AlertaReservaDTO("RES-000102", "Pedro Arias", "Capacitacion",
                "2026-11-02", "13:00", "16:00",
                List.of("Proyector Epson #1")));
        agregarAlerta(new AlertaReservaDTO("RES-000103", "Ana Lopez", "Coordinacion viaje",
                "2026-11-03", "09:00", "11:00",
                List.of("Vehículo Toyota Corolla")));
    }
}