package SRR.Logica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservaLogicaValidacionTest {

    private static final String SIN_CATEGORIAS = "Debe seleccionar al menos una categoría";

    private final String manana = LocalDate.now().plusDays(1).toString();
    private final List<String> categorias = List.of("1");
    private ReservaLogica logica;

    @BeforeEach
    void crearLogica() {
        logica = new ReservaLogica(alerta -> { });
    }

    private String errorAlCrear(String funcionario, String actividad, String fecha,
                                String inicio, String fin, List<String> idsCategorias) {
        return assertThrows(IllegalArgumentException.class,
                () -> logica.crearReserva(funcionario, actividad, fecha, inicio, fin, idsCategorias))
                .getMessage();
    }

    @Test
    void exigeUnFuncionario() {
        assertEquals("No hay un funcionario activo",
                errorAlCrear(null, "Reunión", manana, "09:00", "10:00", categorias));
        assertEquals("No hay un funcionario activo",
                errorAlCrear("  ", "Reunión", manana, "09:00", "10:00", categorias));
    }

    @Test
    void exigeUnaActividad() {
        assertEquals("Debe describir la actividad",
                errorAlCrear("1", null, manana, "09:00", "10:00", categorias));
        assertEquals("Debe describir la actividad",
                errorAlCrear("1", " ", manana, "09:00", "10:00", categorias));
    }

    @Test
    void exigeUnaFecha() {
        assertEquals("Debe indicar la fecha",
                errorAlCrear("1", "Reunión", null, "09:00", "10:00", categorias));
        assertEquals("Debe indicar la fecha",
                errorAlCrear("1", "Reunión", "", "09:00", "10:00", categorias));
    }

    @Test
    void exigeLasDosHoras() {
        assertEquals("Debe indicar las horas",
                errorAlCrear("1", "Reunión", manana, null, "10:00", categorias));
        assertEquals("Debe indicar las horas",
                errorAlCrear("1", "Reunión", manana, "09:00", null, categorias));
    }

    @Test
    void rechazaUnaFechaConFormatoInvalido() {
        assertEquals("La fecha no tiene un formato válido",
                errorAlCrear("1", "Reunión", "2030-13-45", "09:00", "10:00", categorias));
        assertEquals("La fecha no tiene un formato válido",
                errorAlCrear("1", "Reunión", "mañana", "09:00", "10:00", categorias));
    }

    @Test
    void rechazaHorasConFormatoInvalido() {
        assertEquals("Las horas no tienen un formato válido",
                errorAlCrear("1", "Reunión", manana, "25:00", "26:00", categorias));
        assertEquals("Las horas no tienen un formato válido",
                errorAlCrear("1", "Reunión", manana, "9", "10", categorias));
    }

    @Test
    void rechazaUnaFechaPasada() {
        String ayer = LocalDate.now().minusDays(1).toString();

        assertEquals("No se puede reservar en una fecha pasada",
                errorAlCrear("1", "Reunión", ayer, "09:00", "10:00", categorias));
    }

    @Test
    void rechazaUnaHoraDeInicioQueYaPasoHoy() {
        String hoy = LocalDate.now().toString();

        assertEquals("La hora de inicio ya pasó",
                errorAlCrear("1", "Reunión", hoy, "00:00", "00:30", categorias));
    }

    @Test
    void rechazaUnaHoraDeFinAnteriorOIgualAlInicio() {
        assertEquals("La hora de fin debe ser posterior a la de inicio",
                errorAlCrear("1", "Reunión", manana, "10:00", "09:00", categorias));
        assertEquals("La hora de fin debe ser posterior a la de inicio",
                errorAlCrear("1", "Reunión", manana, "10:00", "10:00", categorias));
    }

    @Test
    void rechazaReservasFueraDelHorarioDeOficina() {
        String mensaje = "Las reservas deben realizarse entre las 08:00 y las 17:00";

        assertEquals(mensaje, errorAlCrear("1", "Reunión", manana, "07:59", "09:00", categorias));
        assertEquals(mensaje, errorAlCrear("1", "Reunión", manana, "16:00", "17:01", categorias));
    }

    @Test
    void exigeAlMenosUnaCategoria() {
        assertEquals(SIN_CATEGORIAS, errorAlCrear("1", "Reunión", manana, "09:00", "10:00", null));
        assertEquals(SIN_CATEGORIAS, errorAlCrear("1", "Reunión", manana, "09:00", "10:00", List.of()));
    }

    @Test
    void aceptaLosLimitesDelHorarioDeOficina() {
        assertEquals(SIN_CATEGORIAS, errorAlCrear("1", "Reunión", manana, "08:00", "17:00", List.of()));
    }
}
