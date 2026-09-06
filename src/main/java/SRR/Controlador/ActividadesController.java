package SRR.Controlador;

import SRR.DTO.ReservaDTO;
import SRR.Servicio.ReservaServicio;
import SRR.DTO.UsuarioDTO;
import SRR.Servicio.UsuarioServicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import SRR.Utilidades.Avisos;
import SRR.Utilidades.ReportePdf;
import SRR.Utilidades.RutaDestino;
import java.io.File;

public class ActividadesController {

    @FXML private DatePicker dpFechaReferencia;
    @FXML private Button btnCargar;
    @FXML private Button btnImprimir;
    @FXML private TableView<Map<String, String>> tblActividades;

    private final ReservaServicio reservaServicio = new ReservaServicio();
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        dpFechaReferencia.setValue(LocalDate.now());
        configurarColumnasTableView();
    }

    private void configurarColumnasTableView() {
        tblActividades.getColumns().clear();
        // Ajusta automáticamente las columnas al ancho del TableView
        tblActividades.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String[] dias = {"Hora", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (String dia : dias) {
            TableColumn<Map<String, String>, String> col = new TableColumn<>(dia);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dia)));

            // Centrar texto de la columna
            col.setStyle("-fx-alignment: CENTER;");

            col.setSortable(false);

            // Fijar ancho de la columna Hora y dejar que el resto se divida equitativamente
            if ("Hora".equals(dia)) {
                col.setMinWidth(100);
                col.setMaxWidth(120);
            }

            tblActividades.getColumns().add(col);
        }
    }

    private String nombreFuncionario(String idFuncionario) {
        UsuarioDTO usuario = usuarioServicio.buscarPorId(idFuncionario);
        return usuario == null || usuario.getNombre() == null
                ? idFuncionario : usuario.getNombre();
    }

    @FXML
    public void handleCargar(ActionEvent event) {
        LocalDate fechaRef = dpFechaReferencia.getValue();
        if (fechaRef == null) {
            mostrarAlerta("Atención", "Seleccione una fecha de referencia.");
            return;
        }

        LocalDate lunes = fechaRef.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);

        List<ReservaDTO> reservasSemana = obtenerReservasEnRango(lunes, domingo);

        tblActividades.getItems().clear();
        ObservableList<Map<String, String>> filas = FXCollections.observableArrayList();

        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(17, 0);

        String[] nombresDias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

        while (horaInicio.isBefore(horaFin)) {
            LocalTime siguienteHora = horaInicio.plusHours(1);
            Map<String, String> fila = new HashMap<>();
            String franja = horaInicio.format(timeFormatter) + " - " + siguienteHora.format(timeFormatter);
            fila.put("Hora", franja);

            for (int i = 0; i < 7; i++) {
                LocalDate diaActual = lunes.plusDays(i);
                String claveDia = nombresDias[i];
                String actividadesDia = "-";

                for (ReservaDTO res : reservasSemana) {
                    if (diaActual.toString().equals(res.getFecha())) {
                        LocalTime resInicio = LocalTime.parse(res.getHoraInicio());
                        LocalTime resFin = LocalTime.parse(res.getHoraFin());

                        if (horaInicio.isBefore(resFin) && resInicio.isBefore(siguienteHora)) {
                            actividadesDia = res.getActividad() + " (" + nombreFuncionario(res.getIdFuncionario()) + ")";
                            break;
                        }
                    }
                }
                fila.put(claveDia, actividadesDia);
            }

            filas.add(fila);
            horaInicio = siguienteHora;
        }

        tblActividades.setItems(filas);

        // Ajustar el alto de cada fila para llenar el 100% de la tabla sin dejar filas vacías
        if (!filas.isEmpty()) {
            tblActividades.fixedCellSizeProperty().bind(
                    tblActividades.heightProperty().subtract(29).divide(filas.size())
            );
        }
    }

    @FXML
    public void handleImprimir(ActionEvent event) {
        if (tblActividades.getItems().isEmpty()) {
            Avisos.advertencia("Cargue primero la programacion de actividades");
            return;
        }

        File destino = RutaDestino.pedirDestinoPdf("actividades.pdf",
                btnImprimir.getScene().getWindow());
        if (destino == null) {
            return;
        }

        LocalDate lunes = dpFechaReferencia.getValue().with(DayOfWeek.MONDAY);
        String titulo = "Actividades del " + lunes + " al " + lunes.plusDays(6);

        try {
            ReportePdf.generarDesdeTabla(titulo, tblActividades, destino, true);
            Avisos.info("Reporte generado");
        } catch (RuntimeException e) {
            Avisos.error("No se pudo generar el reporte");
        }
    }

    private List<ReservaDTO> obtenerReservasEnRango(LocalDate inicio, LocalDate fin) {
        List<ReservaDTO> result = new ArrayList<>();
        for (ReservaDTO r : reservaServicio.obtenerTodasLasReservas()) {
            if ("ACTIVA".equalsIgnoreCase(r.getEstado())) {
                LocalDate f = LocalDate.parse(r.getFecha());
                if (!f.isBefore(inicio) && !f.isAfter(fin)) {
                    result.add(r);
                }
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