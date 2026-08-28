package SRR.Controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class SistemaReservasController {

    @FXML private TabPane tabPanePrincipal;
    @FXML private Tab tabReservas;
    @FXML private Tab tabFuncionarios;
    @FXML private Tab tabCategorias;
    @FXML private Tab tabRecursos;
    @FXML private Tab tabCalendarizacion;
    @FXML private Tab tabActividades;
    @FXML private Tab tabEstadisticas;

    // Almacena el rol seleccionado antes de cargar la escena
    private static String rolActual;

    public static void setRolActual(String rol) {
        rolActual = rol;
    }

    @FXML
    public void initialize() {
        if (rolActual != null) {
            configurarSegunRol(rolActual);
        }
    }

    public void configurarSegunRol(String rol) {
        if (rol == null || tabPanePrincipal == null) return;

        if (rol.equalsIgnoreCase("FUNCIONARIO")) {
            tabPanePrincipal.getTabs().removeAll(tabFuncionarios, tabCategorias, tabRecursos);
        } else if (rol.equalsIgnoreCase("ADMINISTRADOR") || rol.equalsIgnoreCase("ADMIN")) {
            tabPanePrincipal.getTabs().remove(tabReservas);
        }
    }
}