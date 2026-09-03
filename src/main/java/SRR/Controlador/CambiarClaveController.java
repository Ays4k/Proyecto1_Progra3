package SRR.Controlador;

import SRR.Servicio.UsuarioServicio;
import SRR.Singleton.MasterStageController;
import SRR.Utilidades.Sesion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class CambiarClaveController {

    @FXML private PasswordField txtActual;
    @FXML private PasswordField txtNueva;
    @FXML private PasswordField txtConfirmar;
    @FXML private Label lblMensaje;

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();

    // El id se recibe antes de mostrar la pantalla, porque este formulario
    // no lo pide. Viene del login o de la sesion activa.
    private static String idPendiente;

    public static void setIdPendiente(String id) {
        idPendiente = id;
    }

    @FXML
    public void handleAceptar(ActionEvent event) {
        lblMensaje.setText("");

        String id = Sesion.getId() != null ? Sesion.getId() : idPendiente;

        try {
            usuarioServicio.cambiarClave(id, txtActual.getText(),
                    txtNueva.getText(), txtConfirmar.getText());
            lblMensaje.setTextFill(javafx.scene.paint.Color.GREEN);
            lblMensaje.setText("Clave cambiada correctamente");
            limpiar();
        } catch (IllegalArgumentException e) {
            lblMensaje.setTextFill(javafx.scene.paint.Color.RED);
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void handleCancelar(ActionEvent event) {
        MasterStageController.getInstance().volverEscenaAnterior();
    }

    private void limpiar() {
        txtActual.clear();
        txtNueva.clear();
        txtConfirmar.clear();
    }
}