package SRR.Servidor;

import SRR.DTO.AlertaReservaDTO;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClienteAlerta {

    private final String idUsuario;
    private final ObjectOutputStream salida;

    public ClienteAlerta(String idUsuario, ObjectOutputStream salida) {
        this.idUsuario = idUsuario;
        this.salida = salida;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void enviar(AlertaReservaDTO alerta) throws IOException {
        salida.writeObject(alerta);
        salida.reset();
        salida.flush();
    }

    public void cerrar() {
        try {
            salida.close();
        } catch (IOException ignorada) {
        }
    }
}
