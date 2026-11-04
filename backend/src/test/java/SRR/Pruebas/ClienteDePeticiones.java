package SRR.Pruebas;

import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteDePeticiones implements AutoCloseable {

    private final Socket socket;
    private final ObjectOutputStream salida;
    private final ObjectInputStream entrada;

    public ClienteDePeticiones(int puerto) throws IOException {
        socket = new Socket("localhost", puerto);
        socket.setSoTimeout(10000);
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
    }

    public Respuesta enviar(String metodo, Object... parametros) throws IOException, ClassNotFoundException {
        salida.writeObject(new Peticion(metodo, parametros));
        salida.reset();
        salida.flush();
        return (Respuesta) entrada.readObject();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignorada) {
        }
    }
}
