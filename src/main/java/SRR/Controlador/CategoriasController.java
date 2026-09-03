package SRR.Controlador;

import SRR.DTO.CategoriaDTO;
import SRR.Launch;
import SRR.Servicio.CategoriaServicio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import org.w3c.dom.Text;

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
    private final CategoriaServicio categoriaServicio = new CategoriaServicio();

    @FXML
    public void initialize() {
        catTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        categoriaList = FXCollections.observableList(categoriaServicio.obtenerCategorias());
        catTable.setItems(categoriaList);
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
        txtShowId.setDisable(false); // Habilitar el campo de ID para nuevas entradas
    }
    private void llenarCampos(CategoriaDTO categoria) {
        txtShowId.setText(categoria.getId());
        txtShowDesc.setText(categoria.getDescripcion());
        txtShowId.setDisable(true); // Deshabilitar el campo de ID para evitar cambios
    }

    private void eliminarCategoria() {
        if(catTable.getSelectionModel().getSelectedItem() == null) {
            //TODO Mostrar mensaje de error responsivo al usuario
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar esta categoría?");
        alert.setContentText("Esta acción no se puede deshacer.");
        alert.showAndWait();
        if(alert.getResult() == ButtonType.OK) {
            CategoriaDTO categoriaSeleccionada = catTable.getSelectionModel().getSelectedItem();
            categoriaServicio.eliminarCategoria(categoriaSeleccionada.getId());
            categoriaList.remove(categoriaSeleccionada);
            limpiarCampos();
            //TODO Mostrar mensaje de éxito responsivo al usuario donde se indique que tiene que
            //reasignar los recursos que estaban asignados a esta categoría
        }
    }

    private void buscarCategoria() {
        String textoBusqueda = txtSearch.getText().trim();
        if(textoBusqueda.isEmpty()) {
            lblSearch.setText("Ingrese un texto para buscar");
            lblSearch.setStyle("-fx-text-fill: red;");
            return;
        }
        for (CategoriaDTO categoria : categoriaList) {
            if (categoria.getDescripcion().toLowerCase().contains(textoBusqueda.toLowerCase())) {
                catTable.getSelectionModel().select(categoria);
                catTable.scrollTo(categoria);
                lblSearch.setText("Categoría encontrada");
                lblSearch.setStyle("-fx-text-fill: green;");
                return;
            }
        }
        lblSearch.setText("Categoría no encontrada");
        lblSearch.setStyle("-fx-text-fill: red;");

    }

    public void guardarCategoria() {
        String id = txtShowId.getText();
        String descripcion = txtShowDesc.getText();
        CategoriaDTO categoria = new CategoriaDTO(id, descripcion);
        int resultado = categoriaServicio.servicioCategorias(categoria);
        if(resultado == 1) {
            categoriaList.add(categoria);
        } else if (resultado == 2) {
            // Actualizar la categoría existente en la lista observable
            for (int i = 0; i < categoriaList.size(); i++) {
                if (categoriaList.get(i).getId().equals(id)) {
                    categoriaList.set(i, categoria);
                    break;
                }
            }

        }
        limpiarCampos();
    }


}
