package UnitTest;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Servicio.LoginServicio;
import SRR.Servicio.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioAutenticaciónTest {

    private UsuarioServicio usuarioServicio;
    private LoginServicio loginServicio;

    @BeforeEach
    void setup(){
        usuarioServicio = new UsuarioServicio("datosPruebas/usuarios.json");
        loginServicio = new LoginServicio("datosPruebas/usuarios.json");

        usuarioServicio.cambiosUsuario(new UsuarioDTO("1010","Isaac","20225432","UsuarioPrueba","ADMINISTRADOR"));
        usuarioServicio.cambiosUsuario(new UsuarioDTO("2345","Saul","12345678","UsuarioPrueba1","FUNCIONARIO" ));

    }

    @Test
    @DisplayName("Login fallido con contraseña incorredta")
    void testLoginIncorrecto(){
        UsuarioDTO user = loginServicio.iniciarSesion(new LoginDTO("1010","incorrecto"));
        assertNull(user,"El login debe volver null cuando la contraseña es incorrecta");
    }

    @Test
    @DisplayName("Login exitoso devuelve el objeto Usuario")
    void testLoginExitoso() {
        // Act: Ejecutamos el login con credenciales correctas
        UsuarioDTO usuario = loginServicio.iniciarSesion(new LoginDTO("1010", "UsuarioPrueba"));

        // Assert: Verificamos que NO sea null y que contenga los datos correctos
        assertNotNull(usuario, "El login debería retornar un objeto Usuario, no null");
        assertEquals("1010", usuario.getId());
        assertEquals("ADMINISTRADOR", usuario.getRol());
    }

    @Test
    @DisplayName("Login fallido usuario no existe")
    void testLoginUsuarioInexistente(){
        UsuarioDTO usuario = loginServicio.iniciarSesion(new LoginDTO("3021","Contrasena"));
        assertNull(usuario,"El login debe devolver null cuando no existe el usuario");
    }
}
