package SRR.Logica;

import SRR.DTO.ReservaAiDTO;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GeminiServiceIT {

    @Test
    @DisplayName("Gemini responde con texto parseable y la app procesa la reserva generada")
    void geminiDevuelveRespuestaProcesable() throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(),
                "Se requiere GEMINI_API_KEY para ejecutar esta prueba de integración real");

        String prompt = "Necesito una reunión de trabajo para 10 personas el 14 de octubre de 8am a 10am en una sala con laptop";
        GeminiService geminiService = new GeminiService();

        String respuesta;
        try{
            respuesta = geminiService.enviarMensaje(prompt);
        }catch (Exception e){
            respuesta = e.getMessage();
        }

        assertNotNull(respuesta, "La respuesta de Gemini no debe ser nula");
        assertFalse(respuesta.isBlank(), "La respuesta de Gemini no debe estar vacía");
        assertTrue(respuesta.toLowerCase().contains("completo") || respuesta.toLowerCase().contains("error"),
                "La respuesta debe seguir el formato esperado por la app: COMPLETO,... o Error,...");

        ReservaLogica reservaLogica = new ReservaLogica();
        ReservaAiDTO reservaAi = assertDoesNotThrow(() -> reservaLogica.crearReservaAi(prompt),
                "La aplicación debe poder parsear la respuesta de Gemini sin errores");

        assertNotNull(reservaAi, "La reserva IA debe construirse correctamente");
        assertNotNull(reservaAi.getActividad(), "La actividad no debe ser nula");
        assertNotNull(reservaAi.getFecha(), "La fecha no debe ser nula");
        assertNotNull(reservaAi.getHoraInicio(), "La hora de inicio no debe ser nula");
        assertNotNull(reservaAi.getHoraFinal(), "La hora final no debe ser nula");
        assertNotNull(reservaAi.getCategorias(), "Las categorías no deben ser nulas");
        assertFalse(reservaAi.getCategorias().isEmpty(), "Debe haber al menos una categoría");
    }

    @Test
    @DisplayName("Gemini responde con texto NO parseable y no se procesa la reserva")
    void geminiDevuelveRespuestaNOProcesable() throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(),
                "Se requiere GEMINI_API_KEY para ejecutar esta prueba de integración real");

        String prompt = "Necesito que me digas como está hoy el clima en andorra";
        GeminiService geminiService = new GeminiService();

        String respuesta;
        try{
            respuesta = geminiService.enviarMensaje(prompt);
        }catch (Exception e){
            respuesta = e.getMessage();
        }

        assertNotNull(respuesta, "La respuesta de Gemini no debe ser nula");
        assertFalse(respuesta.isBlank(), "La respuesta de Gemini no debe estar vacía");
        assertTrue(respuesta.toLowerCase().contains("error"),
                "La respuesta debe ser con el formato para recibir errores");

        ReservaLogica reservaLogica = new ReservaLogica();
        assertThrows(RuntimeException.class,() -> reservaLogica.crearReservaAi(prompt),
                "Debería dar error al no cumplir la función que se le pidió");

    }
}
