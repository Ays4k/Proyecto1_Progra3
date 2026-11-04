package SRR.Servidor;

import SRR.DTO.AlertaReservaDTO;
import SRR.Pruebas.ClienteDeAlertas;
import SRR.Pruebas.ServidorDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Timeout(30)
class ServidorNotificacionesTest {

    private ServidorDePrueba servidor;
    private ServidorNotificaciones notificador;
    private final List<ClienteDeAlertas> clientes = new ArrayList<>();

    @BeforeEach
    void iniciar() throws IOException {
        servidor = new ServidorDePrueba();
        notificador = servidor.notificador();
    }

    @AfterEach
    void cerrar() {
        clientes.forEach(ClienteDeAlertas::close);
        servidor.close();
    }

    private ClienteDeAlertas conectar(Object saludo) throws IOException {
        ClienteDeAlertas cliente = new ClienteDeAlertas(servidor.puertoAlertas(), saludo);
        clientes.add(cliente);
        return cliente;
    }

    private static AlertaReservaDTO alerta(String idReserva, String idCreador) {
        return new AlertaReservaDTO(idReserva, idCreador, "Nombre", "Actividad",
                "2030-01-01", "09:00", "10:00", List.of("Recurso"));
    }

    @Test
    void registraAlClienteQueSaludaConSuId() throws Exception {
        conectar("u1");

        servidor.esperarClientes(1);

        assertEquals(1, notificador.cantidadClientes());
    }

    @Test
    void laAlertaLlegaATodosMenosAlCreador() throws Exception {
        ClienteDeAlertas u1 = conectar("u1");
        ClienteDeAlertas u2 = conectar("u2");
        ClienteDeAlertas u3 = conectar("u3");
        servidor.esperarClientes(3);

        notificador.accept(alerta("R1", "u1"));
        notificador.accept(alerta("R2", "u2"));
        notificador.accept(alerta("R3", "u3"));

        assertEquals("R1", u2.recibir().getIdReserva());
        assertEquals("R3", u2.recibir().getIdReserva());
        assertEquals("R1", u3.recibir().getIdReserva());
        assertEquals("R2", u3.recibir().getIdReserva());
        assertEquals("R2", u1.recibir().getIdReserva());
        assertEquals("R3", u1.recibir().getIdReserva());
    }

    @Test
    void elCreadorConVariasConexionesNoRecibeNingunaCopia() throws Exception {
        ClienteDeAlertas u1a = conectar("u1");
        ClienteDeAlertas u1b = conectar("u1");
        ClienteDeAlertas u2 = conectar("u2");
        servidor.esperarClientes(3);

        notificador.accept(alerta("R1", "u1"));
        notificador.accept(alerta("R2", "u2"));

        assertEquals("R1", u2.recibir().getIdReserva());
        assertEquals("R2", u1a.recibir().getIdReserva());
        assertEquals("R2", u1b.recibir().getIdReserva());
    }

    @Test
    void laAlertaConservaTodosSusDatos() throws Exception {
        ClienteDeAlertas u2 = conectar("u2");
        servidor.esperarClientes(1);

        notificador.accept(alerta("R1", "u1"));
        AlertaReservaDTO recibida = u2.recibir();

        assertEquals("R1", recibida.getIdReserva());
        assertEquals("u1", recibida.getIdFuncionario());
        assertEquals("Nombre", recibida.getEncargado());
        assertEquals("Actividad", recibida.getActividad());
        assertEquals("2030-01-01", recibida.getFecha());
        assertEquals("09:00", recibida.getHoraInicio());
        assertEquals("10:00", recibida.getHoraFin());
        assertEquals(List.of("Recurso"), recibida.getNombresRecursos());
    }

    @Test
    void unClienteCaidoNoImpideNotificarALosDemas() throws Exception {
        ClienteDeAlertas u1 = conectar("u1");
        ClienteDeAlertas u2 = conectar("u2");
        ClienteDeAlertas u3 = conectar("u3");
        servidor.esperarClientes(3);
        u2.close();

        notificador.accept(alerta("R1", "u1"));
        notificador.accept(alerta("R2", "u1"));
        notificador.accept(alerta("R3", "u1"));

        assertEquals("R1", u3.recibir().getIdReserva());
        assertEquals("R2", u3.recibir().getIdReserva());
        assertEquals("R3", u3.recibir().getIdReserva());
    }

    @Test
    void rechazaUnSaludoVacio() throws Exception {
        ClienteDeAlertas cliente = conectar("");

        assertThrows(IOException.class, cliente::leer);
        assertEquals(0, notificador.cantidadClientes());
    }

    @Test
    void rechazaUnSaludoQueNoEsTexto() throws Exception {
        ClienteDeAlertas cliente = conectar(Integer.valueOf(5));

        assertThrows(IOException.class, cliente::leer);
        assertEquals(0, notificador.cantidadClientes());
    }

    @Test
    void descartaAlClienteQueNuncaSaluda() throws Exception {
        ClienteDeAlertas cliente = conectar(null);
        cliente.esperarHasta(15000);

        IOException error = assertThrows(IOException.class, cliente::leer);

        assertFalse(error instanceof SocketTimeoutException);
        assertEquals(0, notificador.cantidadClientes());
    }

    @Test
    void ignoraUnaAlertaNula() {
        assertDoesNotThrow(() -> notificador.accept(null));
        assertEquals(0, notificador.cantidadClientes());
    }
}
