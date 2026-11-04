package SRR.Datos;

import SRR.DTO.UsuarioDTO;
import SRR.Pruebas.DatosDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioDatosIT {

    private final UsuarioDatos datos = new UsuarioDatos();
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

    @Test
    void listarIncluyeLosUsuariosInicialesSinContrasena() {
        boolean hayAdmin = false;
        boolean hayUser = false;
        for (UsuarioDTO usuario : datos.listar()) {
            assertNull(usuario.getContrasena());
            hayAdmin |= "admin".equals(usuario.getId());
            hayUser |= "user".equals(usuario.getId());
        }
        assertTrue(hayAdmin);
        assertTrue(hayUser);
    }

    @Test
    void buscarPorIdEncuentraAlUsuarioConSuRol() {
        UsuarioDTO usuario = datos.buscarPorId("user");

        assertNotNull(usuario);
        assertEquals("User", usuario.getNombre());
        assertEquals("FUNCIONARIO", usuario.getRol());
        assertNull(usuario.getContrasena());
    }

    @Test
    void buscarPorIdIgnoraEspaciosAlrededor() {
        assertNotNull(datos.buscarPorId("  user  "));
    }

    @Test
    void buscarPorIdDevuelveNuloSiNoExisteONoEsValido() {
        assertNull(datos.buscarPorId("000000000"));
        assertNull(datos.buscarPorId(null));
        assertNull(datos.buscarPorId("   "));
    }

    @Test
    void validarCredencialesAceptaLaClaveCorrecta() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        assertEquals(usuario.getId(), datos.validarCredenciales(usuario.getId(), DatosDePrueba.CLAVE));
    }

    @Test
    void validarCredencialesRechazaUnaClaveIncorrecta() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        assertNull(datos.validarCredenciales(usuario.getId(), "otra"));
    }

    @Test
    void validarCredencialesDistingueMayusculasEnLaClave() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        assertNull(datos.validarCredenciales(usuario.getId(), DatosDePrueba.CLAVE.toLowerCase()));
    }

    @Test
    void validarCredencialesConParametrosNulosDevuelveNulo() {
        assertNull(datos.validarCredenciales(null, "x"));
        assertNull(datos.validarCredenciales("user", null));
    }

    @Test
    void validarCredencialesDeUsuarioInexistenteDevuelveNulo() {
        assertNull(datos.validarCredenciales("000000000", "x"));
    }

    @Test
    void unUsuarioSinClaveEntraConSuPropiaIdentificacion() {
        String id = DatosDePrueba.cedulaNueva();
        prueba.registrarUsuario(id);
        datos.agregar(new UsuarioDTO(id, "Sin clave", "8888-0000", null, "FUNCIONARIO"));

        assertEquals(id, datos.validarCredenciales(id, id));
        assertNull(datos.validarCredenciales(id, "otra"));
    }

    @Test
    void agregarGuardaTodosLosCampos() {
        String id = DatosDePrueba.cedulaNueva();
        prueba.registrarUsuario(id);

        datos.agregar(new UsuarioDTO(id, "Nombre Completo", "7777-1234", "abc", "ADMINISTRADOR"));
        UsuarioDTO guardado = datos.buscarPorId(id);

        assertNotNull(guardado);
        assertEquals("Nombre Completo", guardado.getNombre());
        assertEquals("7777-1234", guardado.getTelefono());
        assertEquals("ADMINISTRADOR", guardado.getRol());
    }

    @Test
    void agregarConIdentificacionRepetidaFalla() {
        UsuarioDTO existente = prueba.usuario("FUNCIONARIO");

        assertThrows(RuntimeException.class, () ->
                datos.agregar(new UsuarioDTO(existente.getId(), "Otro", "8888-0000", "x", "FUNCIONARIO")));
    }

    @Test
    void agregarConUnRolInexistenteFalla() {
        String id = DatosDePrueba.cedulaNueva();
        prueba.registrarUsuario(id);

        assertThrows(RuntimeException.class, () ->
                datos.agregar(new UsuarioDTO(id, "Sin rol", "8888-0000", "x", "ROL_QUE_NO_EXISTE")));
        assertNull(datos.buscarPorId(id));
    }

    @Test
    void modificarCambiaLosDatosPeroNoLaClave() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        datos.modificar(new UsuarioDTO(usuario.getId(), "Nombre Nuevo", "1111-2222", null, "ADMINISTRADOR"));
        UsuarioDTO modificado = datos.buscarPorId(usuario.getId());

        assertEquals("Nombre Nuevo", modificado.getNombre());
        assertEquals("1111-2222", modificado.getTelefono());
        assertEquals("ADMINISTRADOR", modificado.getRol());
        assertEquals(usuario.getId(), datos.validarCredenciales(usuario.getId(), DatosDePrueba.CLAVE));
    }

    @Test
    void cambiarContrasenaActualizaLaClave() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        datos.cambiarContrasena(usuario.getId(), "NuevaClave9");

        assertEquals(usuario.getId(), datos.validarCredenciales(usuario.getId(), "NuevaClave9"));
        assertNull(datos.validarCredenciales(usuario.getId(), DatosDePrueba.CLAVE));
    }

    @Test
    void cambiarContrasenaDeUnUsuarioInexistenteLanzaExcepcion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> datos.cambiarContrasena("000000000", "x"));

        assertEquals("Usuario no encontrado", error.getMessage());
    }

    @Test
    void borrarEliminaAlUsuario() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        datos.borrar(usuario.getId());

        assertNull(datos.buscarPorId(usuario.getId()));
    }

    @Test
    void borrarUnUsuarioInexistenteNoFalla() {
        assertDoesNotThrow(() -> datos.borrar("000000000"));
    }
}
