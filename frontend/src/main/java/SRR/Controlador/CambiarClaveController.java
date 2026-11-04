package SRR.Controlador;

import SRR.Comunicacion.ServidorException;
import SRR.Proxy.LoginProxy;
import SRR.Singleton.MasterStageController;
import SRR.Utilidades.Avisos;
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

    private final LoginProxy loginServicio = new LoginProxy();

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
            loginServicio.cambiarClave(id, txtActual.getText(),
                    txtNueva.getText(), txtConfirmar.getText());
            limpiar();
            Avisos.info("Clave cambiada correctamente. Inicie sesión con su nueva clave.");
            MasterStageController.getInstance().volverEscenaAnterior();
        } catch (ServidorException e) {
            if (e.esProblemaDeConexion()) {
                // con causa es un problema de conexion, el mensaje es largo para el label
                Avisos.error(e.getMessage());
            } else {
                // sin causa es una validacion del backend, como "Las claves nuevas no coinciden"
                String mensaje = e.getMessage();
                if ("ID o clave actual incorrectos".equals(mensaje)) {
                    // aqui el id ya se escribio en el login, solo puede fallar la clave actual
                    mensaje = "La clave actual es incorrecta";
                }
                lblMensaje.setTextFill(javafx.scene.paint.Color.RED);
                lblMensaje.setText(mensaje);
            }
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