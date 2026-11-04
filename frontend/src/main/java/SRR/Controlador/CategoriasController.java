package SRR.Controlador;

import SRR.Comunicacion.ServidorException;
import SRR.DTO.CategoriaDTO;
import SRR.Proxy.CategoriaProxy;
import SRR.Utilidades.Avisos;
import SRR.Utilidades.ReportePdf;
import SRR.Utilidades.RutaDestino;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CategoriasController {

    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClean;
    @FXML private TextField txtShowId;
    @FXML private TextField txtShowDesc;
    @FXML private TableView<CategoriaDTO> catTable;
    @FXML private TableColumn<CategoriaDTO, String> colId;
    @FXML private TableColumn<CategoriaDTO, String> colDesc;
    @FXML private ObservableList<CategoriaDTO> categoriaList;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private Label lblSearch;
    @FXML private Button btnImprimir;
    private final CategoriaProxy categoriaServicio = new CategoriaProxy();
    private FilteredList<CategoriaDTO> categoriasFiltradas;

    @FXML
    public void initialize() {
        catTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        txtShowId.setDisable(true);   // el id lo genera la base, solo se muestra
        txtShowId.setPromptText("Automático");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Centrar texto de las columnas
        colId.setStyle("-fx-alignment: CENTER;");
        colDesc.setStyle("-fx-alignment: CENTER;");

        // Ajustar alto de filas cuando cambien los ítems, la lista o la altura de la tabla
        catTable.heightProperty().addListener((obs, oldH, newH) -> ajustarAltoFilas(catTable, catTable.getItems()));
        catTable.itemsProperty().addListener((obs, oldList, newList) -> {
            if (newList != null) {
                ajustarAltoFilas(catTable, newList);
                newList.addListener((javafx.collections.ListChangeListener.Change<? extends CategoriaDTO> c) ->
                        ajustarAltoFilas(catTable, newList)
                );
            }
        });

        try {
            categoriaList = FXCollections.observableList(categoriaServicio.obtenerCategorias());
        } catch (ServidorException e) {
            // sin conexion la tabla queda vacia, pero la pestaña se abre igual
            categoriaList = FXCollections.observableArrayList();
            Avisos.error("No se pudieron cargar las categorías: " + e.getMessage());
        }
        categoriasFiltradas = new FilteredList<>(categoriaList, c -> true);
        catTable.setItems(categoriasFiltradas);
        btnSave.setOnAction(event -> guardarCategoria());
        btnDelete.setOnAction(event -> {
            eliminarCategoria();
        });
        btnClean.setOnAction(event -> limpiarCampos());
        catTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                llenarCampos(newValue);
            }
        });

        btnSearch.setOnAction(event -> buscarCategoria());
        txtSearch.setOnAction(event -> buscarCategoria());
    }

    private void limpiarCampos() {
        txtShowId.clear();
        txtShowDesc.clear();
        catTable.getSelectionModel().clearSelection();
    }
    private void llenarCampos(CategoriaDTO categoria) {
        txtShowId.setText(categoria.getId());
        txtShowDesc.setText(categoria.getDescripcion());
    }

    private void eliminarCategoria() {
        if (catTable.getSelectionModel().getSelectedItem() == null) {
            Avisos.error("Debe seleccionar una categoría primero.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar esta categoría?");
        alert.setContentText("Esta acción no se puede deshacer.");
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            CategoriaDTO categoriaSeleccionada = catTable.getSelectionModel().getSelectedItem();
            try {
                categoriaServicio.eliminarCategoria(categoriaSeleccionada.getId());
                categoriaList.remove(categoriaSeleccionada);
                limpiarBusqueda();
                limpiarCampos();
                Avisos.info("Categoría eliminada correctamente.");
            } catch (ServidorException e) {
                // error del servidor, como una categoria con recursos asociados, o falta de conexion
                Avisos.error(e.getMessage());
            }
        }
    }

    private void buscarCategoria() {
        String texto = txtSearch.getText().trim().toLowerCase();
        categoriasFiltradas.setPredicate(c ->
                texto.isEmpty() || c.getDescripcion().toLowerCase().contains(texto));
        limpiarCampos();

        if (texto.isEmpty()) {
            lblSearch.setText("");   // sin texto se muestran todas las categorías
        } else if (categoriasFiltradas.isEmpty()) {
            lblSearch.setText("Categoría no encontrada");
            lblSearch.setStyle("-fx-text-fill: red;");
        } else {
            lblSearch.setText("Categorías encontradas: " + categoriasFiltradas.size());
            lblSearch.setStyle("-fx-text-fill: green;");
        }
    }

    private void limpiarBusqueda() {
        txtSearch.clear();
        lblSearch.setText("");
        categoriasFiltradas.setPredicate(c -> true);   // la tabla vuelve a mostrar todas
    }

    public void guardarCategoria() {
        String id = txtShowId.getText().trim();
        String descripcion = txtShowDesc.getText().trim();
        if (descripcion.isEmpty()) {
            Avisos.error("La descripción es obligatoria.");
            return;
        }
        CategoriaDTO categoria = new CategoriaDTO(id, descripcion);
        try {
            int resultado = categoriaServicio.servicioCategorias(categoria);
            Avisos.info(resultado == 1 ? "Categoría agregada correctamente."
                    : "Categoría modificada correctamente.");
            categoriaList.setAll(categoriaServicio.obtenerCategorias());   // relee la lista desde el servidor
            limpiarBusqueda();
            limpiarCampos();
        } catch (ServidorException e) {
            Avisos.error(e.getMessage());
        }
    }

    private <T> void ajustarAltoFilas(TableView<T> tabla, ObservableList<T> lista) {
        tabla.fixedCellSizeProperty().unbind();

        if (lista == null || lista.isEmpty()) {
            tabla.setFixedCellSize(-1);
            return;
        }

        double alturaEstandar = 25.0;
        // tope para que pocas filas no se estiren hasta llenar toda la tabla, con una sola fila
        // del alto completo de la tabla el clic no la seleccionaba
        double alturaMaxima = 50.0;
        double altoEncabezado = 29.0;
        double altoDisponible = Math.max(0, (tabla.getHeight() > 0 ? tabla.getHeight() : tabla.getPrefHeight()) - altoEncabezado);
        double altoCalculado = altoDisponible / lista.size();

        if (altoCalculado > alturaMaxima) {
            tabla.setFixedCellSize(alturaMaxima);
        } else if (altoCalculado >= alturaEstandar) {
            tabla.fixedCellSizeProperty().bind(
                    tabla.heightProperty().subtract(altoEncabezado).divide(lista.size())
            );
        } else {
            tabla.setFixedCellSize(alturaEstandar);
        }
    }

    @FXML
    public void handleImprimir(ActionEvent event) {
        List<CategoriaDTO> visibles = catTable.getItems();
        if (visibles.isEmpty()) {
            Avisos.advertencia("No hay categorias para imprimir");
            return;
        }

        File destino = RutaDestino.pedirDestinoPdf("categorias.pdf",
                btnImprimir.getScene().getWindow());
        if (destino == null) {
            return;   // el usuario cancelo
        }

        List<String[]> filas = new ArrayList<>();
        for (CategoriaDTO categoria : visibles) {
            filas.add(new String[]{categoria.getId(), categoria.getDescripcion()});
        }

        try {
            ReportePdf.generar("Listado de Categorias",
                    new String[]{"Id", "Descripcion"}, filas, destino);
            Avisos.info("Reporte generado");
        } catch (RuntimeException e) {
            Avisos.error("No se pudo generar el reporte");
        }
    }


}