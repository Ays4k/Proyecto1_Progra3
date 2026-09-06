package SRR.Utilidades;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class RutaDestino {

    private RutaDestino() {

    }

    //Abre la ventana de guardar y devuelve el archivo elegido,
    //o null si el usuario cancelo.

    public static File pedirDestinoPdf(String nombreSugerido, Window ventana) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar reporte");
        selector.setInitialFileName(nombreSugerido);
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return selector.showSaveDialog(ventana);
    }
}