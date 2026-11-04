package SRR.DTO;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializacionTest {

    @SuppressWarnings("unchecked")
    private static <T> T copiar(T objeto) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(objeto);
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) entrada.readObject();
        }
    }

    @Test
    void respuestaOkNoEsError() {
        Respuesta respuesta = Respuesta.ok("valor");

        assertFalse(respuesta.esError());
        assertEquals("valor", respuesta.getResultado());
        assertNull(respuesta.getError());
    }

    @Test
    void respuestaConErrorNoTieneResultado() {
        Respuesta respuesta = Respuesta.error("fallo");

        assertTrue(respuesta.esError());
        assertEquals("fallo", respuesta.getError());
        assertNull(respuesta.getResultado());
    }

    @Test
    void respuestaOkConNuloNoEsError() {
        assertFalse(Respuesta.ok(null).esError());
    }

    @Test
    void respuestaViajaPorSerializacionConUnaListaDeDtos() throws Exception {
        List<UsuarioDTO> usuarios = new ArrayList<>();
        usuarios.add(new UsuarioDTO("1", "Ana", "8888", null, "FUNCIONARIO"));

        Respuesta copia = copiar(Respuesta.ok(usuarios));

        @SuppressWarnings("unchecked")
        List<UsuarioDTO> recibidos = (List<UsuarioDTO>) copia.getResultado();
        assertEquals(1, recibidos.size());
        assertEquals("Ana", recibidos.get(0).getNombre());
    }

    @Test
    void peticionViajaPorSerializacionConSusParametros() throws Exception {
        Peticion copia = copiar(new Peticion(Protocolo.INICIAR_SESION, new LoginDTO("1", "clave")));

        assertEquals(Protocolo.INICIAR_SESION, copia.getMetodo());
        assertEquals(1, copia.getParametros().length);
        assertEquals("clave", ((LoginDTO) copia.getParametros()[0]).getContrasena());
    }

    @Test
    void peticionSinParametrosTieneArregloVacio() {
        assertEquals(0, new Peticion("operacion").getParametros().length);
    }

    @Test
    void alertaViajaPorSerializacionConTodosSusCampos() throws Exception {
        AlertaReservaDTO original = new AlertaReservaDTO("R1", "123", "Ana", "Reunión",
                "2030-01-01", "09:00", "10:00", List.of("Sala", "Laptop"));

        AlertaReservaDTO copia = copiar(original);

        assertEquals("R1", copia.getIdReserva());
        assertEquals("123", copia.getIdFuncionario());
        assertEquals("Ana", copia.getEncargado());
        assertEquals("Reunión", copia.getActividad());
        assertEquals("2030-01-01", copia.getFecha());
        assertEquals("09:00", copia.getHoraInicio());
        assertEquals("10:00", copia.getHoraFin());
        assertEquals(List.of("Sala", "Laptop"), copia.getNombresRecursos());
    }

    @Test
    void elConstructorAnteriorDeLaAlertaDejaElCreadorSinDefinir() {
        AlertaReservaDTO alerta = new AlertaReservaDTO("R1", "Ana", "Reunión",
                "2030-01-01", "09:00", "10:00", List.of());

        assertNull(alerta.getIdFuncionario());
        assertEquals("Ana", alerta.getEncargado());
    }

    @Test
    void usuarioCopiadoNoConservaLaContrasena() {
        UsuarioDTO original = new UsuarioDTO("1", "Ana", "8888", "secreta", "FUNCIONARIO");

        UsuarioDTO copia = new UsuarioDTO(original);

        assertNull(copia.getContrasena());
        assertEquals("Ana", copia.getNombre());
        assertEquals("FUNCIONARIO", copia.getRol());
    }
}
