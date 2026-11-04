package SRR.Logica;

import SRR.DTO.RecursoDTO;
import SRR.Excepciones.RecursoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecursoLogicaValidacionTest {

    private final RecursoLogica logica = new RecursoLogica();

    private String errorAlGuardar(RecursoDTO recurso) {
        return assertThrows(RecursoException.class, () -> logica.guardarRecurso(recurso)).getMessage();
    }

    @Test
    void exigeLosDatosDelRecurso() {
        assertEquals("Debe proporcionar los datos del recurso.", errorAlGuardar(null));
    }

    @Test
    void exigeUnaDescripcion() {
        assertEquals("Debe indicar la descripción del recurso.", errorAlGuardar(new RecursoDTO(null, null, "1")));
        assertEquals("Debe indicar la descripción del recurso.", errorAlGuardar(new RecursoDTO(null, "  ", "1")));
    }

    @Test
    void exigeUnaCategoria() {
        assertEquals("Debe seleccionar una categoría.", errorAlGuardar(new RecursoDTO(null, "Laptop", null)));
        assertEquals("Debe seleccionar una categoría.", errorAlGuardar(new RecursoDTO(null, "Laptop", " ")));
    }

    @Test
    void eliminarExigeElIdDelRecurso() {
        assertEquals("Debe indicar el recurso que desea eliminar.",
                assertThrows(RecursoException.class, () -> logica.eliminarRecurso(null)).getMessage());
        assertEquals("Debe indicar el recurso que desea eliminar.",
                assertThrows(RecursoException.class, () -> logica.eliminarRecurso(" ")).getMessage());
    }

    @Test
    void buscarPorDescripcionConTextoNuloDevuelveListaVacia() {
        assertTrue(logica.buscarPorDescripcion(null).isEmpty());
    }
}
