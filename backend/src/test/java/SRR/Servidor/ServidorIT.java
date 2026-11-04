package SRR.Servidor;

import SRR.DTO.AlertaReservaDTO;
import SRR.DTO.CategoriaDTO;
import SRR.DTO.LoginDTO;
import SRR.DTO.Protocolo;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;
import SRR.DTO.Respuesta;
import SRR.DTO.UsuarioDTO;
import SRR.Pruebas.ClienteDeAlertas;
import SRR.Pruebas.ClienteDePeticiones;
import SRR.Pruebas.DatosDePrueba;
import SRR.Pruebas.ServidorDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(60)
class ServidorIT {

    private ServidorDePrueba servidor;
    private DatosDePrueba prueba;
    private UsuarioDTO ana;
    private UsuarioDTO beto;
    private CategoriaDTO categoria;
    private RecursoDTO recurso;
    private String fecha;
    private final List<AutoCloseable> abiertos = new ArrayList<>();

    @BeforeAll
    static void verificarBase() {
        DatosDePrueba.verificarBaseDePrueba();
    }

    @BeforeEach
    void iniciar() throws Exception {
        prueba = new DatosDePrueba();
        ana = prueba.usuario("FUNCIONARIO");
        beto = prueba.usuario("FUNCIONARIO");
        categoria = prueba.categoria();
        recurso = prueba.recurso(categoria);
        fecha = DatosDePrueba.fechaFutura();
        servidor = new ServidorDePrueba();
    }

    @AfterEach
    void cerrar() throws Exception {
        for (AutoCloseable abierto : abiertos) {
            abierto.close();
        }
        servidor.close();
        prueba.limpiar();
    }

    private ClienteDePeticiones clienteDePeticiones() throws Exception {
        ClienteDePeticiones cliente = new ClienteDePeticiones(servidor.puertoPeticiones());
        abiertos.add(cliente);
        return cliente;
    }

    private ClienteDeAlertas clienteDeAlertas(UsuarioDTO usuario) throws Exception {
        ClienteDeAlertas cliente = new ClienteDeAlertas(servidor.puertoAlertas(), usuario.getId());
        abiertos.add(cliente);
        return cliente;
    }

    private Respuesta reservar(ClienteDePeticiones cliente, UsuarioDTO usuario, String inicio, String fin)
            throws Exception {
        return cliente.enviar(Protocolo.CREAR_RESERVA, usuario.getId(), "Reunión", fecha, inicio, fin,
                List.of(categoria.getId()));
    }

    @Test
    void iniciarSesionPorSocketDevuelveAlUsuario() throws Exception {
        ClienteDePeticiones cliente = clienteDePeticiones();

        Respuesta respuesta = cliente.enviar(Protocolo.INICIAR_SESION, new LoginDTO(ana.getId(), DatosDePrueba.CLAVE));

        assertFalse(respuesta.esError());
        UsuarioDTO sesion = (UsuarioDTO) respuesta.getResultado();
        assertEquals(ana.getId(), sesion.getId());
        assertNull(sesion.getContrasena());
    }

    @Test
    void iniciarSesionConClaveIncorrectaDevuelveResultadoNuloSinError() throws Exception {
        ClienteDePeticiones cliente = clienteDePeticiones();

        Respuesta respuesta = cliente.enviar(Protocolo.INICIAR_SESION, new LoginDTO(ana.getId(), "equivocada"));

        assertFalse(respuesta.esError());
        assertNull(respuesta.getResultado());
    }

    @Test
    void unaOperacionDesconocidaDevuelveError() throws Exception {
        ClienteDePeticiones cliente = clienteDePeticiones();

        Respuesta respuesta = cliente.enviar("operacionQueNoExiste");

        assertTrue(respuesta.esError());
    }

    @Test
    void parametrosIncompletosDevuelvenErrorYLaConexionSigueViva() throws Exception {
        ClienteDePeticiones cliente = clienteDePeticiones();

        Respuesta mala = cliente.enviar(Protocolo.CREAR_RESERVA);
        Respuesta buena = cliente.enviar(Protocolo.INICIAR_SESION, new LoginDTO(ana.getId(), DatosDePrueba.CLAVE));

        assertTrue(mala.esError());
        assertFalse(buena.esError());
        assertNotNull(buena.getResultado());
    }

