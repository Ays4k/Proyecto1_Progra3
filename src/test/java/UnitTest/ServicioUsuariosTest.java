package UnitTest;

import SRR.DTO.UsuarioDTO;
import SRR.Servicio.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioUsuariosTest {

    private UsuarioServicio usuarioServicio;


    @BeforeEach
    void setUp() {
        usuarioServicio = new UsuarioServicio("datosPruebas/usuarios.json");
        usuarioServicio.cambiosUsuario(new UsuarioDTO("1010", "Isaac", "20225432", "UsuarioPrueba", "ADMINISTRADOR"));
        usuarioServicio.cambiosUsuario(new UsuarioDTO("2345", "Saul", "12345678", "UsuarioPrueba1", "FUNCIONARIO"));
    }

    @Test
    @DisplayName("Busca un usuario por id y devuelve sus datos")
    void buscarUsuarioPorId() {
        UsuarioDTO usuario = usuarioServicio.buscarPorId("1010");

        assertNotNull(usuario);
        assertEquals("1010", usuario.getId());
        assertEquals("Isaac", usuario.getNombre());
        assertEquals("ADMINISTRADOR", usuario.getRol());
    }

    @Test
    @DisplayName("Busca usuarios por nombre y filtra la coincidencia")
    void buscarUsuarioPorNombre() {
        List<UsuarioDTO> usuarios = usuarioServicio.buscarPorNombre("saul");

        assertNotNull(usuarios);
        assertEquals(1, usuarios.size());
        assertEquals("2345", usuarios.get(0).getId());
        assertEquals("Saul", usuarios.get(0).getNombre());
    }

    @Test
    @DisplayName("Agrega un usuario nuevo y lo guarda correctamente")
    void agregarUsuarioNuevo() {
        UsuarioDTO nuevo = new UsuarioDTO("9999", "Ana", "55555555", "ClaveAna", "FUNCIONARIO");

        int resultado = usuarioServicio.cambiosUsuario(nuevo);

        assertEquals(1, resultado);
        assertNotNull(usuarioServicio.buscarPorId("9999"));
        assertEquals("Ana", usuarioServicio.buscarPorId("9999").getNombre());
    }

    @Test
    @DisplayName("Actualiza un usuario existente y mantiene la contraseña anterior")
    void actualizarUsuarioExistente() {
        UsuarioDTO actualizado = new UsuarioDTO("1010", "Isaac Actualizado", "99999999", "nuevaClave", "ADMINISTRADOR");

        int resultado = usuarioServicio.cambiosUsuario(actualizado);
        UsuarioDTO usuario = usuarioServicio.buscarPorId("1010");

        assertEquals(2, resultado);
        assertNotNull(usuario);
        assertEquals("Isaac Actualizado", usuario.getNombre());
        assertEquals("99999999", usuario.getTelefono());
    }

    @Test
    @DisplayName("Elimina un usuario existente")
    void eliminarUsuario() {
        boolean eliminado = usuarioServicio.eliminarUsuario("2345");

        assertTrue(eliminado);
        assertNull(usuarioServicio.buscarPorId("2345"));
    }
}
