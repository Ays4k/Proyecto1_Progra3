package SRR.Servicio;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioAutenticaciónTest {

    static UsuarioServicio usuarioServicio;
    static LoginServicio loginServicio;

    static String id1;
    static String id2;
    @BeforeAll
    static void setup(){
        usuarioServicio = new UsuarioServicio("datosPruebas/usuarios.json");
        loginServicio = new LoginServicio("datosPruebas/usuarios.json");

        usuarioServicio.cambiosUsuario(new UsuarioDTO(null,"Isaac","20225432","UsuarioPrueba","ADMINISTRADOR"));
        usuarioServicio.cambiosUsuario(new UsuarioDTO(null,"Saul","12345678","UsuarioPrueba1","FUNCIONARIO" ));
        id1=usuarioServicio.buscarPorNombre("Isaac").getFirst().getId();
        id2=usuarioServicio.buscarPorNombre("Saul").getFirst().getId();

    }

    @AfterAll
    static void tearDown(){
        List<UsuarioDTO> usuarios = usuarioServicio.obtenerUsuarios();

        for(UsuarioDTO usuario : usuarios){
            usuarioServicio.eliminarUsuario(usuario.getId());
        }
    }
    @Test
    @DisplayName("Login fallido con contraseña incorredta")
    void testLoginIncorrecto(){
        UsuarioDTO user = loginServicio.iniciarSesion(new LoginDTO(id1,"incorrecto"));
        assertNull(user,"El login debe volver null cuando la contraseña es incorrecta");
    }

    @Test
    @DisplayName("Login exitoso devuelve el objeto Usuario")
    void testLoginExitoso() {
        // Act: Ejecutamos el login con credenciales correctas
        UsuarioDTO usuario = loginServicio.iniciarSesion(new LoginDTO(id1, "UsuarioPrueba"));

        // Assert: Verificamos que NO sea null y que contenga los datos correctos
        assertNotNull(usuario, "El login debería retornar un objeto Usuario, no null");
        assertEquals(id1, usuario.getId());
        assertEquals("ADMINISTRADOR", usuario.getRol());
    }

    @Test
    @DisplayName("Login fallido usuario no existe")
    void testLoginUsuarioInexistente(){
        UsuarioDTO usuario = loginServicio.iniciarSesion(new LoginDTO("3021","Contrasena"));
        assertNull(usuario,"El login debe devolver null cuando no existe el usuario");
    }
}
