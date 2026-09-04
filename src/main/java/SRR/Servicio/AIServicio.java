package SRR.Servicio;
import SRR.Logica.GeminiService;

import java.io.IOException;


public class AIServicio {

    private GeminiService servicio;

    public String enviarMensaje(String texto) throws IOException, InterruptedException{
        return servicio.enviarMensaje(texto);
    }

}
