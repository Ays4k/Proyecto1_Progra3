package SRR.Controlador;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Servicio.UsuarioServicio;
import SRR.Singleton.MasterStageController;
import SRR.utilidades.Sesion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();

    @FXML
    public void handleIngresar(ActionEvent event) {
        lblMensaje.setText("");

        String id = txtUsuario.getText().trim();
        String clave = txtPassword.getText();

        if (id.isEmpty() || clave.isEmpty()) {
            lblMensaje.setText("Ingrese su id y su clave");
            return;
        }

        UsuarioDTO usuario = usuarioServicio.iniciarSesion(new LoginDTO(id, clave));

        if (usuario == null) {
            lblMensaje.setText("Usuario o clave incorrectos");
            txtPassword.clear();
            return;
        }

        Sesion.iniciar(usuario);
        SistemaReservasController.setRolActual(usuario.getRol());
        MasterStageController.getInstance().cambiarEscena("/Escenas/Sistema de Reservas.fxml");
    }

    @FXML
    public void handleCambiar(ActionEvent event) {
        String id = txtUsuario.getText().trim();
        if (id.isEmpty()) {
            lblMensaje.setText("Escriba su id primero");
            return;
        }
        CambiarClaveController.setIdPendiente(id);
        MasterStageController.getInstance().cambiarEscena("/Escenas/Cambiar Clave.fxml");
    }
}