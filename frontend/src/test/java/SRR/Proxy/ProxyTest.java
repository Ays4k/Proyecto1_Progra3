package SRR.Proxy;

import SRR.Comunicacion.Conexion;
import SRR.Comunicacion.ServidorException;
import SRR.Comunicacion.ServidorFalso;
import SRR.DTO.CategoriaDTO;
import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas de unidad de los proxies y de Conexion contra el ServidorFalso.
// Cada prueba levanta un ServidorFalso nuevo en un puerto libre, con los mismos datos
// iniciales, asi las pruebas no dependen unas de otras ni del backend real.

class ProxyTest {

    private static final int TIEMPO_ESPERA_MS = 2000;

    private ServidorFalso servidor;
    private int puerto;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = new ServidorFalso(0);   // 0 = el sistema elige un puerto libre
        servidor.iniciar();
        puerto = servidor.getPuerto();
        Conexion.getInstance().configurar("localhost", puerto, TIEMPO_ESPERA_MS);
    }

    @AfterEach
    void detenerServidor() {
        Conexion.getInstance().cerrar();
        servidor.detener();
    }

    // Respuesta correcta:

    @Test
    @DisplayName("Login correcto devuelve el usuario con su rol")
    void loginCorrecto() {
        UsuarioDTO usuario = new LoginProxy().iniciarSesion(new LoginDTO("admin", "admin"));

        assertNotNull(usuario);
        assertEquals("ADMINISTRADOR", usuario.getRol());
    }

    @Test
    @DisplayName("Login con clave incorrecta devuelve null")
    void loginIncorrecto() {
        UsuarioDTO usuario = new LoginProxy().iniciarSesion(new LoginDTO("admin", "otra"));

        assertNull(usuario);
    }

    @Test
    @DisplayName("Una consulta devuelve la lista y se puede modificar")
    void consultaDevuelveLista() {
        List<CategoriaDTO> categorias = new CategoriaProxy().obtenerCategorias();

        assertEquals(4, categorias.size());
        // los controladores le hacen add a estas listas, asi que no puede ser inmutable
        categorias.add(new CategoriaDTO("99", "Otra"));
        assertEquals(5, categorias.size());
    }

    // Error del backend:

    @Test
    @DisplayName("Un error del backend llega como ServidorException con su mensaje")
    void errorDelBackend() {
        // la categoria 1 (Sala de reuniones) tiene recursos, el servidor no deja eliminarla
        ServidorException e = assertThrows(ServidorException.class,
                () -> new CategoriaProxy().eliminarCategoria("1"));

        assertTrue(e.getMessage().contains("todavía tiene recursos"));
        assertFalse(e.esProblemaDeConexion());
    }

    // Tiempo agotado:

    @Test
    @DisplayName("Si el servidor tarda mas que el tiempo de espera se lanza ServidorException")
    void tiempoAgotado() {
        Conexion.getInstance().configurar("localhost", puerto, 500);
        servidor.setRetrasoMs(1500);

        ServidorException e = assertThrows(ServidorException.class,
                () -> new CategoriaProxy().obtenerCategorias());

        assertTrue(e.getMessage().contains("tardó demasiado"));
        assertTrue(e.esProblemaDeConexion());
    }

    // Problemas de conexion:

    @Test
    @DisplayName("Con el servidor apagado se lanza ServidorException de conexion")
    void servidorApagado() {
        servidor.detener();

        ServidorException e = assertThrows(ServidorException.class,
                () -> new CategoriaProxy().obtenerCategorias());

        assertTrue(e.getMessage().contains("No se pudo conectar"));
        assertTrue(e.esProblemaDeConexion());
    }

    @Test
    @DisplayName("Una consulta se reintenta sola si se corto la conexion")
    void consultaSeReintenta() {
        new CategoriaProxy().obtenerCategorias();   // deja la conexion abierta
        servidor.cortarConexiones();   // como si el backend se hubiera reiniciado

        // el socket viejo ya no sirve, pero la consulta se reintenta con una conexion nueva
        List<CategoriaDTO> categorias = new CategoriaProxy().obtenerCategorias();

        assertEquals(4, categorias.size());
    }

    @Test
    @DisplayName("Una escritura no se reintenta si se corto la conexion")
    void escrituraNoSeReintenta() {
        new CategoriaProxy().obtenerCategorias();   // deja la conexion abierta
        servidor.cortarConexiones();   // como si el backend se hubiera reiniciado

        ServidorException e = assertThrows(ServidorException.class,
                () -> new CategoriaProxy().servicioCategorias(new CategoriaDTO(null, "Nueva")));
        assertTrue(e.getMessage().contains("Se perdió la conexión"));

        // la siguiente peticion ya funciona, y la categoria no se creo dos veces ni a medias
        assertEquals(4, new CategoriaProxy().obtenerCategorias().size());
    }

    @Test
    @DisplayName("Mientras la IA responde, otra peticion avisa en vez de quedarse esperando")
    void iaOcupada() throws InterruptedException {
        servidor.setRetrasoMs(1500);
        Thread hiloIa = new Thread(() -> new ReservaProxy().generarReservaAi("reunion manana"));
        hiloIa.start();
        Thread.sleep(300);   // le da tiempo al hilo de la IA de mandar su peticion

        long inicio = System.currentTimeMillis();
        ServidorException e = assertThrows(ServidorException.class,
                () -> new UsuarioProxy().obtenerUsuarios());
        long espera = System.currentTimeMillis() - inicio;

        assertTrue(e.getMessage().contains("La IA todavía está procesando"));
        assertTrue(espera < 500, "el aviso debe ser inmediato, no esperar a la IA");

        hiloIa.join();
        servidor.setRetrasoMs(0);
        // cuando la IA termina, la conexion queda libre otra vez
        assertFalse(Conexion.getInstance().isEsperandoIa());
        assertEquals(3, new UsuarioProxy().obtenerUsuarios().size());
    }
}