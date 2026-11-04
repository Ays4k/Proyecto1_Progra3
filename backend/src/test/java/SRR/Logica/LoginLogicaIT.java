package SRR.Logica;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Pruebas.DatosDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginLogicaIT {

    private final LoginLogica logica = new LoginLogica();
    private DatosDePrueba prueba;
    private UsuarioDTO usuario;

    @BeforeAll
    static void verificarBase() {
        DatosDePrueba.verificarBaseDePrueba();
    }

    @BeforeEach
    void preparar() {
        prueba = new DatosDePrueba();
        usuario = prueba.usuario("FUNCIONARIO");
    }

    @AfterEach
    void limpiar() {
        prueba.limpiar();
    }

    private String errorAlCambiar(String actual, String nueva, String confirmacion) {
        return assertThrows(IllegalArgumentException.class,
                () -> logica.cambiarClave(usuario.getId(), actual, nueva, confirmacion)).getMessage();
    }

    @Test
    void iniciarSesionConCredencialesValidasDevuelveAlUsuarioSinContrasena() {
        UsuarioDTO sesion = logica.iniciarSesion(new LoginDTO("admin", "admin"));

        assertNotNull(sesion);
        assertEquals("admin", sesion.getId());
        assertEquals("ADMINISTRADOR", sesion.getRol());
        assertNull(sesion.getContrasena());
    }

    @Test
    void iniciarSesionConUnaClaveIncorrectaDevuelveNulo() {
        assertNull(logica.iniciarSesion(new LoginDTO("admin", "incorrecta")));
    }

    @Test
    void iniciarSesionConUnUsuarioInexistenteDevuelveNulo() {
        assertNull(logica.iniciarSesion(new LoginDTO("000000000", "x")));
    }

    @Test
    void cambiarClaveDejaEntrarSoloConLaNueva() {
        logica.cambiarClave(usuario.getId(), DatosDePrueba.CLAVE, "Nueva456", "Nueva456");

        assertNotNull(logica.iniciarSesion(new LoginDTO(usuario.getId(), "Nueva456")));
        assertNull(logica.iniciarSesion(new LoginDTO(usuario.getId(), DatosDePrueba.CLAVE)));
    }

    @Test
    void cambiarClaveExigeLaClaveActualCorrecta() {
        assertEquals("ID o clave actual incorrectos", errorAlCambiar("equivocada", "Nueva456", "Nueva456"));
    }

    @Test
    void cambiarClaveExigeUnaClaveNueva() {
        assertEquals("La clave nueva no puede estar vacía", errorAlCambiar(DatosDePrueba.CLAVE, " ", " "));
        assertEquals("La clave nueva no puede estar vacía", errorAlCambiar(DatosDePrueba.CLAVE, null, null));
    }

    @Test
    void cambiarClaveExigeQueLaConfirmacionCoincida() {
        assertEquals("Las claves nuevas no coinciden",
                errorAlCambiar(DatosDePrueba.CLAVE, "Nueva456", "Distinta"));
    }

    @Test
    void cambiarClaveExigeQueSeaDistintaDeLaActual() {
        assertEquals("La clave nueva debe ser distinta de la actual",
                errorAlCambiar(DatosDePrueba.CLAVE, DatosDePrueba.CLAVE, DatosDePrueba.CLAVE));
    }

    @Test
    void cambiarClaveNoModificaNadaCuandoFalla() {
        errorAlCambiar(DatosDePrueba.CLAVE, "Nueva456", "Distinta");

        assertTrue(logica.iniciarSesion(new LoginDTO(usuario.getId(), DatosDePrueba.CLAVE)) != null);
    }
}
