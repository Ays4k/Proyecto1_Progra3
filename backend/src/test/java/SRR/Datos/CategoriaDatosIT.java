package SRR.Datos;

import SRR.DTO.CategoriaDTO;
import SRR.Excepciones.CategoriaEnUsoException;
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

class CategoriaDatosIT {

    private final CategoriaDatos datos = new CategoriaDatos();
    private DatosDePrueba prueba;

    @BeforeAll
    static void verificarBase() {
        DatosDePrueba.verificarBaseDePrueba();
    }

    @BeforeEach
    void preparar() {
        prueba = new DatosDePrueba();
    }

    @AfterEach
    void limpiar() {
        prueba.limpiar();
    }

    private static boolean contiene(List<CategoriaDTO> lista, String id) {
        return lista.stream().anyMatch(categoria -> id.equals(categoria.getId()));
    }

    @Test
    void listarIncluyeLasCategoriasIniciales() {
        List<CategoriaDTO> lista = datos.listar();

        assertTrue(lista.stream().anyMatch(categoria -> "Laptop".equals(categoria.getDescripcion())));
    }

    @Test
    void agregarAsignaElIdYPersiste() {
        CategoriaDTO categoria = prueba.categoria();

        assertNotNull(categoria.getId());
        assertEquals(categoria.getDescripcion(), datos.buscarPorId(categoria.getId()).getDescripcion());
        assertTrue(contiene(datos.listar(), categoria.getId()));
    }

    @Test
    void agregarUnaDescripcionRepetidaLanzaExcepcion() {
        CategoriaDTO existente = prueba.categoria();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> datos.agregar(new CategoriaDTO(null, existente.getDescripcion())));

        assertEquals("Ya existe una categoría con esa descripción.", error.getMessage());
    }

    @Test
    void buscarPorIdDevuelveNuloSiNoExisteONoEsValido() {
        assertNull(datos.buscarPorId("999999999"));
        assertNull(datos.buscarPorId("abc"));
        assertNull(datos.buscarPorId(null));
        assertNull(datos.buscarPorId(" "));
    }

    @Test
    void buscarPorDescripcionEncuentraCoincidenciasParciales() {
        CategoriaDTO categoria = prueba.categoria();
        String fragmento = categoria.getDescripcion().substring(4, 12).toLowerCase();

        assertTrue(contiene(datos.buscarPorDescripcion(fragmento), categoria.getId()));
    }

    @Test
    void buscarPorDescripcionTrataElPorcentajeComoTextoLiteral() {
        String marca = DatosDePrueba.textoUnico();
        CategoriaDTO conPorcentaje = new CategoriaDTO(null, "Sx" + marca + "%");
        CategoriaDTO sinPorcentaje = new CategoriaDTO(null, "Sx" + marca + "A");
        datos.agregar(conPorcentaje);
        prueba.registrarCategoria(conPorcentaje.getId());
        datos.agregar(sinPorcentaje);
        prueba.registrarCategoria(sinPorcentaje.getId());

        List<CategoriaDTO> encontradas = datos.buscarPorDescripcion(marca + "%");

        assertTrue(contiene(encontradas, conPorcentaje.getId()));
        assertFalse(contiene(encontradas, sinPorcentaje.getId()));
    }

    @Test
    void buscarPorDescripcionTrataElGuionBajoComoTextoLiteral() {
        String marca = DatosDePrueba.textoUnico();
        CategoriaDTO conGuion = new CategoriaDTO(null, "Sy" + marca + "_");
        CategoriaDTO conLetra = new CategoriaDTO(null, "Sy" + marca + "Z");
        datos.agregar(conGuion);
        prueba.registrarCategoria(conGuion.getId());
        datos.agregar(conLetra);
        prueba.registrarCategoria(conLetra.getId());

        List<CategoriaDTO> encontradas = datos.buscarPorDescripcion(marca + "_");

        assertTrue(contiene(encontradas, conGuion.getId()));
        assertFalse(contiene(encontradas, conLetra.getId()));
    }

    @Test
    void buscarPorDescripcionNulaDevuelveListaVacia() {
        assertTrue(datos.buscarPorDescripcion(null).isEmpty());
    }

    @Test
    void modificarCambiaLaDescripcion() {
        CategoriaDTO categoria = prueba.categoria();
        String nueva = "Cat nueva " + DatosDePrueba.textoUnico();

        datos.modificar(new CategoriaDTO(categoria.getId(), nueva));

        assertEquals(nueva, datos.buscarPorId(categoria.getId()).getDescripcion());
    }

    @Test
    void modificarUnaCategoriaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> datos.modificar(new CategoriaDTO("999999999", "Cualquiera")));
    }

    @Test
    void modificarAUnaDescripcionRepetidaLanzaExcepcion() {
        CategoriaDTO primera = prueba.categoria();
        CategoriaDTO segunda = prueba.categoria();

        assertThrows(IllegalArgumentException.class,
                () -> datos.modificar(new CategoriaDTO(segunda.getId(), primera.getDescripcion())));
    }

    @Test
    void borrarEliminaLaCategoria() {
        CategoriaDTO categoria = prueba.categoria();

        datos.borrar(categoria.getId());

        assertNull(datos.buscarPorId(categoria.getId()));
    }

    @Test
    void borrarUnaCategoriaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> datos.borrar("999999999"));
    }

    @Test
    void borrarUnaCategoriaConRecursosLanzaCategoriaEnUso() {
        CategoriaDTO categoria = prueba.categoria();
        prueba.recurso(categoria);

        assertThrows(CategoriaEnUsoException.class, () -> datos.borrar(categoria.getId()));
        assertNotNull(datos.buscarPorId(categoria.getId()));
    }
}
