package SRR.Servidor.Enrutadores;

import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrutadorBaseTest {

    private static class EnrutadorDePrueba extends EnrutadorBase {
        EnrutadorDePrueba() {
            registrar("sumar", p -> (Integer) p[0] + (Integer) p[1]);
            registrar("restar", p -> (Integer) p[0] - (Integer) p[1]);
            registrar("sinResultado", p -> null);
            registrar("fallar", p -> {
                throw new IllegalStateException("fallo de prueba");
            });
        }
    }

    private EnrutadorDePrueba enrutador;

    @BeforeEach
    void crearEnrutador() {
        enrutador = new EnrutadorDePrueba();
    }

    @Test
    void manejaDevuelveTrueParaMetodoRegistrado() {
        assertTrue(enrutador.maneja("sumar"));
    }

    @Test
    void manejaDevuelveFalseParaMetodoNoRegistrado() {
        assertFalse(enrutador.maneja("dividir"));
    }

    @Test
    void atenderDevuelveRespuestaExitosaConElResultado() throws Exception {
        Respuesta r = enrutador.atender(new Peticion("sumar", 2, 3));

        assertFalse(r.esError());
        assertNull(r.getError());
        assertEquals(5, r.getResultado());
    }

    @Test
    void atenderEntregaLosParametrosEnElOrdenEnviado() throws Exception {
        Respuesta r = enrutador.atender(new Peticion("restar", 10, 4));

        assertEquals(6, r.getResultado());
    }

    @Test
    void atenderConMetodoSinResultadoDevuelveExitoConNull() throws Exception {
        Respuesta r = enrutador.atender(new Peticion("sinResultado"));

        assertFalse(r.esError());
        assertNull(r.getResultado());
    }

    @Test
    void atenderConMetodoNoRegistradoDevuelveError() throws Exception {
        Respuesta r = enrutador.atender(new Peticion("dividir", 1, 2));

        assertTrue(r.esError());
        assertNull(r.getResultado());
        assertTrue(r.getError().contains("dividir"));
    }

    @Test
    void atenderPropagaLaExcepcionDelManejador() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> enrutador.atender(new Peticion("fallar")));

        assertEquals("fallo de prueba", e.getMessage());
    }
}
