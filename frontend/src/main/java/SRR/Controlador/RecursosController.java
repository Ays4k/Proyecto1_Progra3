package SRR.Controlador;

import SRR.Comunicacion.ServidorException;
import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.Proxy.CategoriaProxy;
import SRR.Proxy.RecursoProxy;
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

    private final RecursoProxy recursoServicio = new RecursoProxy();
    private final CategoriaProxy categoriaServicio = new CategoriaProxy();



    @FXML
    public void initialize() {
        txtId.setDisable(true);   // el id lo genera la base, solo se muestra
        txtId.setPromptText("Automático");
        tablaRecursos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        // la celda guarda el id de la categoria, pero la tabla muestra su nombre
        colCategoria.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String idCat, boolean empty) {
                super.updateItem(idCat, empty);
                setText(empty || idCat == null ? null : nombreDeCategoria(idCat));
            }
        });
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
        try {
            recursoList = FXCollections.observableArrayList(recursoServicio.obtenerRecursos());
        } catch (ServidorException e) {
            // sin conexion la tabla queda vacia, pero la pestaña se abre igual
            recursoList = FXCollections.observableArrayList();
            Avisos.error("No se pudieron cargar los recursos: " + e.getMessage());
        }
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

    private String nombreDeCategoria(String idCat) {
        if (categoriaList != null) {
            for (CategoriaDTO cat : categoriaList) {
                if (cat.getId().equals(idCat)) {
                    return cat.getDescripcion();
                }
            }
        }
        return idCat;   // si no la encuentra, muestra el numero
    }

    private void cargarCategorias() {
        try {
            categoriaList = FXCollections.observableArrayList(categoriaServicio.obtenerCategorias());
        } catch (ServidorException e) {
            // si falla se quedan las categorias que ya estaban cargadas
            Avisos.error("No se pudieron cargar las categorías: " + e.getMessage());
            if (categoriaList == null) {
                categoriaList = FXCollections.observableArrayList();
            }
        }

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

            // se relee la lista del servidor porque el id del recurso nuevo lo genera la base
            // y no vuelve en el objeto que se envio por el socket
            recursoList.setAll(recursoServicio.obtenerRecursos());

            Avisos.info(resultado == 1 ? "Recurso agregado correctamente."
                    : "Recurso modificado correctamente.");

            // solo se limpian los campos si la operación
            // fue exitosa.

            limpiarCampos();

        } catch (ServidorException e) {
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

            } catch (ServidorException e) {
                // error del servidor, como un recurso en una reserva activa, o falta de conexion
                Avisos.error(e.getMessage());
            }
        }
    }

    private void buscarRecursos() {
        CategoriaDTO catFiltro = cbFiltroCategoria.getValue();
        String idCat = (catFiltro != null) ? catFiltro.getId() : "";
        String descFiltro = txtBuscarDesc.getText().trim();

        try {
            ObservableList<RecursoDTO> filtrados = FXCollections.observableArrayList(
                    recursoServicio.filtrarRecursos(idCat, descFiltro)
            );
            tablaRecursos.setItems(filtrados);
        } catch (ServidorException e) {
            Avisos.error("No se pudo buscar: " + e.getMessage());
        }
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
            // el nombre de la categoria se toma de la lista ya cargada, igual que en la tabla,
            // asi no se hace una peticion al servidor por cada recurso
            filas.add(new String[]{recurso.getId(), nombreDeCategoria(recurso.getIdCategoria()),
                    recurso.getDescripcion()});
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