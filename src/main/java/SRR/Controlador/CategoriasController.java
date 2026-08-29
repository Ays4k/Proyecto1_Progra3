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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        categoriaList = FXCollections.observableList(categoriaServicio.obtenerCategorias());
        catTable.setItems(categoriaList);
        btnSave.setOnAction(event -> guardarCategoria());
        btnDelete.setOnAction(event -> {
            System.out.println("Eliminar categoria");
        });
        btnClean.setOnAction(event -> {
            limpiarCampos();
        });
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
    }
    private void llenarCampos(CategoriaDTO categoria) {
        txtShowId.setText(categoria.getId());
        txtShowDesc.setText(categoria.getDescripcion());
        txtShowId.setDisable(true); // Deshabilitar el campo de ID para evitar cambios
    }

    private void buscarCategoria() {
        String textoBusqueda = txtSearch.getText().trim();
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
