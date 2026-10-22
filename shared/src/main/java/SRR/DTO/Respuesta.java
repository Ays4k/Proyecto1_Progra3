package SRR.DTO;

import java.io.Serializable;

/**
 * Mensaje que el backend devuelve al frontend.
 * Si todo salió bien, "error" es null y "resultado" trae el valor (puede ser null si el método es void).
 * Si algo falló, "error" trae el mensaje y "resultado" es null.
 */
public class Respuesta implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object resultado;
    private final String error;

    public Respuesta(Object resultado, String error) {
        this.resultado = resultado;
        this.error = error;
    }

    public static Respuesta ok(Object resultado) {
        return new Respuesta(resultado, null);
    }

    public static Respuesta error(String mensaje) {
        return new Respuesta(null, mensaje);
    }

    public boolean esError() {
        return error != null;
    }

    public Object getResultado() {
        return resultado;
    }

    public String getError() {
        return error;
    }
}
