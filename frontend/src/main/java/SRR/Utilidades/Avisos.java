package SRR.Utilidades;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

public class Avisos {

    private Avisos() {
    }

    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, mensaje);
    }

    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, mensaje);
    }

    public static void advertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, mensaje);
    }

    private static void mostrar(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        // sin esto JavaFX no agranda la ventana segun el texto y corta los mensajes largos con "..."
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.showAndWait();
    }
}