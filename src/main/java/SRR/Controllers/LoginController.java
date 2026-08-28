package SRR.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException; // Entrada/Salida

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    @FXML
    public void handleIngresar(ActionEvent event) {
        System.out.println(">>> BOTÓN INGRESAR PRESIONADO <<<"); // Mensaje de prueba

        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        // Validación temporal de prueba (luego se usará la base de datos)
        String rolDetectado = null;

        if (usuario.equalsIgnoreCase("admin")) {
            rolDetectado = "ADMINISTRADOR";
        } else if (usuario.equalsIgnoreCase("func")) {
            rolDetectado = "FUNCIONARIO";
        }

        if (rolDetectado != null) {
            abrirSistemaPrincipal(event, rolDetectado);
        } else {
            System.out.println("Usuario no reconocido. Usa 'admin' o 'func' para probar.");
        }
    }

    private void abrirSistemaPrincipal(ActionEvent event, String rol) {
        try {
            // Carga la vista principal del sistema
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Sistema de Reservas.fxml"));
            Parent root = loader.load();

            // Obtiene el controlador de la ventana principal y le envía el rol
            SistemaReservasController controller = loader.getController();
            controller.configurarSegunRol(rol);

            // Cambia la escena a la ventana principal
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}