package SRR.Servidor.Enrutadores;

import SRR.DTO.LoginDTO;
import SRR.DTO.Peticion;
import SRR.DTO.Protocolo;
import SRR.DTO.Respuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrutadorPrincipalTest {

    private EnrutadorPrincipal enrutador;

    @BeforeEach
    void crearEnrutador() {
        enrutador = new EnrutadorPrincipal(alerta -> { });
    }

    @Test
    void manejaTodasLasOperacionesDelProtocolo() throws IllegalAccessException {
        List<String> sinManejar = new ArrayList<>();
        for (Field campo : Protocolo.class.getFields()) {
            boolean esOperacion = campo.getType() == String.class
                    && Modifier.isStatic(campo.getModifiers())
                    && !campo.getName().equals("HOST_POR_DEFECTO");
            if (esOperacion && !enrutador.maneja((String) campo.get(null))) {
                sinManejar.add(campo.getName());
            }
        }
        assertTrue(sinManejar.isEmpty(), "Operaciones del protocolo sin enrutador: " + sinManejar);
    }

    @Test
    void noManejaUnaOperacionDesconocida() {
        assertFalse(enrutador.maneja("operacionQueNoExiste"));
    }

    @Test
    void unaOperacionDesconocidaDevuelveError() throws Exception {
        Respuesta respuesta = enrutador.atender(new Peticion("operacionQueNoExiste"));

        assertTrue(respuesta.esError());
        assertTrue(respuesta.getError().contains("operacionQueNoExiste"));
    }

    @Test
    void unInicioDeSesionSinIdentificacionDevuelveNuloSinConsultarLaBase() throws Exception {
        Respuesta respuesta = enrutador.atender(new Peticion(Protocolo.INICIAR_SESION, new LoginDTO(" ", "x")));

        assertFalse(respuesta.esError());
        assertNull(respuesta.getResultado());
    }

    @Test
    void noSePuedeCrearConUnObservadorNulo() {
        assertThrows(NullPointerException.class, () -> new EnrutadorPrincipal(null));
    }
}
