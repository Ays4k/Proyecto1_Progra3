package SRR.Controlador;

import SRR.DTO.UsuarioDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import SRR.Servicio.UsuarioServicio;
import jdk.jfr.Event;

public class FuncionariosController {

    private final UsuarioServicio userService = new UsuarioServicio();

    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClean;
    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private ToggleGroup grupoRol;
    @FXML private TextField txtSearchId;
    @FXML private TextField txtSearchName;
    @FXML private Button btnSearch;
    @FXML private Button btnCleanSearch;

    @FXML private TableView<UsuarioDTO> tableUsers;
    @FXML private TableColumn<UsuarioDTO, String> colId;
    @FXML private TableColumn<UsuarioDTO, String> colName;
    @FXML private TableColumn<UsuarioDTO, String> colPhone;
    @FXML private TableColumn<UsuarioDTO, String> colRol;

    @FXML private ObservableList<UsuarioDTO> userList;
    @FXML
    public void initialize() {
        tableUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        userList = FXCollections.observableList(userService.obtenerUsuarios());
        tableUsers.setItems(userList);

        btnSearch.setOnAction(event -> buscarFuncionario());
        btnCleanSearch.setOnAction(event -> limpiarBusqueda());

        txtSearchId.textProperty().addListener((observable, oldValue, newValue) ->
            textosBusqueda(!newValue.isEmpty(), txtSearchName)
        );
        txtSearchName.textProperty().addListener((observable, oldValue, newValue) ->
            textosBusqueda(!newValue.isEmpty(), txtSearchId)
        );



        btnSave.setOnAction(event -> guardarFuncionario());
        btnClean.setOnAction(event -> limpiarCampos());
        btnDelete.setOnAction(event -> eliminarFuncionario());


        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                seleccionarFuncionario(newSelection);
            }
        });


    }

    private void textosBusqueda(boolean condition, TextField event) {
        event.setDisable(condition);
    }
    private void buscarFuncionario() {
        String searchId = txtSearchId.getText().trim();
        String searchName = txtSearchName.getText().trim();

        ObservableList<UsuarioDTO> filteredList = FXCollections.observableArrayList();
        if (searchId.isEmpty() && searchName.isEmpty()) {
            return;
        }
        if(!searchId.isEmpty() && !searchName.isEmpty()) {
            for (UsuarioDTO user : userList) {
                if (user.getId().contains(searchId) && user.getNombre().equalsIgnoreCase(searchName.toLowerCase())) {
                    filteredList.add(user);
                }
            }
            tableUsers.setItems(filteredList);
            return;
        }
        for (UsuarioDTO user : userList) {
            if (searchId.isEmpty() || user.getId().contains(searchId)) {
                if (searchName.isEmpty() || user.getNombre().toLowerCase().contains(searchName.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }

        tableUsers.setItems(filteredList);

    }
    private void limpiarBusqueda() {
        txtSearchId.clear();
        txtSearchName.clear();
        tableUsers.setItems(userList);
        txtSearchId.setDisable(false);
        txtSearchName.setDisable(false);
    }
    private void eliminarFuncionario() {
        if(txtId.getText().isEmpty()) {
            System.out.println("Error: No se ha seleccionado un funcionario.");
            return;
        }
        if(userService.eliminarUsuario(txtId.getText())) {
            userList.remove(tableUsers.getSelectionModel().getSelectedItem());
        }
        limpiarCampos();
    }
    private void seleccionarFuncionario(UsuarioDTO funcionario) {
        txtId.setText(funcionario.getId());
        txtId.setDisable(true); // Deshabilitar el campo de ID para evitar cambios
        txtName.setText(funcionario.getNombre());
        txtPhone.setText(funcionario.getTelefono());
        // Seleccionar el rol correspondiente
        for (Toggle toggle : grupoRol.getToggles()) {
            if (((RadioButton) toggle).getText().equals(funcionario.getRol())) {
                grupoRol.selectToggle(toggle);
                break;
            }
        }
    }
    private void guardarFuncionario() {
        if(grupoRol.getSelectedToggle() == null) {
            System.out.println("Error: No se ha seleccionado un rol.");
            return;
        }

        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String rol = ((RadioButton) grupoRol.getSelectedToggle()).getText();

        txtId.setDisable(false); // Habilitar el campo de ID para evitar cambios
        UsuarioDTO newUser = new UsuarioDTO(id, name, phone, null, rol);
        int resultado = userService.cambiosUsuario(newUser);

        if(resultado == 1) {
            userList.add(newUser);
        } else if(resultado == 2) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getId().equals(id)) {
                    userList.set(i, newUser);
                    break;
                }
            }
        }
        limpiarCampos();
    }

    private void limpiarCampos() {
        txtId.clear();
        txtName.clear();
        txtPhone.clear();
        grupoRol.selectToggle(null); // Deseleccionar cualquier rol seleccionado
        txtId.setDisable(false); // Habilitar el campo de ID para nuevas entradas
    }

}
