package SRR.Comunicacion;

import SRR.DTO.AlertaReservaDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteNotificacionesTest {

    private ServerSocket servidor;
    private ClienteNotificaciones cliente;

    // Cada "CompletableFuture" es una cajita que se llena cuando ocurre algo en otro hilo
    private final CompletableFuture<String> cedulaRecibida = new CompletableFuture<>();
    private final CompletableFuture<AlertaReservaDTO> alertaRecibida = new CompletableFuture<>();
    private final CompletableFuture<String> falla = new CompletableFuture<>();

    @AfterEach
    void limpiar() throws Exception {
        if (cliente != null) {
            cliente.detener();
        }
        if (servidor != null) {
            servidor.close();
        }
    }

    // Servidor de mentira en un puerto libre. Hace el mismo saludo que el servidor real:
    // canal de salida con flush, canal de entrada, y lee la cédula.
    private void iniciarServidor(AlertaReservaDTO alertaAEnviar, boolean cerrarDespuesDelSaludo) throws Exception {
        servidor = new ServerSocket(0);   // el 0 significa "cualquier puerto libre"
        Thread hilo = new Thread(() -> {
            try (Socket conexion = servidor.accept()) {
                ObjectOutputStream salida = new ObjectOutputStream(conexion.getOutputStream());
                salida.flush();
                ObjectInputStream entrada = new ObjectInputStream(conexion.getInputStream());
                cedulaRecibida.complete((String) entrada.readObject());

                if (alertaAEnviar != null) {
                    salida.writeObject(alertaAEnviar);
                    salida.flush();
                }
                if (!cerrarDespuesDelSaludo) {
                    Thread.sleep(2000);   // mantiene la conexión abierta mientras corre la prueba
                }
            } catch (Exception e) {
                // la prueba termina cerrando todo; aquí no hay nada que reportar
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    private void iniciarCliente(String cedula) {
        cliente = new ClienteNotificaciones("localhost", servidor.getLocalPort(), cedula,
                alertaRecibida::complete, falla::complete);
        cliente.iniciar();
    }

    @Test
    void enviaLaCedulaYRecibeLasAlertas() throws Exception {
        AlertaReservaDTO alerta = new AlertaReservaDTO("RES-1", "Juan", "Reunion", "2026-11-05",
                "08:00", "10:00", List.of("Sala de reuniones - Piso 1"));
        iniciarServidor(alerta, false);
        iniciarCliente("111");

        assertEquals("111", cedulaRecibida.get(5, TimeUnit.SECONDS));

        AlertaReservaDTO recibida = alertaRecibida.get(5, TimeUnit.SECONDS);
        assertEquals("RES-1", recibida.getIdReserva());
        assertEquals("Juan", recibida.getEncargado());
        assertEquals("Reunion", recibida.getActividad());
        assertEquals(1, recibida.getNombresRecursos().size());
    }

    @Test
    void alDetenerNoSeReportaComoFalla() throws Exception {
        iniciarServidor(null, false);
        iniciarCliente("111");
        cedulaRecibida.get(5, TimeUnit.SECONDS);   // esperamos a que la conexión esté lista

        cliente.detener();
        Thread.sleep(500);

        assertFalse(falla.isDone());
    }

    @Test
    void avisaSiElServidorCierraLaConexion() throws Exception {
        iniciarServidor(null, true);
        iniciarCliente("111");

        String mensaje = falla.get(5, TimeUnit.SECONDS);

        assertTrue(mensaje.contains("servidor de alertas"));
    }
}