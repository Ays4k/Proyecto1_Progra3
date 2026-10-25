package SRR.Servidor;

import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;
import SRR.Servidor.Enrutadores.Enrutador;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class AtencionCliente implements Runnable {

    private final Socket cliente;
    private final Enrutador enrutador;

    public AtencionCliente(Socket cliente, Enrutador enrutador) {
        this.cliente = cliente;
        this.enrutador = enrutador;
    }

    @Override
    public void run() {
        System.out.println("Cliente conectado: " + cliente.getRemoteSocketAddress());
        try (cliente) {
            ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());

            while (true) {
                Peticion p = (Peticion) in.readObject();
                Respuesta r = procesar(p);
                out.writeObject(r);
                out.reset();
                out.flush();
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Cliente desconectado: " + cliente.getRemoteSocketAddress());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error de conexión con " + cliente.getRemoteSocketAddress()
                    + ": " + e.getMessage());
        }
    }

    private Respuesta procesar(Peticion p) {
        try {
            return enrutador.atender(p);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Error interno del servidor";
            return Respuesta.error(msg);
        }
    }
}
