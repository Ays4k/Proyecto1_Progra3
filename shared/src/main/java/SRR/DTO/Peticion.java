package SRR.DTO;

import java.io.Serializable;

public class Peticion implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String metodo;        // ej: Protocolo.CREAR_RESERVA
    private final Object[] parametros;  // ej: { "123", "Reunión", ... }

    public Peticion(String metodo, Object... parametros) {
        this.metodo = metodo;
        this.parametros = parametros;
    }

    public String getMetodo() {
        return metodo;
    }

    public Object[] getParametros() {
        return parametros;
    }
}
