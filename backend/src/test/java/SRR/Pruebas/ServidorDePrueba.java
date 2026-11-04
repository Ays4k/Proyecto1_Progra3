package SRR.Pruebas;

import SRR.Servidor.AtencionCliente;
import SRR.Servidor.Enrutadores.Enrutador;
import SRR.Servidor.Enrutadores.EnrutadorPrincipal;
import SRR.Servidor.ServidorNotificaciones;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class ServidorDePrueba implements AutoCloseable {

    private final ServidorNotificaciones notificador = new ServidorNotificaciones();
    private final ServerSocket peticiones;
    private final ServerSocket alertas;

    public ServidorDePrueba() throws IOException {
        Enrutador enrutador = new EnrutadorPrincipal(notificador);
        peticiones = new ServerSocket(0);
        alertas = new ServerSocket(0);
        aceptar(peticiones, socket -> hilo(new AtencionCliente(socket, enrutador)));
        aceptar(alertas, socket -> hilo(() -> notificador.registrar(socket)));
    }

    public ServidorNotificaciones notificador() {
        return notificador;
    }

    public int puertoPeticiones() {
        return peticiones.getLocalPort();
    }

    public int puertoAlertas() {
        return alertas.getLocalPort();
    }

    public void esperarClientes(int cantidad) {
        long limite = System.currentTimeMillis() + 5000;
        while (notificador.cantidadClientes() != cantidad) {
            if (System.currentTimeMillis() > limite) {
                throw new IllegalStateException("Se esperaban " + cantidad + " clientes de alertas y hay "
                        + notificador.cantidadClientes());
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }

    private static void aceptar(ServerSocket servidor, Consumer<Socket> accion) {
        Thread aceptador = new Thread(() -> {
            try {
                while (true) {
                    accion.accept(servidor.accept());
                }
            } catch (IOException ignorada) {
            }
        });
        aceptador.setDaemon(true);
        aceptador.start();
    }

    private static void hilo(Runnable tarea) {
        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void close() {
        try {
            peticiones.close();
            alertas.close();
        } catch (IOException ignorada) {
        }
    }
}
