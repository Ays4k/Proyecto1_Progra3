package SRR.Datos;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.UsuarioDTO;
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

class RecursoDatosIT {

    private final RecursoDatos datos = new RecursoDatos();
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

    private static boolean contiene(List<RecursoDTO> lista, String id) {
        return lista.stream().anyMatch(recurso -> id.equals(recurso.getId()));
    }

    @Test
    void listarIncluyeLosRecursosIniciales() {
        assertTrue(datos.listar().stream()
                .anyMatch(recurso -> "Proyector Epson #1".equals(recurso.getDescripcion())));
    }

    @Test
    void agregarAsignaElIdYGuardaSusCampos() {
        RecursoDTO recurso = prueba.recurso(categoria);

        RecursoDTO guardado = datos.buscarPorId(recurso.getId());

        assertNotNull(guardado);
        assertEquals(recurso.getDescripcion(), guardado.getDescripcion());
        assertEquals(categoria.getId(), guardado.getIdCategoria());
    }

    @Test
    void agregarConUnaCategoriaInexistenteLanzaRecursoException() {
        RecursoException error = assertThrows(RecursoException.class,
                () -> datos.agregar(new RecursoDTO(null, "Huérfano", "999999999")));

        assertEquals("La categoría seleccionada no existe.", error.getMessage());
    }

    @Test
    void buscarPorIdDevuelveNuloSiNoExisteONoEsValido() {
        assertNull(datos.buscarPorId("999999999"));
        assertNull(datos.buscarPorId("abc"));
        assertNull(datos.buscarPorId(null));
    }

    @Test
    void listarPorCategoriaDevuelveSoloLosDeEsaCategoria() {
        CategoriaDTO otra = prueba.categoria();
        RecursoDTO propio = prueba.recurso(categoria);
        RecursoDTO ajeno = prueba.recurso(otra);

        List<RecursoDTO> resultado = datos.listarPorCategoria(categoria.getId());

        assertTrue(contiene(resultado, propio.getId()));
        assertFalse(contiene(resultado, ajeno.getId()));
    }

    @Test
    void filtrarCombinaCategoriaYDescripcion() {
        RecursoDTO primero = new RecursoDTO(null, "Alfa " + DatosDePrueba.textoUnico(), categoria.getId());
        RecursoDTO segundo = new RecursoDTO(null, "Beta " + DatosDePrueba.textoUnico(), categoria.getId());
        datos.agregar(primero);
        prueba.registrarRecurso(primero.getId());
        datos.agregar(segundo);
        prueba.registrarRecurso(segundo.getId());

        List<RecursoDTO> resultado = datos.filtrar(categoria.getId(), "alfa");

        assertTrue(contiene(resultado, primero.getId()));
        assertFalse(contiene(resultado, segundo.getId()));
    }

    @Test
    void filtrarSinCriteriosDevuelveTodos() {
        RecursoDTO recurso = prueba.recurso(categoria);

        assertTrue(contiene(datos.filtrar(null, null), recurso.getId()));
    }

    @Test
    void filtrarConUnaCategoriaNoNumericaDevuelveListaVacia() {
        assertTrue(datos.filtrar("abc", null).isEmpty());
    }

    @Test
    void modificarCambiaDescripcionYCategoria() {
        CategoriaDTO otra = prueba.categoria();
        RecursoDTO recurso = prueba.recurso(categoria);
        String nueva = "Rec nuevo " + DatosDePrueba.textoUnico();

        datos.modificar(new RecursoDTO(recurso.getId(), nueva, otra.getId()));
        RecursoDTO modificado = datos.buscarPorId(recurso.getId());

        assertEquals(nueva, modificado.getDescripcion());
        assertEquals(otra.getId(), modificado.getIdCategoria());
    }

    @Test
    void modificarUnRecursoInexistenteLanzaRecursoException() {
        assertThrows(RecursoException.class,
                () -> datos.modificar(new RecursoDTO("999999999", "Nada", categoria.getId())));
    }

    @Test
    void modificarConUnaCategoriaInexistenteLanzaRecursoException() {
        RecursoDTO recurso = prueba.recurso(categoria);

        assertThrows(RecursoException.class,
                () -> datos.modificar(new RecursoDTO(recurso.getId(), "x", "999999999")));
    }

    @Test
    void borrarEliminaElRecurso() {
        RecursoDTO recurso = prueba.recurso(categoria);

        datos.borrar(recurso.getId());

        assertNull(datos.buscarPorId(recurso.getId()));
    }

    @Test
    void borrarUnRecursoInexistenteLanzaRecursoException() {
        assertThrows(RecursoException.class, () -> datos.borrar("999999999"));
    }

    @Test
    void borrarUnRecursoAsignadoAUnaReservaLanzaRecursoException() {
        RecursoDTO recurso = prueba.recurso(categoria);
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");
        prueba.reserva(usuario.getId(), List.of(recurso.getId()), DatosDePrueba.fechaFutura(),
                "09:00", "10:00", "ACTIVA");

        RecursoException error = assertThrows(RecursoException.class, () -> datos.borrar(recurso.getId()));

        assertTrue(error.getMessage().contains("asignado"));
        assertNotNull(datos.buscarPorId(recurso.getId()));
    }
}
