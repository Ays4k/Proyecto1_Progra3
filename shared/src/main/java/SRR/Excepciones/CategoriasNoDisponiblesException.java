package SRR.Excepciones;

import java.util.List;

public class CategoriasNoDisponiblesException extends RuntimeException {

    private final List<String> categorias;

    public CategoriasNoDisponiblesException(List<String> categorias) {
        super("No hay disponibilidad en: " + String.join(", ", categorias));
        this.categorias = categorias;
    }

    public List<String> getCategorias() {
        return categorias;
    }
}