    @Test
    void unaReservaInvalidaDevuelveElMensajeDeLaValidacion() throws Exception {
        ClienteDePeticiones cliente = clienteDePeticiones();

        Respuesta respuesta = reservar(cliente, ana, "10:00", "09:00");

        assertTrue(respuesta.esError());
        assertEquals("La hora de fin debe ser posterior a la de inicio", respuesta.getError());
    }

    @Test
    void unaReservaSinCupoDevuelveLasCategoriasSinDisponibilidad() throws Exception {
        ClienteDePeticiones cliente = clienteDePeticiones();
        assertFalse(reservar(cliente, ana, "09:00", "10:00").esError());

        Respuesta respuesta = reservar(cliente, beto, "09:30", "10:30");

        assertTrue(respuesta.esError());
        assertEquals("No hay disponibilidad en: " + categoria.getDescripcion(), respuesta.getError());
    }

    @Test
    void cancelarUnaReservaPorSocketLaQuitaDeLasActivas() throws Exception {
        ClienteDePeticiones cliente = clienteDePeticiones();
        ReservaDTO reserva = (ReservaDTO) reservar(cliente, ana, "09:00", "10:00").getResultado();

        Respuesta cancelada = cliente.enviar(Protocolo.CANCELAR_RESERVA, reserva.getId());
        Respuesta activas = cliente.enviar(Protocolo.RESERVAS_ACTIVAS_DE, ana.getId());

        assertFalse(cancelada.esError());
        assertNull(cancelada.getResultado());
        assertTrue(((List<?>) activas.getResultado()).isEmpty());
    }

    @Test
    void variosClientesPuedenUsarElServidorAlMismoTiempo() throws Exception {
        ClienteDePeticiones primero = clienteDePeticiones();
        ClienteDePeticiones segundo = clienteDePeticiones();

        Respuesta deAna = primero.enviar(Protocolo.INICIAR_SESION, new LoginDTO(ana.getId(), DatosDePrueba.CLAVE));
        Respuesta deBeto = segundo.enviar(Protocolo.INICIAR_SESION, new LoginDTO(beto.getId(), DatosDePrueba.CLAVE));

        assertEquals(ana.getId(), ((UsuarioDTO) deAna.getResultado()).getId());
        assertEquals(beto.getId(), ((UsuarioDTO) deBeto.getResultado()).getId());
    }

    @Test
    void crearUnaReservaAvisaALosDemasPeroNoAlCreador() throws Exception {
        ClienteDeAlertas alertasDeAna = clienteDeAlertas(ana);
        ClienteDeAlertas alertasDeBeto = clienteDeAlertas(beto);
        servidor.esperarClientes(2);
        ClienteDePeticiones deAna = clienteDePeticiones();
        ClienteDePeticiones deBeto = clienteDePeticiones();

        ReservaDTO primera = (ReservaDTO) reservar(deAna, ana, "09:00", "10:00").getResultado();
        ReservaDTO segunda = (ReservaDTO) reservar(deBeto, beto, "10:00", "11:00").getResultado();
        ReservaDTO tercera = (ReservaDTO) reservar(deAna, ana, "11:00", "12:00").getResultado();

        AlertaReservaDTO primeraDeBeto = alertasDeBeto.recibir();
        assertEquals(primera.getId(), primeraDeBeto.getIdReserva());
        assertEquals(ana.getId(), primeraDeBeto.getIdFuncionario());
        assertEquals(ana.getNombre(), primeraDeBeto.getEncargado());
        assertEquals(List.of(recurso.getDescripcion()), primeraDeBeto.getNombresRecursos());
        assertEquals(tercera.getId(), alertasDeBeto.recibir().getIdReserva());

        assertEquals(segunda.getId(), alertasDeAna.recibir().getIdReserva());
    }

    @Test
    void unaReservaFallidaNoGeneraAlertas() throws Exception {
        ClienteDeAlertas alertasDeBeto = clienteDeAlertas(beto);
        servidor.esperarClientes(1);
        ClienteDePeticiones deAna = clienteDePeticiones();

        assertTrue(reservar(deAna, ana, "10:00", "09:00").esError());
        ReservaDTO valida = (ReservaDTO) reservar(deAna, ana, "09:00", "10:00").getResultado();

        assertEquals(valida.getId(), alertasDeBeto.recibir().getIdReserva());
    }
}
