package SRR.Logica;

import SRR.DTO.UsuarioDTO;
import SRR.Excepciones.UsuarioEnUsoException;
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

class UsuarioLogicaIT {

    private final UsuarioLogica logica = new UsuarioLogica();
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
    void cambiosUsuarioCreaUnUsuarioNuevoYDevuelveUno() {
        String id = DatosDePrueba.cedulaNueva();
        prueba.registrarUsuario(id);

        int resultado = logica.cambiosUsuario(new UsuarioDTO(id, "Nuevo", "8888-0000", "clave", "FUNCIONARIO"));

        assertEquals(1, resultado);
        assertNotNull(logica.buscarPorId(id));
    }

    @Test
    void cambiosUsuarioModificaUnUsuarioExistenteYDevuelveDos() {
        UsuarioDTO existente = prueba.usuario("FUNCIONARIO");

        int resultado = logica.cambiosUsuario(
                new UsuarioDTO(existente.getId(), "Cambiado", "1234-5678", null, "ADMINISTRADOR"));

        assertEquals(2, resultado);
        assertEquals("Cambiado", logica.buscarPorId(existente.getId()).getNombre());
        assertEquals("ADMINISTRADOR", logica.buscarPorId(existente.getId()).getRol());
    }

    @Test
    void cambiosUsuarioRechazaUnaCedulaNuevaConFormatoInvalido() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                logica.cambiosUsuario(new UsuarioDTO("abc123", "Nuevo", "8888-0000", "clave", "FUNCIONARIO")));

        assertEquals("La cédula debe tener solo números (entre 9 y 12 dígitos).", error.getMessage());
        assertNull(logica.buscarPorId("abc123"));
    }

    @Test
    void cambiosUsuarioRechazaUnaCedulaNuevaDemasiadoCorta() {
        assertThrows(IllegalArgumentException.class, () ->
                logica.cambiosUsuario(new UsuarioDTO("12345", "Nuevo", "8888-0000", "clave", "FUNCIONARIO")));
    }

    @Test
    void nombreDeDevuelveElNombreODesconocido() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        assertEquals(usuario.getNombre(), logica.nombreDe(usuario.getId()));
        assertEquals("Usuario desconocido", logica.nombreDe("000000000"));
        assertEquals("Usuario desconocido", logica.nombreDe(null));
    }

    @Test
    void buscarPorNombreNoDistingueMayusculas() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        List<UsuarioDTO> encontrados = logica.buscarPorNombre(usuario.getNombre().toUpperCase());

        assertTrue(encontrados.stream().anyMatch(u -> usuario.getId().equals(u.getId())));
    }

    @Test
    void eliminarUnUsuarioInexistenteDevuelveFalso() {
        assertFalse(logica.eliminarUsuario("000000000"));
    }

    @Test
    void eliminarUnUsuarioSinReservasLoBorra() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");

        assertTrue(logica.eliminarUsuario(usuario.getId()));
        assertNull(logica.buscarPorId(usuario.getId()));
    }

    @Test
    void eliminarUnUsuarioConReservasActivasLanzaUsuarioEnUso() {
        UsuarioDTO usuario = prueba.usuario("FUNCIONARIO");
        prueba.reserva(usuario.getId(), List.of(), DatosDePrueba.fechaFutura(), "09:00", "10:00", "ACTIVA");

        assertThrows(UsuarioEnUsoException.class, () -> logica.eliminarUsuario(usuario.getId()));
        assertNotNull(logica.buscarPorId(usuario.getId()));
    }
}
