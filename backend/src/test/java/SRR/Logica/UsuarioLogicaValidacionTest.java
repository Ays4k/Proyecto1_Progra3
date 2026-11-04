package SRR.Logica;

import SRR.DTO.UsuarioDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioLogicaValidacionTest {

    private final UsuarioLogica logica = new UsuarioLogica();

    private static UsuarioDTO usuario(String nombre, String telefono) {
        return new UsuarioDTO("123456789", nombre, telefono, "clave", "FUNCIONARIO");
    }

    private String errorAlGuardar(UsuarioDTO usuario) {
        return assertThrows(IllegalArgumentException.class, () -> logica.cambiosUsuario(usuario)).getMessage();
    }

    @Test
    void exigeUnNombre() {
        assertEquals("El nombre no puede estar vacío.", errorAlGuardar(usuario(null, "8888-0000")));
        assertEquals("El nombre no puede estar vacío.", errorAlGuardar(usuario("  ", "8888-0000")));
    }

    @Test
    void limitaElNombreACincuentaCaracteres() {
        assertEquals("El nombre no puede tener más de 50 caracteres.",
                errorAlGuardar(usuario("x".repeat(51), "8888-0000")));
    }

    @Test
    void exigeUnTelefono() {
        assertEquals("El teléfono no puede estar vacío.", errorAlGuardar(usuario("Ana", null)));
        assertEquals("El teléfono no puede estar vacío.", errorAlGuardar(usuario("Ana", " ")));
    }

    @Test
    void limitaElTelefonoAQuinceCaracteres() {
        assertEquals("El teléfono no puede tener más de 15 caracteres.",
                errorAlGuardar(usuario("Ana", "1".repeat(16))));
    }

    @Test
    void buscarPorNombreConTextoNuloDevuelveListaVacia() {
        assertTrue(logica.buscarPorNombre(null).isEmpty());
    }
}
