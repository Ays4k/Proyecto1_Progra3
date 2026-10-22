package SRR.Controlador;

import SRR.Singleton.MasterStageController;
import SRR.Utilidades.Sesion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML private Button btnCerrarSesion;

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

    @FXML
    public void handleCerrarSesion(ActionEvent event) {
        // 1. Limpia la sesión activa
        Sesion.cerrar();

        // 2. Reinicia el estado del rol
        rolActual = null;

        // 3. Redirige a la pantalla de Login
        MasterStageController.getInstance().cambiarEscena("/Escenas/Login.fxml");
    }
}