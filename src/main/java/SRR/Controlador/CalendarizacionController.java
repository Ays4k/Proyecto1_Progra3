package SRR.Controlador;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;
import SRR.Servicio.CategoriaServicio;
import SRR.Servicio.RecursoServicio;
import SRR.Servicio.ReservaServicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CalendarizacionController {

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<CategoriaDTO> cbCategoria;
    @FXML private Button btnCargar;
    @FXML private Button btnImprimir;
    @FXML private TableView<Map<String, String>> tblCalendarizacion;

    private final CategoriaServicio categoriaServicio = new CategoriaServicio();
    private final RecursoServicio recursoServicio = new RecursoServicio();
    private final ReservaServicio reservaServicio = new ReservaServicio();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        dpFecha.setValue(LocalDate.now());
        cargarCategorias();
    }

    private void cargarCategorias() {
        List<CategoriaDTO> lista = categoriaServicio.obtenerCategorias();
        cbCategoria.setItems(FXCollections.observableArrayList(lista));

        cbCategoria.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoriaDTO c) {
                return c == null ? "" : c.getDescripcion();
            }

            @Override
            public CategoriaDTO fromString(String s) {
                return null;
            }
        });

        if (!lista.isEmpty()) {
            cbCategoria.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleCargar(ActionEvent event) {
        LocalDate fecha = dpFecha.getValue();
        CategoriaDTO categoria = cbCategoria.getValue();

        if (fecha == null || categoria == null) {
            mostrarAlerta("Atención", "Seleccione una fecha y una categoría.");
            return;
        }

        tblCalendarizacion.getColumns().clear();
        tblCalendarizacion.getItems().clear();

        List<RecursoDTO> recursos = recursoServicio.obtenerRecursosPorCategoria(categoria.getId());
        List<TableColumn<Map<String, String>, ?>> nuevasColumnas = new ArrayList<>();

        // Columna Hora
        TableColumn<Map<String, String>, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Hora")));
        colHora.setStyle("-fx-alignment: CENTER;");
        colHora.setMinWidth(110); // Fija el tamaño de la columna Hora para que no se estire en exceso
        colHora.setMaxWidth(110);
        tblCalendarizacion.getColumns().add(colHora);

        // Calcular el ancho proporcional equitativo para cada recurso
        double anchoTabla = tblCalendarizacion.getWidth() > 0 ? tblCalendarizacion.getWidth() : 771.0;
        double anchoDisponible = Math.max(100.0, anchoTabla - 112.0);
        double anchoRecurso = recursos.isEmpty() ? 100.0 : (anchoDisponible / recursos.size());

        // Crear las columnas de recursos asignando el ancho proporcional inicial
        for (RecursoDTO rec : recursos) {
            TableColumn<Map<String, String>, String> colRecurso = new TableColumn<>(rec.getDescripcion());
            colRecurso.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(rec.getId())));
            colRecurso.setStyle("-fx-alignment: CENTER;");
            colRecurso.setPrefWidth(anchoRecurso);
            nuevasColumnas.add(colRecurso);
        }

        // Insertar TODAS las columnas atómicamente y aplicar la política de ajuste
        tblCalendarizacion.getColumns().addAll(nuevasColumnas);
        tblCalendarizacion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar datos de la tabla
        String fechaStr = fecha.toString();
        List<ReservaDTO> reservasDia = obtenerReservasActivasPorFecha(fechaStr);

        ObservableList<Map<String, String>> filas = FXCollections.observableArrayList();
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(17, 0);

        while (horaInicio.isBefore(horaFin)) {
            LocalTime siguienteHora = horaInicio.plusHours(1);
            Map<String, String> fila = new HashMap<>();
            String franja = horaInicio.format(timeFormatter) + " - " + siguienteHora.format(timeFormatter);
            fila.put("Hora", franja);

            for (RecursoDTO rec : recursos) {
                String estado = "Disponible";
                for (ReservaDTO res : reservasDia) {
                    if (res.getIdsRecursos() != null && res.getIdsRecursos().contains(rec.getId())) {
                        LocalTime resInicio = LocalTime.parse(res.getHoraInicio());
                        LocalTime resFin = LocalTime.parse(res.getHoraFin());

                        if (horaInicio.isBefore(resFin) && resInicio.isBefore(siguienteHora)) {
                            estado = "Ocupado: " + res.getActividad();
                            break;
                        }
                    }
                }
                fila.put(rec.getId(), estado);
            }

            filas.add(fila);
            horaInicio = siguienteHora;
        }

        tblCalendarizacion.setItems(filas);

        // Ajustar el alto de cada fila para llenar el 100% de la tabla sin dejar filas vacías
        if (!filas.isEmpty()) {
            tblCalendarizacion.fixedCellSizeProperty().bind(
                    tblCalendarizacion.heightProperty().subtract(29).divide(filas.size())
            );
        }
    }

    @FXML
    public void handleImprimir(ActionEvent event) {
        mostrarAlerta("Imprimir", "Enviando tabla de calendarización a la impresora...");
    }

    private List<ReservaDTO> obtenerReservasActivasPorFecha(String fecha) {
        List<ReservaDTO> result = new ArrayList<>();
        for (ReservaDTO r : reservaServicio.obtenerTodasLasReservas()) {
            if ("ACTIVA".equalsIgnoreCase(r.getEstado()) && fecha.equals(r.getFecha())) {
                result.add(r);
            }
        }
        return result;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}