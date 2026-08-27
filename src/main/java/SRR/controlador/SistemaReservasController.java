package SRR.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class SistemaReservasController {

    // Son fx:id que agregué desde scene builder para identificar cada pestaña
    @FXML private TabPane tabPanePrincipal;
    @FXML private Tab tabReservas;
    @FXML private Tab tabFuncionarios;
    @FXML private Tab tabCategorias;
    @FXML private Tab tabRecursos;
    @FXML private Tab tabCalendarizacion;
    @FXML private Tab tabActividades;
    @FXML private Tab tabEstadisticas;

    @FXML
    public void initialize() {
        // Inicialización base de componentes
    }


    // Oculta o muestra pestañas dependiendo del rol recibido del Login
    public void configurarSegunRol(String rol) {
        if (rol == null || tabPanePrincipal == null) return;

        if (rol.equalsIgnoreCase("FUNCIONARIO")) {
            // Remueve las pestañas administrativas
            tabPanePrincipal.getTabs().removeAll(tabFuncionarios, tabCategorias, tabRecursos);
        } else if (rol.equalsIgnoreCase("ADMINISTRADOR") || rol.equalsIgnoreCase("ADMIN")) {
            // Remueve la pestaña exclusiva de reservas del funcionario
            tabPanePrincipal.getTabs().remove(tabReservas);
        }
    }


}