package SRR.Alertas;

import SRR.DTO.AlertaReservaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListaAlertasTest {

    private ListaAlertas lista;

    @BeforeEach
    void preparar() {
        lista = new ListaAlertas();   // antes de cada prueba empezamos con una lista nueva
    }

    private AlertaReservaDTO crearAlerta(String id, String encargado, String actividad) {
        return new AlertaReservaDTO(id, encargado, actividad, "2026-11-05", "08:00", "10:00",
                List.of("Sala de reuniones - Piso 1", "Proyector Epson #1"));
    }

    @Test
    void empiezaVacia() {
        assertEquals(0, lista.cantidad());
    }

    @Test
    void agregarGuardaLasAlertasEnElOrdenQueLlegan() {
        AlertaReservaDTO primera = crearAlerta("RES-1", "Juan", "Reunion");
        AlertaReservaDTO segunda = crearAlerta("RES-2", "Ana", "Capacitacion");

        assertTrue(lista.agregar(primera));
        assertTrue(lista.agregar(segunda));

        assertEquals(2, lista.cantidad());
        assertSame(primera, lista.getAlertas().get(0));
        assertSame(segunda, lista.getAlertas().get(1));
    }

    @Test
    void agregarIgnoraUnaAlertaNula() {
        assertFalse(lista.agregar(null));
        assertEquals(0, lista.cantidad());
    }

    @Test
    void verConservaElDetalleCompletoDeLaAlerta() {
        lista.agregar(crearAlerta("RES-1", "Juan", "Reunion"));

        AlertaReservaDTO alerta = lista.getAlertas().get(0);

        assertEquals("Juan", alerta.getEncargado());
        assertEquals("2026-11-05", alerta.getFecha());
        assertEquals("08:00", alerta.getHoraInicio());
        assertEquals("10:00", alerta.getHoraFin());
        assertEquals(2, alerta.getNombresRecursos().size());
    }

    @Test
    void descartarQuitaSoloEsaAlerta() {
        AlertaReservaDTO primera = crearAlerta("RES-1", "Juan", "Reunion");
        AlertaReservaDTO segunda = crearAlerta("RES-2", "Ana", "Capacitacion");
        lista.agregar(primera);
        lista.agregar(segunda);

        assertTrue(lista.descartar(primera));

        assertEquals(1, lista.cantidad());
        assertSame(segunda, lista.getAlertas().get(0));
    }

    @Test
    void descartarUnaAlertaQueNoEstaDevuelveFalso() {
        lista.agregar(crearAlerta("RES-1", "Juan", "Reunion"));

        assertFalse(lista.descartar(crearAlerta("RES-9", "Otro", "Otra")));

        assertEquals(1, lista.cantidad());
    }

    @Test
    void resumenMuestraEncargadoYActividad() {
        assertEquals("Juan - Reunion", ListaAlertas.resumen(crearAlerta("RES-1", "Juan", "Reunion")));
    }
}