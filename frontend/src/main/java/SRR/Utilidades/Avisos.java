package SRR.Utilidades;

import javafx.scene.control.Alert;

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
        alerta.showAndWait();
    }
}