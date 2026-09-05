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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.Color;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class EstadisticasController {

    // ---- Panel de Recursos ----
    @FXML private DatePicker dpDesdeRecursos;
    @FXML private DatePicker dpHastaRecursos;
    @FXML private Button btnAgregarRecursos;
    @FXML private TableView<Map<String, String>> tblEstadRecursos;
    @FXML private TableColumn<Map<String, String>, String> colCategoriaRecursos;
    @FXML private TableColumn<Map<String, String>, String> colCantidadRecursos;
    @FXML private AnchorPane paneGraficoRecursos;

    // ---- Panel de Actividades ----
    @FXML private DatePicker dpDesdeActividades;
    @FXML private DatePicker dpHastaActividades;
    @FXML private Button btnAgregarActividades;
    @FXML private TableView<Map<String, String>> tblEstadActividades;
    @FXML private TableColumn<Map<String, String>, String> colSemanaActividades;
    @FXML private TableColumn<Map<String, String>, String> colCantidadActividades;
    @FXML private AnchorPane paneGraficoActividades;

    private final ReservaServicio reservaServicio = new ReservaServicio();
    private final RecursoServicio recursoServicio = new RecursoServicio();
    private final CategoriaServicio categoriaServicio = new CategoriaServicio();

    @FXML
    public void initialize() {
        // Reparte el ancho de la tabla entre sus columnas (mismo criterio que en
        // CalendarizacionController y ActividadesController), evitando el scroll horizontal
        tblEstadRecursos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblEstadActividades.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Las columnas leen los valores desde un mapa clave-valor, ya que la cantidad
        // de filas depende de las categorias o semanas encontradas en cada consulta
        colCategoriaRecursos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("etiqueta")));
        colCantidadRecursos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("cantidad")));

        colSemanaActividades.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("etiqueta")));
        colCantidadActividades.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("cantidad")));

        // Centrar datos de las tablas de estadísticas
        colCategoriaRecursos.setStyle("-fx-alignment: CENTER;");
        colCantidadRecursos.setStyle("-fx-alignment: CENTER;");
        colSemanaActividades.setStyle("-fx-alignment: CENTER;");
        colCantidadActividades.setStyle("-fx-alignment: CENTER;");
    }

    @FXML
    public void handleCargarRecursos(ActionEvent event) {
        LocalDate desde = dpDesdeRecursos.getValue();
        LocalDate hasta = dpHastaRecursos.getValue();

        if (!fechasValidas(desde, hasta)) {
            return;
        }

        Map<String, Integer> conteo = contarCategoriasReservadas(desde, hasta);
        ObservableList<Map<String, String>> filas = convertirAFilas(conteo);

        tblEstadRecursos.setItems(filas);
        ajustarAltoFilas(tblEstadRecursos, filas);
        mostrarGrafico(paneGraficoRecursos, "Recursos Usados", "Categoria", "Cantidad",
                "Recurso", Color.BLUE, conteo);
    }

    @FXML
    public void handleCargarActividades(ActionEvent event) {
        LocalDate desde = dpDesdeActividades.getValue();
        LocalDate hasta = dpHastaActividades.getValue();

        if (!fechasValidas(desde, hasta)) {
            return;
        }

        Map<String, Integer> conteo = contarActividadesPorSemana(desde, hasta);
        ObservableList<Map<String, String>> filas = convertirAFilas(conteo);

        tblEstadActividades.setItems(filas);
        ajustarAltoFilas(tblEstadActividades, filas);
        mostrarGrafico(paneGraficoActividades, "Actividades Realizadas", "Semana", "Cantidad",
                "Semana", Color.RED, conteo);
    }

    // Cuenta cuantas veces se reservo cada categoria de recurso dentro del rango de fechas.
    // Solo se toman en cuenta las reservas ACTIVAS, ya que una reserva cancelada no llego
    // a usar realmente el recurso.
    private Map<String, Integer> contarCategoriasReservadas(LocalDate desde, LocalDate hasta) {
        Map<String, RecursoDTO> recursosPorId = obtenerRecursosPorId();
        Map<String, CategoriaDTO> categoriasPorId = obtenerCategoriasPorId();

        // LinkedHashMap conserva el orden en que se van encontrando las categorias
        Map<String, Integer> conteoPorCategoria = new LinkedHashMap<>();

        for (ReservaDTO reserva : reservaServicio.obtenerTodasLasReservas()) {
            if (!"ACTIVA".equalsIgnoreCase(reserva.getEstado())) {
                continue;
            }

            LocalDate fechaReserva = LocalDate.parse(reserva.getFecha());
            if (fechaReserva.isBefore(desde) || fechaReserva.isAfter(hasta)) {
                continue;
            }

            if (reserva.getIdsRecursos() == null) {
                continue;
            }

            for (String idRecurso : reserva.getIdsRecursos()) {
                String descripcionCategoria = descripcionDeLaCategoria(idRecurso, recursosPorId, categoriasPorId);
                incrementar(conteoPorCategoria, descripcionCategoria);
            }
        }
        return conteoPorCategoria;
    }

    // Agrupa las reservas activas del rango por semana (el lunes de cada semana se usa como
    // etiqueta, en formato ISO) y cuenta cuantas actividades cayeron en cada una.
    private Map<String, Integer> contarActividadesPorSemana(LocalDate desde, LocalDate hasta) {
        // TreeMap ordena las semanas cronologicamente porque la llave es la fecha del
        // lunes en texto ISO ("2026-08-03"), que ordena igual que la fecha real
        Map<String, Integer> conteoPorSemana = new TreeMap<>();

        for (ReservaDTO reserva : reservaServicio.obtenerTodasLasReservas()) {
            if (!"ACTIVA".equalsIgnoreCase(reserva.getEstado())) {
                continue;
            }

            LocalDate fechaReserva = LocalDate.parse(reserva.getFecha());
            if (fechaReserva.isBefore(desde) || fechaReserva.isAfter(hasta)) {
                continue;
            }

            LocalDate lunesDeLaSemana = fechaReserva.with(DayOfWeek.MONDAY);
            incrementar(conteoPorSemana, lunesDeLaSemana.toString());
        }
        return conteoPorSemana;
    }

    // Busca el recurso por id y devuelve la descripcion de su categoria.
    // Si el recurso o la categoria no existen (datos inconsistentes), se devuelve el id tal cual.
    private String descripcionDeLaCategoria(String idRecurso, Map<String, RecursoDTO> recursosPorId,
                                            Map<String, CategoriaDTO> categoriasPorId) {
        RecursoDTO recurso = recursosPorId.get(idRecurso);
        if (recurso == null) {
            return idRecurso;
        }
        CategoriaDTO categoria = categoriasPorId.get(recurso.getIdCategoria());
        return (categoria == null) ? recurso.getIdCategoria() : categoria.getDescripcion();
    }

    private Map<String, RecursoDTO> obtenerRecursosPorId() {
        Map<String, RecursoDTO> mapa = new LinkedHashMap<>();
        for (RecursoDTO recurso : recursoServicio.obtenerRecursos()) {
            mapa.put(recurso.getId(), recurso);
        }
        return mapa;
    }

    private Map<String, CategoriaDTO> obtenerCategoriasPorId() {
        Map<String, CategoriaDTO> mapa = new LinkedHashMap<>();
        for (CategoriaDTO categoria : categoriaServicio.obtenerCategorias()) {
            mapa.put(categoria.getId(), categoria);
        }
        return mapa;
    }

    // Suma 1 a la cantidad guardada para esa clave, o la crea en 1 si es la primera vez
    private void incrementar(Map<String, Integer> mapa, String clave) {
        Integer actual = mapa.get(clave);
        mapa.put(clave, (actual == null) ? 1 : actual + 1);
    }

    // Convierte el mapa de conteos en filas que el TableView pueda mostrar
    private ObservableList<Map<String, String>> convertirAFilas(Map<String, Integer> conteo) {
        ObservableList<Map<String, String>> filas = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entrada : conteo.entrySet()) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("etiqueta", entrada.getKey());
            fila.put("cantidad", String.valueOf(entrada.getValue()));
            filas.add(fila);
        }
        return filas;
    }

    // Estira el alto de cada fila para que las filas con datos llenen el 100% de la tabla,
    // todas del mismo tamaño y sin espacios en blanco al final. Si son muchas filas y no caben
    // adecuadamente, fija un alto cómodo por fila y habilita el scrollbar vertical.
    private void ajustarAltoFilas(TableView<Map<String, String>> tabla, ObservableList<Map<String, String>> filas) {
        tabla.fixedCellSizeProperty().unbind();

        if (filas.isEmpty()) {
            tabla.setFixedCellSize(-1);
            return;
        }

        double alturaEstandar = 25.0; // Alto mínimo y legible por fila
        double altoEncabezado = 29.0;
        double altoDisponible = Math.max(0, (tabla.getHeight() > 0 ? tabla.getHeight() : tabla.getPrefHeight()) - altoEncabezado);
        double altoCalculado = altoDisponible / filas.size();

        if (altoCalculado >= alturaEstandar) {
            // Sobra espacio: estira las filas equitativamente para llenar tudo el alto disponible
            tabla.fixedCellSizeProperty().bind(
                    tabla.heightProperty().subtract(altoEncabezado).divide(filas.size())
            );
        } else {
            // Hay bastantes filas: fija el tamaño estándar y activa la barra de desplazamiento vertical
            tabla.setFixedCellSize(alturaEstandar);
        }
    }

    // Arma un grafico de barras con JFreeChart a partir del conteo y lo coloca dentro del
    // AnchorPane que viene del FXML, estirandolo para que ocupe tudo el espacio disponible.
    private void mostrarGrafico(AnchorPane contenedor, String titulo, String ejeCategorias,
                                String ejeValores, String nombreSerie, Color color,
                                Map<String, Integer> conteo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entrada : conteo.entrySet()) {
            dataset.addValue(entrada.getValue(), nombreSerie, entrada.getKey());
        }

        JFreeChart grafico = ChartFactory.createBarChart(titulo, ejeCategorias, ejeValores, dataset);

        // Ajustes visuales: color de las barras y eje Y solo con numeros enteros,
        // ya que las cantidades siempre son numeros enteros
        CategoryPlot plot = grafico.getCategoryPlot();
        plot.getRenderer().setSeriesPaint(0, color);
        ((NumberAxis) plot.getRangeAxis()).setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ChartViewer visor = new ChartViewer(grafico);
        AnchorPane.setTopAnchor(visor, 0.0);
        AnchorPane.setBottomAnchor(visor, 0.0);
        AnchorPane.setLeftAnchor(visor, 0.0);
        AnchorPane.setRightAnchor(visor, 0.0);

        // Se limpia el contenedor por si ya tenia un grafico de una consulta anterior
        contenedor.getChildren().clear();
        contenedor.getChildren().add(visor);
    }

    // Valida que ambas fechas esten seleccionadas y que "desde" no sea posterior a "hasta"
    private boolean fechasValidas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            mostrarAlerta("Atencion", "Debe seleccionar ambas fechas.");
            return false;
        }
        if (desde.isAfter(hasta)) {
            mostrarAlerta("Atencion", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}