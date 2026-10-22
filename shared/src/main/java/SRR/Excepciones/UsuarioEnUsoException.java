package SRR.Excepciones;

public class UsuarioEnUsoException extends RuntimeException {

    public UsuarioEnUsoException(String mensaje) {
        super(mensaje);
    }
}