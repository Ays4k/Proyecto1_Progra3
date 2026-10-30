package SRR.Modelo;

import SRR.DTO.AlertaReservaDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// La lista de alertas que ve el usuario durante su sesión. No sabe nada de ventanas.
public class ListaAlertas {

    private final ObservableList<AlertaReservaDTO> alertas = FXCollections.observableArrayList();

    // La lista que muestra la tabla (si cambia, la tabla se actualiza sola)
    public ObservableList<AlertaReservaDTO> getAlertas() {
        return alertas;
    }

    // Agrega una alerta al final. Una alerta nula se ignora.
    public boolean agregar(AlertaReservaDTO alerta) {
        if (alerta == null) {
            return false;
        }
        return alertas.add(alerta);
    }

    // Quita la alerta SOLO de esta lista. Devuelve false si no estaba.
    public boolean descartar(AlertaReservaDTO alerta) {
        return alertas.remove(alerta);
    }

    public int cantidad() {
        return alertas.size();
    }

    // El texto que se ve en la tabla: "Encargado - Actividad"
    public static String resumen(AlertaReservaDTO alerta) {
        return alerta.getEncargado() + " - " + alerta.getActividad();
    }
}