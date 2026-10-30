package SRR.Controlador;

import SRR.DTO.AlertaReservaDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import SRR.Comunicacion.ClienteNotificaciones;
import SRR.Comunicacion.Conexion;
import SRR.DTO.Protocolo;
import SRR.Utilidades.Avisos;
import SRR.Utilidades.Sesion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

import SRR.Modelo.ListaAlertas;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AlertasController {

    @FXML private TableView<AlertaReservaDTO> tablaAlertas;
    @FXML private TableColumn<AlertaReservaDTO, String> colActividad;
    @FXML private TableColumn<AlertaReservaDTO, Void> colVer;
    @FXML private TableColumn<AlertaReservaDTO, Void> colDescartar;

    // La lista que la tabla muestra. Si se agrega o quita algo aquí, la tabla se actualiza sola.
    private final ListaAlertas listaAlertas = new ListaAlertas();

    // El hilo que escucha al servidor de alertas (uno por sesión)
    private ClienteNotificaciones clienteNotificaciones;

    @FXML
    public void initialize() {
        tablaAlertas.setItems(listaAlertas.getAlertas());
        tablaAlertas.setPlaceholder(new Label("No hay alertas"));

        // Texto de la primera columna: "Encargado - Actividad"
        colActividad.setCellValueFactory(celda -> new SimpleStringProperty(
                ListaAlertas.resumen(celda.getValue())));

        // Las otras dos columnas llevan un botón en cada fila
        colVer.setCellFactory(columna -> crearCeldaConBoton("Ver", "", this::verAlerta));
        colDescartar.setCellFactory(columna -> crearCeldaConBoton("X",
                "-fx-text-fill: red; -fx-font-weight: bold;", this::descartarAlerta));

        iniciarEscucha();
    }

    // Agrega una alerta a la lista. Se puede llamar desde cualquier hilo:
    // Platform.runLater hace que el cambio ocurra en el hilo de la pantalla.
    public void agregarAlerta(AlertaReservaDTO alerta) {
        Platform.runLater(() -> listaAlertas.agregar(alerta));
    }

    // Crea una celda que muestra un botón y, al pulsarlo, ejecuta la acción sobre la alerta de esa fila
    private TableCell<AlertaReservaDTO, Void> crearCeldaConBoton(String texto, String estilo,
                                                                 Consumer<AlertaReservaDTO> accion) {
        return new TableCell<>() {
            private final Button boton = new Button(texto);

            {
                boton.setStyle("-fx-padding: 2 6 2 6; " + estilo);
                boton.setPrefWidth(36);        // los dos botones miden lo mismo
                setAlignment(Pos.CENTER);      // centra el botón dentro de la celda
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
        GridPane datos = new GridPane();
        datos.setHgap(12);
        datos.setVgap(8);
        datos.setPadding(new Insets(10, 10, 5, 10));

        agregarFila(datos, 0, "Encargado:", alerta.getEncargado());
        agregarFila(datos, 1, "Fecha:", alerta.getFecha());
        agregarFila(datos, 2, "Desde:", alerta.getHoraInicio());
        agregarFila(datos, 3, "Hasta:", alerta.getHoraFin());

        Label etiquetaRecursos = new Label("Recursos:");
        etiquetaRecursos.setStyle("-fx-font-weight: bold;");
        ListView<String> listaRecursos = new ListView<>();
        if (alerta.getNombresRecursos() != null) {
            listaRecursos.getItems().addAll(alerta.getNombresRecursos());
        }
        listaRecursos.setPrefSize(260, 90);
        datos.add(etiquetaRecursos, 0, 4);
        datos.add(listaRecursos, 1, 4);
        GridPane.setValignment(etiquetaRecursos, VPos.TOP);

        Alert ventana = new Alert(Alert.AlertType.INFORMATION);
        ventana.setTitle("Detalle de la reserva");
        ventana.setHeaderText(alerta.getActividad());
        ventana.setGraphic(null);
        ventana.getDialogPane().setContent(datos);
        ventana.showAndWait();
    }

    // Agrega una fila "campo: valor" a la ventana de detalle
    private void agregarFila(GridPane tabla, int fila, String titulo, String valor) {
        Label etiqueta = new Label(titulo);
        etiqueta.setStyle("-fx-font-weight: bold;");
        tabla.add(etiqueta, 0, fila);
        tabla.add(new Label(valor), 1, fila);
    }

    // "Descartar": pregunta y, si dice que sí, quita la alerta SOLO de esta lista (no se avisa al servidor)
    private void descartarAlerta(AlertaReservaDTO alerta) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, alerta.getActividad(),
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Descartar?");
        confirmacion.setHeaderText("¿Desea descartar esta alerta?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            listaAlertas.descartar(alerta);
        }
    }

    // Abre la conexión con el servidor de alertas y empieza a escuchar
    private void iniciarEscucha() {
        clienteNotificaciones = new ClienteNotificaciones(
                Conexion.getInstance().getHost(),
                Protocolo.PUERTO_NOTIFICACIONES,
                Sesion.getId(),
                this::agregarAlerta,
                mensaje -> Platform.runLater(() -> Avisos.advertencia(mensaje)));
        clienteNotificaciones.iniciar();
    }

    // Se llama al cerrar sesión: apaga el hilo y cierra su conexión
    public void detenerEscucha() {
        if (clienteNotificaciones != null) {
            clienteNotificaciones.detener();
            clienteNotificaciones = null;
        }
    }
}