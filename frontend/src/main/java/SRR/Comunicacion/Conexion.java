package SRR.Comunicacion;

import SRR.DTO.Peticion;
import SRR.DTO.Protocolo;
import SRR.DTO.Respuesta;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// Singleton con el unico socket de peticiones del frontend (puerto 5000 por defecto).
// Todos los proxies envian por aqui. Los metodos son synchronized para que
// salga una sola peticion a la vez y cada respuesta le llegue a quien la pidio.
public class Conexion {

    private static Conexion instancia;

    private String host;
    private int puerto;
    private int puertoNotificaciones;
    private int tiempoEspera;      // milisegundos que se espera una respuesta normal
    private int tiempoEsperaIa;    // milisegundos para generarReservaAi (Gemini tarda mas)

    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    private Conexion() {
        cargarConfiguracion();
    }

    public static synchronized Conexion getInstance() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    // Busca primero un config.properties junto al programa (para cambiar la IP sin recompilar),
    // y si no existe usa el de resources. Si falta algun valor, usa el de Protocolo.
    private void cargarConfiguracion() {
        Properties propiedades = new Properties();
        Path externo = Path.of("config.properties");
        try {
            if (Files.exists(externo)) {
                try (InputStream in = Files.newInputStream(externo)) {
                    propiedades.load(in);
                }
            } else {
                try (InputStream in = Conexion.class.getResourceAsStream("/config.properties")) {
                    if (in != null) {
                        propiedades.load(in);
                    }
                }
            }
        } catch (IOException e) {
            // si no se puede leer el archivo se quedan los valores por defecto
        }

        host = propiedades.getProperty("servidor.host", Protocolo.HOST_POR_DEFECTO).trim();
        puerto = leerEntero(propiedades, "servidor.puerto", Protocolo.PUERTO_PETICIONES);
        puertoNotificaciones = leerEntero(propiedades, "servidor.puertoNotificaciones", Protocolo.PUERTO_NOTIFICACIONES);
        tiempoEspera = leerEntero(propiedades, "servidor.tiempoEspera", 10000);
        tiempoEsperaIa = leerEntero(propiedades, "servidor.tiempoEsperaIa", 60000);
    }

    private int leerEntero(Properties propiedades, String clave, int porDefecto) {
        try {
            return Integer.parseInt(propiedades.getProperty(clave, "").trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    // Para las pruebas: apunta a otro servidor (por ejemplo el ServidorFalso)
    // sin tener que cambiar config.properties.
    public synchronized void configurar(String host, int puerto, int tiempoEspera) {
        cerrar();
        this.host = host;
        this.puerto = puerto;
        this.tiempoEspera = tiempoEspera;
    }

    public synchronized void conectar() {
        if (estaConectado()) {
            return;
        }
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, puerto), tiempoEspera);
            socket.setSoTimeout(tiempoEspera);
            // Primero se crea el de salida y se hace flush. Si ambos lados crean primero
            // el ObjectInputStream, los dos se quedan esperando el encabezado del otro.
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            cerrar();
            throw new ServidorException("No se pudo conectar con el servidor (" + host + ":" + puerto
                    + "). Verifique que el backend esté encendido.", e);
        }
    }

    public synchronized Respuesta enviar(Peticion peticion) {
        return enviar(peticion, tiempoEspera);
    }

    public synchronized Respuesta enviar(Peticion peticion, int tiempoMs) {
        conectar();   // si se habia perdido la conexion, intenta abrirla de nuevo
        try {
            socket.setSoTimeout(tiempoMs);
            salida.writeObject(peticion);
            salida.flush();
            // Olvida los objetos ya enviados. Sin esto, si se reenvia el mismo DTO
            // despues de modificarlo, el backend lo recibe con los datos viejos.
            salida.reset();

            Object recibido = entrada.readObject();
            if (!(recibido instanceof Respuesta respuesta)) {
                cerrar();
                throw new ServidorException("El servidor envió una respuesta inválida.");
            }
            return respuesta;
        } catch (SocketTimeoutException e) {
            // Se cierra porque la respuesta podria llegar tarde y confundirse
            // con la de la siguiente peticion.
            cerrar();
            throw new ServidorException("El servidor tardó demasiado en responder. Intente de nuevo.", e);
        } catch (IOException | ClassNotFoundException e) {
            cerrar();
            throw new ServidorException("Se perdió la conexión con el servidor.", e);
        }
    }

    public synchronized boolean estaConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void cerrar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // ya estaba cerrado, no hay nada que hacer
        }
        socket = null;
        salida = null;
        entrada = null;
    }

    public String getHost() {
        return host;
    }

    public int getPuertoNotificaciones() {
        return puertoNotificaciones;
    }

    public int getTiempoEsperaIa() {
        return tiempoEsperaIa;
    }
}