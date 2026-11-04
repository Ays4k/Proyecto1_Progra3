package SRR.Pruebas;

import SRR.DTO.AlertaReservaDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteDeAlertas implements AutoCloseable {

    private final Socket socket;
    private final ObjectOutputStream salida;
    private final ObjectInputStream entrada;

    public ClienteDeAlertas(int puerto, Object saludo) throws IOException {
        socket = new Socket("localhost", puerto);
        socket.setSoTimeout(5000);
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
        if (saludo != null) {
            salida.writeObject(saludo);
            salida.flush();
        }
    }

    public void esperarHasta(int milisegundos) throws IOException {
        socket.setSoTimeout(milisegundos);
    }

    public Object leer() throws IOException, ClassNotFoundException {
        return entrada.readObject();
    }

    public AlertaReservaDTO recibir() throws IOException, ClassNotFoundException {
        return (AlertaReservaDTO) entrada.readObject();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignorada) {
        }
    }
}
