package SRR.Comunicacion;

import SRR.DTO.AlertaReservaDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

// Hilo que escucha las alertas que manda el servidor de notificaciones.
// No toca la pantalla: cada alerta se la entrega a quien lo creó (alRecibir).
public class ClienteNotificaciones implements Runnable {

    private static final int TIEMPO_CONEXION_MS = 5000;

    private final String host;
    private final int puerto;
    private final String cedula;
    private final Consumer<AlertaReservaDTO> alRecibir;   // qué hacer con cada alerta
    private final Consumer<String> alFallar;              // qué hacer si se pierde la conexión

    private volatile boolean activo = true;
    private volatile Socket socket;

    public ClienteNotificaciones(String host, int puerto, String cedula,
                                 Consumer<AlertaReservaDTO> alRecibir, Consumer<String> alFallar) {
        this.host = host;
        this.puerto = puerto;
        this.cedula = cedula;
        this.alRecibir = alRecibir;
        this.alFallar = alFallar;
    }

    // Arranca el hilo. Es "daemon": no impide que la aplicación se cierre.
    public void iniciar() {
        Thread hilo = new Thread(this, "escucha-alertas");
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void run() {
        try {
            Socket nuevo = new Socket();
            nuevo.connect(new InetSocketAddress(host, puerto), TIEMPO_CONEXION_MS);
            socket = nuevo;

            // Primero el canal de salida con flush, después el de entrada (igual que el servidor)
            ObjectOutputStream salida = new ObjectOutputStream(nuevo.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(nuevo.getInputStream());

            // Saludo: la cédula del usuario
            salida.writeObject(cedula);
            salida.flush();

            // Ciclo de escucha: se queda esperando alertas hasta que lo detengan
            while (activo) {
                Object recibido = entrada.readObject();
                if (recibido instanceof AlertaReservaDTO alerta) {
                    alRecibir.accept(alerta);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (activo) {   // si lo detuvimos nosotros, no es un error
                alFallar.accept("Se perdió la conexión con el servidor de alertas: " + e.getMessage());
            }
        } finally {
            cerrarSocket();
        }
    }

    // Se llama al cerrar sesión: apaga el ciclo y cierra el socket
    public void detener() {
        activo = false;
        cerrarSocket();
    }

    private void cerrarSocket() {
        Socket actual = socket;
        if (actual != null) {
            try {
                actual.close();
            } catch (IOException ignorada) {
                // ya estaba cerrado
            }
        }
    }
}