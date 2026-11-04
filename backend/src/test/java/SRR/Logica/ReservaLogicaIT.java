package SRR.Logica;

import SRR.DTO.AlertaReservaDTO;
import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Excepciones.CategoriasNoDisponiblesException;
import SRR.Pruebas.DatosDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaLogicaIT {

    private final List<AlertaReservaDTO> alertas = new CopyOnWriteArrayList<>();
    private ReservaLogica logica;
    private DatosDePrueba prueba;
    private UsuarioDTO funcionario;
    private CategoriaDTO categoria;
    private RecursoDTO recurso;
    private String fecha;

    @BeforeAll
    static void verificarBase() {
        DatosDePrueba.verificarBaseDePrueba();
    }

    @BeforeEach
    void preparar() {
        prueba = new DatosDePrueba();
        funcionario = prueba.usuario("FUNCIONARIO");
        categoria = prueba.categoria();
        recurso = prueba.recurso(categoria);
        fecha = DatosDePrueba.fechaFutura();
        logica = new ReservaLogica(alertas::add);
    }

    @AfterEach
    void limpiar() {
        prueba.limpiar();
    }

    private ReservaDTO crear(String inicio, String fin) {
        return logica.crearReserva(funcionario.getId(), "Reunión de prueba", fecha, inicio, fin,
                List.of(categoria.getId()));
    }

    @Test
    void crearReservaAsignaUnRecursoDeLaCategoriaYPersiste() {
        ReservaDTO reserva = crear("09:00", "10:00");

        assertNotNull(reserva.getId());
        assertEquals("ACTIVA", reserva.getEstado());
        assertEquals(List.of(recurso.getId()), reserva.getIdsRecursos());
        ReservaDTO guardada = logica.buscarPorId(reserva.getId());
        assertEquals(funcionario.getId(), guardada.getIdFuncionario());
        assertEquals("Reunión de prueba", guardada.getActividad());
        assertEquals(List.of(recurso.getId()), guardada.getIdsRecursos());
    }

    @Test
    void crearReservaEnviaUnaAlertaConLosDatosDeLaReserva() {
        ReservaDTO reserva = crear("09:00", "10:00");

        assertEquals(1, alertas.size());
        AlertaReservaDTO alerta = alertas.get(0);
        assertEquals(reserva.getId(), alerta.getIdReserva());
        assertEquals(funcionario.getId(), alerta.getIdFuncionario());
        assertEquals(funcionario.getNombre(), alerta.getEncargado());
        assertEquals("Reunión de prueba", alerta.getActividad());
        assertEquals(fecha, alerta.getFecha());
        assertEquals("09:00", alerta.getHoraInicio());
        assertEquals("10:00", alerta.getHoraFin());
        assertEquals(List.of(recurso.getDescripcion()), alerta.getNombresRecursos());
    }

    @Test
    void crearReservaSigueFuncionandoSiElObservadorFalla() {
        ReservaLogica conObservadorRoto = new ReservaLogica(alerta -> {
            throw new IllegalStateException("fallo del observador");
        });

        ReservaDTO reserva = conObservadorRoto.crearReserva(funcionario.getId(), "Reunión", fecha,
                "09:00", "10:00", List.of(categoria.getId()));

        assertNotNull(logica.buscarPorId(reserva.getId()));
    }

    @Test
    void crearReservaSinCupoLanzaCategoriasNoDisponiblesYNoNotifica() {
        crear("09:00", "10:00");

        CategoriasNoDisponiblesException error = assertThrows(CategoriasNoDisponiblesException.class,
                () -> crear("09:30", "10:30"));

        assertEquals(List.of(categoria.getDescripcion()), error.getCategorias());
        assertEquals(1, alertas.size());
        assertEquals(1, logica.buscarPorFuncionario(funcionario.getId()).size());
    }

    @Test
    void crearReservasEnHorariosContiguosUsaElMismoRecurso() {
        ReservaDTO primera = crear("09:00", "10:00");
        ReservaDTO segunda = crear("10:00", "11:00");

        assertEquals(primera.getIdsRecursos(), segunda.getIdsRecursos());
    }

    @Test
    void crearReservaConUnaCategoriaRepetidaAsignaUnSoloRecurso() {
        prueba.recurso(categoria);

        ReservaDTO reserva = logica.crearReserva(funcionario.getId(), "Reunión", fecha, "09:00", "10:00",
                List.of(categoria.getId(), categoria.getId()));

        assertEquals(1, reserva.getIdsRecursos().size());
    }

    @Test
    void crearReservaConVariasCategoriasAsignaUnRecursoDeCadaUna() {
        CategoriaDTO otra = prueba.categoria();
        RecursoDTO otroRecurso = prueba.recurso(otra);

        ReservaDTO reserva = logica.crearReserva(funcionario.getId(), "Reunión", fecha, "09:00", "10:00",
                List.of(categoria.getId(), otra.getId()));

        assertEquals(List.of(recurso.getId(), otroRecurso.getId()), reserva.getIdsRecursos());
    }

    @Test
    void crearReservaListaTodasLasCategoriasSinCupo() {
        CategoriaDTO otra = prueba.categoria();

        CategoriasNoDisponiblesException error = assertThrows(CategoriasNoDisponiblesException.class,
                () -> logica.crearReserva(funcionario.getId(), "Reunión", fecha, "09:00", "10:00",
                        List.of(categoria.getId(), otra.getId())));

        assertEquals(List.of(otra.getDescripcion()), error.getCategorias());
        assertTrue(logica.buscarPorFuncionario(funcionario.getId()).isEmpty());
    }

    @Test
    void cancelarReservaLiberaElRecurso() {
        ReservaDTO reserva = crear("09:00", "10:00");
        assertFalse(logica.estaDisponible(recurso.getId(), fecha, "09:00", "10:00"));

        logica.cancelarReserva(reserva.getId());

        assertEquals("CANCELADA", logica.buscarPorId(reserva.getId()).getEstado());
        assertTrue(logica.estaDisponible(recurso.getId(), fecha, "09:00", "10:00"));
        assertTrue(logica.estaDisponible(recurso.getId()));
        assertNotNull(crear("09:00", "10:00").getId());
    }

    @Test
    void cancelarUnaReservaInexistenteLanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> logica.cancelarReserva("999999999"));

        assertEquals("No existe la reserva 999999999", error.getMessage());
    }

    @Test
    void cancelarUnaReservaQueYaPasoLanzaExcepcion() {
        String ayer = LocalDate.now().minusDays(1).toString();
        ReservaDTO vieja = prueba.reserva(funcionario.getId(), List.of(), ayer, "09:00", "10:00", "ACTIVA");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> logica.cancelarReserva(vieja.getId()));

        assertEquals("No se puede cancelar una reserva que ya pasó.", error.getMessage());
        assertEquals("ACTIVA", logica.buscarPorId(vieja.getId()).getEstado());
    }

    @Test
    void reservasActivasDeExcluyeLasCanceladas() {
        ReservaDTO activa = crear("09:00", "10:00");
        ReservaDTO cancelada = crear("10:00", "11:00");
        logica.cancelarReserva(cancelada.getId());

        List<ReservaDTO> resultado = logica.reservasActivasDe(funcionario.getId());

        assertEquals(1, resultado.size());
        assertEquals(activa.getId(), resultado.get(0).getId());
        assertEquals(2, logica.buscarPorFuncionario(funcionario.getId()).size());
    }

    @Test
    void recursosDisponiblesExcluyeLosOcupados() {
        RecursoDTO libre = prueba.recurso(categoria);
        crear("09:00", "10:00");

        List<RecursoDTO> disponibles = logica.recursosDisponibles(categoria.getId(), fecha, "09:00", "10:00");

        assertEquals(1, disponibles.size());
        assertEquals(libre.getId(), disponibles.get(0).getId());
    }

    @Test
    void estaDisponibleSinHorarioMiraTodasLasReservasActivas() {
        assertTrue(logica.estaDisponible(recurso.getId()));

        crear("09:00", "10:00");

        assertFalse(logica.estaDisponible(recurso.getId()));
    }

    @Test
    void obtenerTodasLasReservasIncluyeLaCreada() {
        ReservaDTO reserva = crear("09:00", "10:00");

        assertTrue(logica.obtenerTodasLasReservas().stream().anyMatch(r -> reserva.getId().equals(r.getId())));
    }
}
