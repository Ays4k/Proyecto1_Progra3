package SRR.Proxy;

import SRR.Comunicacion.Conexion;
import SRR.Comunicacion.ServidorException;
import SRR.DTO.CategoriaDTO;
import SRR.DTO.LoginDTO;
import SRR.DTO.Protocolo;
import SRR.DTO.ReservaDTO;
import SRR.DTO.UsuarioDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Pruebas de integracion de los proxies contra el backend real.
// IMPORTANTE: Antes de correrlas hay que tener MySQL con la base srr y el Servidor encendidos.
// Si el Servidor no responde, las pruebas se saltan en vez de fallar.
// Usan los datos del script sql/schema_srr.sql, el usuario user/user y la categoria Laptop.
// IMPORTANTE: el usuario user tiene que tener la clave user, si se le cambio el login falla.
// Como no hay una base aparte para pruebas, la reserva que se crea se cancela al terminar.
class ProxyIT {

    private static final String USUARIO = "user";
    private static final String CLAVE = "user";
    private static boolean hayServidor;   // se revisa una sola vez, antes de todas las pruebas

    private String idReservaCreada;   // para cancelarla al final aunque la prueba falle

    @BeforeAll
    static void verificarServidor() {
        // se conecta al Servidor real, en la direccion de config.properties y el puerto de Protocolo
        Conexion conexion = Conexion.getInstance();
        conexion.configurar(conexion.getHost(), Protocolo.PUERTO_PETICIONES, 10000);
        try {
            conexion.conectar();
            hayServidor = true;
        } catch (ServidorException e) {
            hayServidor = false;
        }
    }

    @BeforeEach
    void saltarSiNoHayServidor() {
        // se revisa en cada prueba para que Failsafe las cuente como saltadas y no como cero pruebas
        assumeTrue(hayServidor, "El Servidor no esta encendido, se saltan las pruebas de integracion");
    }

    @AfterEach
    void cancelarReservaDePrueba() {
        if (idReservaCreada != null) {
            new ReservaProxy().cancelarReserva(idReservaCreada);
            idReservaCreada = null;
        }
    }

    // Login:

    @Test
    @DisplayName("Login contra el servidor real devuelve el usuario con su rol")
    void loginCorrecto() {
        UsuarioDTO usuario = new LoginProxy().iniciarSesion(new LoginDTO(USUARIO, CLAVE));

        assertNotNull(usuario);
        assertEquals(USUARIO, usuario.getId());
        assertEquals("FUNCIONARIO", usuario.getRol());
    }

    @Test
    @DisplayName("Login con clave incorrecta contra el servidor real devuelve null")
    void loginIncorrecto() {
        assertNull(new LoginProxy().iniciarSesion(new LoginDTO(USUARIO, "clave-incorrecta")));
    }

    // Crear reserva:

    @Test
    @DisplayName("Crear una reserva en el servidor real devuelve la reserva con id y recurso asignado")
    void crearReserva() {
        String fecha = LocalDate.now().plusDays(30).toString();   // lejos, para no chocar con reservas reales
        String idLaptop = idDeCategoria("Laptop");

        ReservaDTO reserva = new ReservaProxy().crearReserva(USUARIO, "Prueba de integracion",
                fecha, "09:00", "10:00", List.of(idLaptop));
        idReservaCreada = reserva.getId();

        assertNotNull(reserva.getId());
        assertEquals("ACTIVA", reserva.getEstado());
        assertEquals(1, reserva.getIdsRecursos().size());

        // la reserva quedo guardada, aparece entre las activas del usuario
        boolean aparece = false;
        for (ReservaDTO r : new ReservaProxy().reservasActivasDe(USUARIO)) {
            if (r.getId().equals(reserva.getId())) {
                aparece = true;
            }
        }
        assertTrue(aparece, "la reserva creada deberia estar entre las activas del usuario");
    }

    @Test
    @DisplayName("Crear una reserva sin categorias devuelve el error de validacion del backend")
    void crearReservaSinCategorias() {
        String fecha = LocalDate.now().plusDays(30).toString();

        ServidorException e = assertThrows(ServidorException.class,
                () -> new ReservaProxy().crearReserva(USUARIO, "Prueba de integracion",
                        fecha, "09:00", "10:00", List.of()));

        assertTrue(e.getMessage().contains("al menos una categoría"));
        assertFalse(e.esProblemaDeConexion());
    }

    // Busca el id de una categoria por su nombre, asi la prueba no depende del numero
    private String idDeCategoria(String descripcion) {
        for (CategoriaDTO categoria : new CategoriaProxy().obtenerCategorias()) {
            if (categoria.getDescripcion().equals(descripcion)) {
                return categoria.getId();
            }
        }
        throw new IllegalStateException("No existe la categoria " + descripcion + " en la base");
    }
}