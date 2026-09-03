package SRR.Controlador;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.Servicio.CategoriaServicio;
import SRR.Servicio.RecursoServicio;

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

        cargarCategorias();

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
        if (cbFormCategoria.getValue() == null || txtId.getText().trim().isEmpty()) {
            System.out.println("Error: Complete los campos requeridos.");
            return;
        }

        String id = txtId.getText().trim();
        String descripcion = txtDesc.getText().trim();
        String idCategoria = cbFormCategoria.getValue().getId();

        RecursoDTO nuevoRecurso = new RecursoDTO(id, descripcion, idCategoria);
        int resultado = recursoServicio.servicioRecursos(nuevoRecurso);

        if (resultado == 1) {
            recursoList.add(nuevoRecurso);
        } else if (resultado == 2) {
            for (int i = 0; i < recursoList.size(); i++) {
                if (recursoList.get(i).getId().equals(id)) {
                    recursoList.set(i, nuevoRecurso);
                    break;
                }
            }
        }
        limpiarCampos();
    }

    private void eliminarRecurso() {
        RecursoDTO seleccionado = tablaRecursos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            System.out.println("Error: No se ha seleccionado un recurso.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de eliminación");
        alert.setHeaderText("¿Está seguro de eliminar el recurso?");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.OK) {
            if (recursoServicio.eliminarRecurso(seleccionado.getId())) {
                recursoList.remove(seleccionado);
                limpiarCampos();
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
}