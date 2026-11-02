package SRR.Comunicacion;

import SRR.DTO.AlertaReservaDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

// Hilo que escucha las alertas que manda el servidor de notificaciones.
// Si la conexión se cae, intenta reconectarse solo hasta que lo detengan (al cerrar sesión).
// No toca la pantalla: cada alerta se la entrega a quien lo creó (alRecibir).
public class ClienteNotificaciones implements Runnable {

    private static final int TIEMPO_CONEXION_MS = 5000;
    private static final int ESPERA_REINTENTO_MS = 3000;

    private final String host;
    private final int puerto;
    private final String cedula;
    private final Consumer<AlertaReservaDTO> alRecibir;   // qué hacer con cada alerta
    private final Consumer<String> alFallar;              // qué hacer cuando se pierde la conexión
    private final int esperaReintentoMs;

    private volatile boolean activo = true;
    private volatile Socket socket;
    private volatile Thread hilo;
    private boolean falloAvisado = false;   // solo lo usa el hilo de escucha

    public ClienteNotificaciones(String host, int puerto, String cedula,
                                 Consumer<AlertaReservaDTO> alRecibir, Consumer<String> alFallar) {
        this(host, puerto, cedula, alRecibir, alFallar, ESPERA_REINTENTO_MS);
    }

    public ClienteNotificaciones(String host, int puerto, String cedula,
                                 Consumer<AlertaReservaDTO> alRecibir, Consumer<String> alFallar,
                                 int esperaReintentoMs) {
        this.host = host;
        this.puerto = puerto;
        this.cedula = cedula;
        this.alRecibir = alRecibir;
        this.alFallar = alFallar;
        this.esperaReintentoMs = esperaReintentoMs;
    }

    // Arranca el hilo. Es "daemon": no impide que la aplicación se cierre.
    public void iniciar() {
        hilo = new Thread(this, "escucha-alertas");
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void run() {
        while (activo) {
            try {
                conectarYEscuchar();   // solo termina cuando la conexión se cae o la cierran
            } catch (IOException | ClassNotFoundException e) {
                // avisa solo la primera vez, no en cada reintento
                if (activo && !falloAvisado) {
                    falloAvisado = true;
                    alFallar.accept("Se perdió la conexión con el servidor de alertas: " + e.getMessage()
                            + ". Se intentará reconectar automáticamente.");
                }
            } finally {
                cerrarSocket();
            }

            if (!activo) {
                break;   // cerraron sesión: no reconectar
            }
            try {
                Thread.sleep(esperaReintentoMs);   // espera antes de volver a intentar
            } catch (InterruptedException e) {
                break;   // detener() nos despertó: salimos
            }
        }
    }

    private void conectarYEscuchar() throws IOException, ClassNotFoundException {
        Socket nuevo = new Socket();
        socket = nuevo;   // se guarda antes de conectar para que detener() también pueda cortarlo
        nuevo.connect(new InetSocketAddress(host, puerto), TIEMPO_CONEXION_MS);

        // Primero el canal de salida con flush, después el de entrada (igual que el servidor)
        ObjectOutputStream salida = new ObjectOutputStream(nuevo.getOutputStream());
        salida.flush();
        ObjectInputStream entrada = new ObjectInputStream(nuevo.getInputStream());

        // Saludo: la cédula del usuario
        salida.writeObject(cedula);
        salida.flush();

        falloAvisado = false;   // conectó bien: si se vuelve a caer, se avisa de nuevo

        // Ciclo de escucha: se queda esperando alertas hasta que lo detengan o se caiga
        while (activo) {
            Object recibido = entrada.readObject();
            if (recibido instanceof AlertaReservaDTO alerta) {
                alRecibir.accept(alerta);
            }
        }
    }

    // Se llama al cerrar sesión: apaga el ciclo, cierra el socket y despierta la espera
    public void detener() {
        activo = false;
        cerrarSocket();
        Thread actual = hilo;
        if (actual != null) {
            actual.interrupt();
        }
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