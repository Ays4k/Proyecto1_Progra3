package SRR.Controlador;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.Excepciones.RecursoException;
import SRR.Servicio.CategoriaServicio;
import SRR.Servicio.RecursoServicio;
import SRR.Utilidades.Avisos;
import SRR.Utilidades.ReportePdf;
import SRR.Utilidades.RutaDestino;
import javafx.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class RecursosController {

    @FXML private ComboBox<CategoriaDTO> cbFiltroCategoria;
    @FXML private TextField txtBuscarDesc;
    @FXML private Button btnBuscar;
    @FXML private Button btnImprimir;

    @FXML private TextField txtId;
    @FXML private ComboBox<CategoriaDTO> cbFormCategoria;
    @FXML private TextField txtDesc;
    @FXML private Button btnGuardar;
    @FXML private Button btnBorrar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<RecursoDTO> tablaRecursos;
    @FXML private TableColumn<RecursoDTO, String> colId;
    @FXML private TableColumn<RecursoDTO, String> colCategoria;
    @FXML private TableColumn<RecursoDTO, String> colDescripcion;

    private ObservableList<RecursoDTO> recursoList;
    private ObservableList<CategoriaDTO> categoriaList;

    private final RecursoServicio recursoServicio = new RecursoServicio();
    private final CategoriaServicio categoriaServicio = new CategoriaServicio();



    @FXML
    public void initialize() {
        tablaRecursos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Centrar texto de las columnas
        colId.setStyle("-fx-alignment: CENTER;");
        colCategoria.setStyle("-fx-alignment: CENTER;");
        colDescripcion.setStyle("-fx-alignment: CENTER;");

        // Ajustar alto de filas cuando cambien los ítems, la lista o la altura de la tabla
        tablaRecursos.heightProperty().addListener((obs, oldH, newH) -> ajustarAltoFilas(tablaRecursos, tablaRecursos.getItems()));
        tablaRecursos.itemsProperty().addListener((obs, oldList, newList) -> {
            if (newList != null) {
                ajustarAltoFilas(tablaRecursos, newList);
                newList.addListener((javafx.collections.ListChangeListener.Change<? extends RecursoDTO> c) ->
                        ajustarAltoFilas(tablaRecursos, newList)
                );
            }
        });

        cbFormCategoria.setOnShowing(event->{
            cargarCategorias();
        });
        cbFiltroCategoria.setOnShowing(event->{
            cargarCategorias();
        });
        recursoList = FXCollections.observableArrayList(recursoServicio.obtenerRecursos());
        tablaRecursos.setItems(recursoList);

        btnGuardar.setOnAction(event -> guardarRecurso());
        btnBorrar.setOnAction(event -> eliminarRecurso());
        btnLimpiar.setOnAction(event -> limpiarCampos());
        btnBuscar.setOnAction(event -> buscarRecursos());

        tablaRecursos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                seleccionarRecurso(newSel);
            }
        });
        cargarCategorias();
    }

    private void cargarCategorias() {
        categoriaList = FXCollections.observableArrayList(categoriaServicio.obtenerCategorias());

        StringConverter<CategoriaDTO> converter = new StringConverter<>() {
            @Override
            public String toString(CategoriaDTO cat) {
                return cat != null ? cat.getDescripcion() : "";
            }

            @Override
            public CategoriaDTO fromString(String string) {
                return null;
            }
        };

        cbFiltroCategoria.setConverter(converter);
        cbFormCategoria.setConverter(converter);

        cbFiltroCategoria.setItems(categoriaList);
        cbFormCategoria.setItems(categoriaList);
    }

    private void guardarRecurso() {
        String id = txtId.getText().trim();
        String descripcion = txtDesc.getText().trim();
        CategoriaDTO categoriaSeleccionada = cbFormCategoria.getValue();

           // si no se selecciono una categoría se envia null
           // RecursoLogica será responsable de rechazarlo

        String idCategoria = categoriaSeleccionada == null
                ? null : categoriaSeleccionada.getId();

        RecursoDTO nuevoRecurso =
                new RecursoDTO(
                        id,
                        descripcion,
                        idCategoria
                );

        try {
            int resultado =
                    recursoServicio.guardarRecurso(nuevoRecurso);

            if (resultado == 1) {
                recursoList.add(nuevoRecurso);

                Avisos.info("Recurso agregado correctamente.");

            } else if (resultado == 2) {
                for (int i = 0; i < recursoList.size(); i++) {
                    if (recursoList.get(i)
                            .getId()
                            .equals(nuevoRecurso.getId())) {

                        recursoList.set(i, nuevoRecurso);
                        break;
                    }
                }

                Avisos.info("Recurso modificado correctamente.");
            }

              // solo se limpian los campos si la operación
              // fue exitosa.

            limpiarCampos();

        } catch (RecursoException e) {
            Avisos.error(e.getMessage());
        }
    }

    private void eliminarRecurso() {
        RecursoDTO seleccionado = tablaRecursos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Avisos.error("Debe seleccionar un recurso primero.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de eliminación");
        alert.setHeaderText("¿Está seguro de eliminar el recurso?");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.OK) {

            try {

                 // el controlador solamente solicita la eliminacion
                 // RecursoLogica decide si puede hacerse

                recursoServicio.eliminarRecurso(seleccionado.getId());

                recursoList.remove(seleccionado);
                limpiarCampos();

                Avisos.info(
                        "Recurso eliminado correctamente."
                );

            } catch (RecursoException e) {
                Avisos.error(e.getMessage());
            }
        }
    }

    private void buscarRecursos() {
        CategoriaDTO catFiltro = cbFiltroCategoria.getValue();
        String idCat = (catFiltro != null) ? catFiltro.getId() : "";
        String descFiltro = txtBuscarDesc.getText().trim();

        ObservableList<RecursoDTO> filtrados = FXCollections.observableArrayList(
                recursoServicio.filtrarRecursos(idCat, descFiltro)
        );
        tablaRecursos.setItems(filtrados);
    }

    private void seleccionarRecurso(RecursoDTO recurso) {
        txtId.setText(recurso.getId());
        txtId.setDisable(true);
        txtDesc.setText(recurso.getDescripcion());

        for (CategoriaDTO cat : categoriaList) {
            if (cat.getId().equals(recurso.getIdCategoria())) {
                cbFormCategoria.setValue(cat);
                break;
            }
        }
    }

    private void limpiarCampos() {
        txtId.clear();
        txtId.setDisable(false);
        txtDesc.clear();
        cbFormCategoria.setValue(null);
        cbFiltroCategoria.setValue(null);
        txtBuscarDesc.clear();
        tablaRecursos.setItems(recursoList);
        tablaRecursos.getSelectionModel().clearSelection();
    }

    private <T> void ajustarAltoFilas(TableView<T> tabla, ObservableList<T> lista) {
        tabla.fixedCellSizeProperty().unbind();

        if (lista == null || lista.isEmpty()) {
            tabla.setFixedCellSize(-1);
            return;
        }

        double alturaEstandar = 25.0;
        double altoEncabezado = 29.0;
        double altoDisponible = Math.max(0, (tabla.getHeight() > 0 ? tabla.getHeight() : tabla.getPrefHeight()) - altoEncabezado);
        double altoCalculado = altoDisponible / lista.size();

        if (altoCalculado >= alturaEstandar) {
            tabla.fixedCellSizeProperty().bind(
                    tabla.heightProperty().subtract(altoEncabezado).divide(lista.size())
            );
        } else {
            tabla.setFixedCellSize(alturaEstandar);
        }
    }

    @FXML
    public void handleImprimir(ActionEvent event) {
        List<RecursoDTO> visibles = tablaRecursos.getItems();
        if (visibles.isEmpty()) {
            Avisos.advertencia("No hay recursos para imprimir");
            return;
        }

        File destino = RutaDestino.pedirDestinoPdf("recursos.pdf",
                btnImprimir.getScene().getWindow());
        if (destino == null) {
            return;
        }

        List<String[]> filas = new ArrayList<>();
        for (RecursoDTO recurso : visibles) {
            CategoriaDTO categoria = categoriaServicio.buscarPorId(recurso.getIdCategoria());
            String nombreCategoria = categoria == null
                    ? recurso.getIdCategoria() : categoria.getDescripcion();
            filas.add(new String[]{recurso.getId(), nombreCategoria, recurso.getDescripcion()});
        }

        try {
            ReportePdf.generar("Listado de Recursos",
                    new String[]{"Id", "Categoria", "Descripcion"}, filas, destino);
            Avisos.info("Reporte generado");
        } catch (RuntimeException e) {
            Avisos.error("No se pudo generar el reporte");
        }
    }
}