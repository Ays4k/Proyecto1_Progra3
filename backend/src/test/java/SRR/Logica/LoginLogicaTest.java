package SRR.Logica;

import SRR.DTO.LoginDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginLogicaTest {

    private final LoginLogica logica = new LoginLogica();

    @Test
    void iniciarSesionSinDatosDevuelveNulo() {
        assertNull(logica.iniciarSesion(null));
        assertNull(logica.iniciarSesion(new LoginDTO(null, "clave")));
        assertNull(logica.iniciarSesion(new LoginDTO("  ", "clave")));
    }

    @Test
    void cambiarClaveExigeLaIdentificacion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> logica.cambiarClave(null, "a", "b", "b"));
        assertEquals("Debe indicar su ID", error.getMessage());

        error = assertThrows(IllegalArgumentException.class,
                () -> logica.cambiarClave(" ", "a", "b", "b"));
        assertEquals("Debe indicar su ID", error.getMessage());
    }
}
