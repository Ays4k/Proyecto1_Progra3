package SRR.Excepciones;

public class CategoriaEnUsoException extends RuntimeException {

    public CategoriaEnUsoException(String mensaje) {
        super(mensaje);
    }
}