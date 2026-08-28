package SRR.Controlador;

import SRR.Singleton.MasterStageController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    @FXML
    public void handleIngresar(ActionEvent event) {
        System.out.println(">>> BOTÓN INGRESAR PRESIONADO <<<");

        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        String rolDetectado = null;

        if (usuario.equalsIgnoreCase("admin")) {
            rolDetectado = "ADMINISTRADOR";
        } else if (usuario.equalsIgnoreCase("func")) {
            rolDetectado = "FUNCIONARIO";
        }

        if (rolDetectado != null) {
            abrirSistemaPrincipal(rolDetectado);
        } else {
            System.out.println("Usuario no reconocido. Usa 'admin' o 'func' para probar.");
        }
    }

    private void abrirSistemaPrincipal(String rol) {
        // Asignamos el rol al controlador receptor
        SistemaReservasController.setRolActual(rol);
        MasterStageController.getInstance().cambiarEscena("/Escenas/Sistema de Reservas.fxml");
    }
}