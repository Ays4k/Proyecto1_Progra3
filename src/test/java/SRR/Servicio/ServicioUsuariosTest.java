package SRR.Servicio;

import SRR.DTO.UsuarioDTO;
import org.junit.jupiter.api.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioUsuariosTest {

    static private UsuarioServicio usuarioServicio = new UsuarioServicio("datosPruebas/usuarios.json");


    @BeforeAll
    static void setUp() {
        UsuarioDTO usuario1 = new UsuarioDTO(null, "Isaac", "20225432", "UsuarioPrueba", "ADMINISTRADOR");
        UsuarioDTO usuario2 = new UsuarioDTO(null, "Saul", "12345678", "UsuarioPrueba1", "FUNCIONARIO");
        usuarioServicio.cambiosUsuario(usuario1);
        usuarioServicio.cambiosUsuario(usuario2);
    }

    @AfterAll
    static void tearDown(){
        List<UsuarioDTO> usuarios = usuarioServicio.obtenerUsuarios();

        for(UsuarioDTO usuario : usuarios){
            usuarioServicio.eliminarUsuario(usuario.getId());
        }
    }

    @Test
    @DisplayName("Busca un usuario por id y devuelve sus datos")
    void buscarUsuarioPorId() {
        String id = usuarioServicio.buscarPorNombre("Saul").getFirst().getId();
        UsuarioDTO usuario = usuarioServicio.buscarPorId(id);

        assertNotNull(usuario);
        assertEquals("Saul", usuario.getNombre());
        assertEquals("FUNCIONARIO", usuario.getRol());
    }

    @Test
    @DisplayName("Busca usuarios por nombre y filtra la coincidencia")
    void buscarUsuarioPorNombre() {
        List<UsuarioDTO> usuarios = usuarioServicio.buscarPorNombre("saul");

        assertNotNull(usuarios);
        assertEquals(1, usuarios.size());
        assertEquals("Saul", usuarios.get(0).getNombre());
    }

    @Test
    @DisplayName("Agrega un usuario nuevo y lo guarda correctamente")
    void agregarUsuarioNuevo() {
        UsuarioDTO nuevo = new UsuarioDTO(null, "Ana", "55555555", "ClaveAna", "FUNCIONARIO");

        int resultado = usuarioServicio.cambiosUsuario(nuevo);

        assertEquals(1, resultado);
        assertNotNull(usuarioServicio.buscarPorNombre("Ana").getFirst());
    }

    @Test
    @DisplayName("Actualiza un usuario existente y mantiene la contraseña anterior")
    void actualizarUsuarioExistente() {
        String telefonoNuevo = "1231231";
        UsuarioDTO usuario = usuarioServicio.buscarPorNombre("Saul").getFirst();
        usuario.setTelefono(telefonoNuevo);

        int resultado = usuarioServicio.cambiosUsuario(usuario);
        UsuarioDTO cambio = usuarioServicio.buscarPorId(usuario.getId());

        assertEquals(2, resultado);
        assertNotNull(usuario);
        assertEquals(telefonoNuevo, usuario.getTelefono());
    }

    @Test
    @DisplayName("Elimina un usuario existente")
    void eliminarUsuario() {
        String usuarioEliminadoId = usuarioServicio.obtenerUsuarios().getFirst().getId();
        boolean eliminado = usuarioServicio.eliminarUsuario(usuarioEliminadoId);

        assertTrue(eliminado);
        assertNull(usuarioServicio.buscarPorId(usuarioEliminadoId));
    }

    @Test
    @DisplayName("Al obtener una lista no se muestran las constraseñas")
    void verificarContrasenas(){
        List<UsuarioDTO> usuarios = usuarioServicio.obtenerUsuarios();

        for(UsuarioDTO usuario : usuarios){
            assertNull(usuario.getContrasena());
        }
    }
}
