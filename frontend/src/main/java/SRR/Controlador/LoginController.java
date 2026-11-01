package SRR.Controlador;

import SRR.Comunicacion.Conexion;
import SRR.Comunicacion.ServidorException;
import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Proxy.LoginProxy;
import SRR.Singleton.MasterStageController;
import SRR.Utilidades.Avisos;
import SRR.Utilidades.Sesion;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;

    private final LoginProxy loginServicio = new LoginProxy();
    private static final String SIN_CONEXION = "No hay conexión con el servidor.";

    @FXML
    public void initialize() {
        // Al abrir el login se carga la configuracion. Si config.properties no se pudo
        // leer, se avisa y runLater hace que salga cuando la ventana ya esta visible.
        String aviso = Conexion.getInstance().tomarAvisoConfiguracion();
        if (aviso != null) {
            Platform.runLater(() -> Avisos.advertencia(aviso));
        }
        conectarAlServidor();
    }

    // El frontend se conecta con el backend al iniciar. Se hace en otro hilo porque, si el
    // servidor no responde, conectar puede tardar hasta el tiempo de espera y la ventana
    // se congelaria. Mientras no haya conexion se reintenta cada 3 segundos, y cuando el
    // backend se enciende el aviso del label se quita solo.
    private void conectarAlServidor() {
        Thread hilo = new Thread(() -> {
            Conexion conexion = Conexion.getInstance();
            while (!conexion.estaConectado()) {
                try {
                    conexion.conectar();
                    // se logro conectar, se quita el aviso si todavia esta en el label
                    Platform.runLater(() -> {
                        if (SIN_CONEXION.equals(lblMensaje.getText())) {
                            lblMensaje.setText("");
                        }
                    });
                } catch (ServidorException e) {
                    // solo se escribe si el label esta libre, para no tapar otro mensaje
                    Platform.runLater(() -> {
                        if (lblMensaje.getText().isEmpty()) {
                            lblMensaje.setText(SIN_CONEXION);
                        }
                    });
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
            }
        }, "conexion-inicial");
        hilo.setDaemon(true);   // no impide cerrar la aplicacion
        hilo.start();
    }

    @FXML
    public void handleIngresar(ActionEvent event) {
        lblMensaje.setText("");

        String user = txtUsuario.getText().trim();
        String clave = txtPassword.getText();

        if (user.isEmpty() || clave.isEmpty()) {
            lblMensaje.setText("Ingrese su id y su clave");
            return;
        }

        UsuarioDTO usuario;
        try {
            usuario = loginServicio.iniciarSesion(new LoginDTO(user, clave));
        } catch (ServidorException e) {
            // backend apagado, conexion perdida o error del servidor
            Avisos.error(e.getMessage());
            return;
        }

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
            lblMensaje.setText("Escriba su user primero");
            return;
        }
        CambiarClaveController.setIdPendiente(id);
        MasterStageController.getInstance().cambiarEscena("/Escenas/Cambiar Clave.fxml");
    }

    @FXML
    public void handleCancelar(ActionEvent event) {
        Platform.exit();
    }
}