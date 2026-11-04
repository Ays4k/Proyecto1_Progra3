package SRR.Logica;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Excepciones.CategoriaEnUsoException;
import SRR.Excepciones.RecursoException;
import SRR.Pruebas.DatosDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoriaYRecursoLogicaIT {

    private final CategoriaLogica categorias = new CategoriaLogica();
    private final RecursoLogica recursos = new RecursoLogica();
    private DatosDePrueba prueba;
    private CategoriaDTO categoria;

    @BeforeAll
    static void verificarBase() {
        DatosDePrueba.verificarBaseDePrueba();
    }

    @BeforeEach
    void preparar() {
        prueba = new DatosDePrueba();
        categoria = prueba.categoria();
    }

    @AfterEach
    void limpiar() {
        prueba.limpiar();
    }

    @Test
    void agregarCategoriaSinIdCreaUnaNuevaYDevuelveUno() {
        CategoriaDTO nueva = new CategoriaDTO(null, "Cat nueva " + DatosDePrueba.textoUnico());

        int resultado = categorias.agregarCategoria(nueva);
        prueba.registrarCategoria(nueva.getId());

        assertEquals(1, resultado);
        assertNotNull(categorias.buscarPorId(nueva.getId()));
    }

    @Test
    void agregarCategoriaConIdLaModificaYDevuelveDos() {
        String descripcion = "Cat cambiada " + DatosDePrueba.textoUnico();

        int resultado = categorias.agregarCategoria(new CategoriaDTO(categoria.getId(), descripcion));

        assertEquals(2, resultado);
        assertEquals(descripcion, categorias.buscarPorId(categoria.getId()).getDescripcion());
    }

    @Test
    void eliminarUnaCategoriaConRecursosLanzaCategoriaEnUso() {
        prueba.recurso(categoria);

        assertThrows(CategoriaEnUsoException.class, () -> categorias.eliminarCategoria(categoria.getId()));
        assertNotNull(categorias.buscarPorId(categoria.getId()));
    }

    @Test
    void eliminarUnaCategoriaSinRecursosLaBorra() {
        categorias.eliminarCategoria(categoria.getId());

        assertNull(categorias.buscarPorId(categoria.getId()));
    }

    @Test
    void guardarRecursoSinIdCreaUnoNuevoYDevuelveUno() {
        RecursoDTO nuevo = new RecursoDTO(null, "Rec nuevo " + DatosDePrueba.textoUnico(), categoria.getId());

        int resultado = recursos.guardarRecurso(nuevo);
        prueba.registrarRecurso(nuevo.getId());

        assertEquals(1, resultado);
        assertNotNull(recursos.buscarPorId(nuevo.getId()));
    }

    @Test
    void guardarRecursoConIdLoModificaYDevuelveDos() {
        RecursoDTO existente = prueba.recurso(categoria);
        String descripcion = "Rec cambiado " + DatosDePrueba.textoUnico();

        int resultado = recursos.guardarRecurso(new RecursoDTO(existente.getId(), descripcion, categoria.getId()));

        assertEquals(2, resultado);
        assertEquals(descripcion, recursos.buscarPorId(existente.getId()).getDescripcion());
    }

    @Test
    void guardarRecursoConUnaCategoriaInexistenteLanzaRecursoException() {
        RecursoException error = assertThrows(RecursoException.class,
                () -> recursos.guardarRecurso(new RecursoDTO(null, "Huérfano", "999999999")));

        assertEquals("La categoría seleccionada no existe.", error.getMessage());
    }

    @Test
    void eliminarUnRecursoInexistenteLanzaRecursoException() {
        RecursoException error = assertThrows(RecursoException.class, () -> recursos.eliminarRecurso("999999999"));

        assertEquals("No existe el recurso 999999999.", error.getMessage());
    }

    @Test
    void eliminarUnRecursoLibreLoBorra() {
        RecursoDTO recurso = prueba.recurso(categoria);

        recursos.eliminarRecurso(recurso.getId());

        assertNull(recursos.buscarPorId(recurso.getId()));
    }

    @Test
    void eliminarUnRecursoDeUnaReservaActivaLanzaRecursoException() {
        RecursoDTO recurso = prueba.recurso(categoria);
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");
        prueba.reserva(usuario.getId(), List.of(recurso.getId()), DatosDePrueba.fechaFutura(),
                "09:00", "10:00", "ACTIVA");

        RecursoException error = assertThrows(RecursoException.class,
                () -> recursos.eliminarRecurso(recurso.getId()));

        assertEquals("No se puede eliminar el recurso porque está asignado a una reserva activa.",
                error.getMessage());
        assertNotNull(recursos.buscarPorId(recurso.getId()));
    }

    @Test
    void buscarPorDescripcionNoDistingueMayusculas() {
        RecursoDTO recurso = prueba.recurso(categoria);

        List<RecursoDTO> encontrados = recursos.buscarPorDescripcion(recurso.getDescripcion().toUpperCase());

        assertTrue(encontrados.stream().anyMatch(r -> recurso.getId().equals(r.getId())));
    }

    @Test
    void obtenerRecursosPorCategoriaYFiltrarCoinciden() {
        RecursoDTO recurso = prueba.recurso(categoria);

        assertEquals(1, recursos.obtenerRecursosPorCategoria(categoria.getId()).size());
        assertEquals(recurso.getId(), recursos.filtrarRecursos(categoria.getId(), null).get(0).getId());
        assertFalse(recursos.filtrarRecursos(categoria.getId(), "no existe esta descripcion").size() > 0);
    }
}
