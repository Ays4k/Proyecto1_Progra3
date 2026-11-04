package SRR.Servidor;

import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;
import SRR.Servidor.Enrutadores.Enrutador;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(20)
class AtencionClienteTest {

    private interface Comportamiento {
        Respuesta aplicar(Peticion peticion) throws Exception;
    }

    private static class EnrutadorFalso implements Enrutador {
        private final Comportamiento comportamiento;

        EnrutadorFalso(Comportamiento comportamiento) {
            this.comportamiento = comportamiento;
        }

        @Override
        public boolean maneja(String metodo) {
            return true;
        }

        @Override
        public Respuesta atender(Peticion peticion) throws Exception {
            return comportamiento.aplicar(peticion);
        }
    }

    private ServerSocket servidor;
    private Socket cliente;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private Thread hiloDeAtencion;

    private void conectar(Comportamiento comportamiento) throws IOException {
        servidor = new ServerSocket(0);
        hiloDeAtencion = new Thread(() -> {
            try {
                Socket socket = servidor.accept();
                new AtencionCliente(socket, new EnrutadorFalso(comportamiento)).run();
            } catch (IOException ignorada) {
            }
        });
        hiloDeAtencion.setDaemon(true);
        hiloDeAtencion.start();

        cliente = new Socket("localhost", servidor.getLocalPort());
        cliente.setSoTimeout(5000);
        salida = new ObjectOutputStream(cliente.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(cliente.getInputStream());
    }

    private Respuesta enviar(String metodo, Object... parametros) throws IOException, ClassNotFoundException {
        salida.writeObject(new Peticion(metodo, parametros));
        salida.reset();
        salida.flush();
        return (Respuesta) entrada.readObject();
    }

    @AfterEach
    void cerrar() throws IOException {
        if (cliente != null) {
            cliente.close();
        }
        if (servidor != null) {
            servidor.close();
        }
    }

    @Test
    void devuelveLaRespuestaDelEnrutador() throws Exception {
        conectar(peticion -> Respuesta.ok("hola " + peticion.getParametros()[0]));

        Respuesta respuesta = enviar("saludar", "Ana");

        assertFalse(respuesta.esError());
        assertEquals("hola Ana", respuesta.getResultado());
    }

    @Test
    void unaExcepcionConMensajeSeConvierteEnRespuestaDeError() throws Exception {
        conectar(peticion -> {
            throw new IllegalStateException("fallo de prueba");
        });

        Respuesta respuesta = enviar("fallar");

        assertTrue(respuesta.esError());
        assertEquals("fallo de prueba", respuesta.getError());
        assertNull(respuesta.getResultado());
    }

    @Test
    void unaExcepcionSinMensajeUsaElMensajePorDefecto() throws Exception {
        conectar(peticion -> {
            throw new RuntimeException();
        });

        Respuesta respuesta = enviar("fallar");

        assertTrue(respuesta.esError());
        assertEquals("Error interno del servidor", respuesta.getError());
    }

    @Test
    void atiendeVariasPeticionesEnLaMismaConexion() throws Exception {
        AtomicInteger contador = new AtomicInteger();
        conectar(peticion -> Respuesta.ok(contador.incrementAndGet()));

        assertEquals(1, enviar("contar").getResultado());
        assertEquals(2, enviar("contar").getResultado());
        assertEquals(3, enviar("contar").getResultado());
    }

    @Test
    void despuesDeUnErrorSigueAtendiendoLaConexion() throws Exception {
        conectar(peticion -> {
            if ("fallar".equals(peticion.getMetodo())) {
                throw new IllegalArgumentException("mal");
            }
            return Respuesta.ok("bien");
        });

        assertTrue(enviar("fallar").esError());
        Respuesta siguiente = enviar("normal");

        assertFalse(siguiente.esError());
        assertEquals("bien", siguiente.getResultado());
    }

    @Test
    void terminaCuandoElClienteSeDesconecta() throws Exception {
        conectar(peticion -> Respuesta.ok(null));
        enviar("algo");

        cliente.close();
        hiloDeAtencion.join(5000);

        assertFalse(hiloDeAtencion.isAlive());
    }
}
