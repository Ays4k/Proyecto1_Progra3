package SRR.Datos;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Pruebas.DatosDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaDatosIT {

    private final ReservaDatos datos = new ReservaDatos();
    private final RecursoDatos recursoDatos = new RecursoDatos();
    private DatosDePrueba prueba;
    private UsuarioDTO funcionario;
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
        CategoriaDTO categoria = prueba.categoria();
        recurso = prueba.recurso(categoria);
        fecha = DatosDePrueba.fechaFutura();
    }

    @AfterEach
    void limpiar() {
        prueba.limpiar();
    }

    private ReservaDTO reservaActiva(String inicio, String fin) {
        return prueba.reserva(funcionario.getId(), List.of(recurso.getId()), fecha, inicio, fin, "ACTIVA");
    }

    @Test
    void agregarAsignaElIdYGuardaLosRecursos() {
        RecursoDTO segundo = prueba.recurso(prueba.categoria());
        ReservaDTO reserva = prueba.reserva(funcionario.getId(), List.of(recurso.getId(), segundo.getId()),
                fecha, "09:00", "10:00", "ACTIVA");

        assertNotNull(reserva.getId());
        ReservaDTO guardada = datos.buscarPorId(reserva.getId());
        assertEquals(Set.of(recurso.getId(), segundo.getId()), new HashSet<>(guardada.getIdsRecursos()));
    }

    @Test
    void buscarPorIdDevuelveTodosLosCampos() {
        ReservaDTO reserva = reservaActiva("09:00", "10:30");

        ReservaDTO guardada = datos.buscarPorId(reserva.getId());

        assertEquals(funcionario.getId(), guardada.getIdFuncionario());
        assertEquals("Actividad prueba", guardada.getActividad());
        assertEquals(fecha, guardada.getFecha());
        assertEquals("09:00", guardada.getHoraInicio());
        assertEquals("10:30", guardada.getHoraFin());
        assertEquals("ACTIVA", guardada.getEstado());
    }

    @Test
    void buscarPorIdDevuelveNuloSiNoExisteONoEsValido() {
        assertNull(datos.buscarPorId("999999999"));
        assertNull(datos.buscarPorId("abc"));
        assertNull(datos.buscarPorId(null));
    }

    @Test
    void listarIncluyeLaReservaCreada() {
        ReservaDTO reserva = reservaActiva("09:00", "10:00");

        assertTrue(datos.listar().stream().anyMatch(r -> reserva.getId().equals(r.getId())));
    }

    @Test
    void listarPorFuncionarioDevuelveSoloLasSuyas() {
        UsuarioDTO otro = prueba.usuario("FUNCIONARIO");
        ReservaDTO propia = reservaActiva("09:00", "10:00");
        ReservaDTO ajena = prueba.reserva(otro.getId(), List.of(), fecha, "11:00", "12:00", "ACTIVA");

        List<ReservaDTO> propias = datos.listarPorFuncionario(funcionario.getId());

        assertEquals(1, propias.size());
        assertEquals(propia.getId(), propias.get(0).getId());
        assertEquals(1, datos.listarPorFuncionario(otro.getId()).size());
        assertNotNull(ajena.getId());
    }

    @Test
    void listarPorFuncionarioSinIdentificacionDevuelveListaVacia() {
        assertTrue(datos.listarPorFuncionario(null).isEmpty());
        assertTrue(datos.listarPorFuncionario("  ").isEmpty());
    }

    @Test
    void recursoOcupadoDetectaLosSolapamientos() {
        reservaActiva("09:00", "11:00");
        String id = recurso.getId();

        assertTrue(datos.recursoOcupado(id, fecha, "09:00", "11:00"));
        assertTrue(datos.recursoOcupado(id, fecha, "10:00", "12:00"));
        assertTrue(datos.recursoOcupado(id, fecha, "08:00", "09:30"));
        assertTrue(datos.recursoOcupado(id, fecha, "09:30", "10:00"));
        assertTrue(datos.recursoOcupado(id, fecha, "08:00", "12:00"));
    }

    @Test
    void recursoOcupadoPermiteHorariosContiguosOLejanos() {
        reservaActiva("09:00", "11:00");
        String id = recurso.getId();

        assertFalse(datos.recursoOcupado(id, fecha, "11:00", "12:00"));
        assertFalse(datos.recursoOcupado(id, fecha, "08:00", "09:00"));
        assertFalse(datos.recursoOcupado(id, fecha, "14:00", "15:00"));
    }

    @Test
    void recursoOcupadoSoloMiraLaFechaYElRecursoIndicados() {
        reservaActiva("09:00", "11:00");
        RecursoDTO otroRecurso = prueba.recurso(prueba.categoria());
        String otraFecha = java.time.LocalDate.parse(fecha).plusDays(1).toString();

        assertFalse(datos.recursoOcupado(recurso.getId(), otraFecha, "09:00", "11:00"));
        assertFalse(datos.recursoOcupado(otroRecurso.getId(), fecha, "09:00", "11:00"));
    }

    @Test
    void unaReservaCanceladaNoOcupaElRecurso() {
        prueba.reserva(funcionario.getId(), List.of(recurso.getId()), fecha, "09:00", "11:00", "CANCELADA");

        assertFalse(datos.recursoOcupado(recurso.getId(), fecha, "09:00", "11:00"));
        assertFalse(datos.recursoEnReservaActiva(recurso.getId()));
    }

    @Test
    void recursoEnReservaActivaDetectaLasReservasActivas() {
        assertFalse(datos.recursoEnReservaActiva(recurso.getId()));

        reservaActiva("09:00", "10:00");

        assertTrue(datos.recursoEnReservaActiva(recurso.getId()));
    }

    @Test
    void modificarCambiaElEstadoYReemplazaLosRecursos() {
        RecursoDTO nuevoRecurso = prueba.recurso(prueba.categoria());
        ReservaDTO reserva = reservaActiva("09:00", "10:00");

        reserva.setEstado("CANCELADA");
        reserva.setActividad("Actividad cambiada");
        reserva.setIdsRecursos(List.of(nuevoRecurso.getId()));
        datos.modificar(reserva);
        ReservaDTO modificada = datos.buscarPorId(reserva.getId());

        assertEquals("CANCELADA", modificada.getEstado());
        assertEquals("Actividad cambiada", modificada.getActividad());
        assertEquals(List.of(nuevoRecurso.getId()), modificada.getIdsRecursos());
        assertFalse(datos.recursoEnReservaActiva(recurso.getId()));
    }

    @Test
    void modificarUnaReservaInexistenteLanzaExcepcion() {
        ReservaDTO fantasma = new ReservaDTO("999999999", funcionario.getId(), List.of(), "x",
                fecha, "09:00", "10:00", "ACTIVA");

        assertThrows(IllegalArgumentException.class, () -> datos.modificar(fantasma));
    }

    @Test
    void borrarEliminaLaReservaYLiberaSusRecursos() {
        ReservaDTO reserva = reservaActiva("09:00", "10:00");

        datos.borrar(reserva.getId());

        assertNull(datos.buscarPorId(reserva.getId()));
        recursoDatos.borrar(recurso.getId());
        assertNull(recursoDatos.buscarPorId(recurso.getId()));
    }

    @Test
    void borrarUnaReservaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> datos.borrar("999999999"));
    }

    @Test
    void agregarConUnFuncionarioInexistenteLanzaExcepcion() {
        ReservaDTO reserva = new ReservaDTO(null, "000000000", List.of(), "x", fecha, "09:00", "10:00", "ACTIVA");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> datos.agregar(reserva));

        assertTrue(error.getMessage().contains("funcionario"));
    }

    @Test
    void agregarConUnRecursoInexistenteLanzaExcepcionYNoDejaNadaGuardado() {
        ReservaDTO reserva = new ReservaDTO(null, funcionario.getId(), List.of("999999999"), "x",
                fecha, "09:00", "10:00", "ACTIVA");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> datos.agregar(reserva));

        assertTrue(error.getMessage().contains("recursos"));
        assertTrue(datos.listarPorFuncionario(funcionario.getId()).isEmpty());
    }

    @Test
    void agregarConHoraDeFinAnteriorALaDeInicioFalla() {
        ReservaDTO reserva = new ReservaDTO(null, funcionario.getId(), List.of(), "x",
                fecha, "10:00", "09:00", "ACTIVA");

        assertThrows(RuntimeException.class, () -> datos.agregar(reserva));
        assertTrue(datos.listarPorFuncionario(funcionario.getId()).isEmpty());
    }

    @Test
    void agregarConUnEstadoInvalidoFalla() {
        ReservaDTO reserva = new ReservaDTO(null, funcionario.getId(), List.of(), "x",
                fecha, "09:00", "10:00", "PENDIENTE");

        assertThrows(RuntimeException.class, () -> datos.agregar(reserva));
    }
}
