package SRR.Comunicacion;

// Unica excepcion que reciben los controladores. Trae el mensaje que hay que mostrarle
// al usuario: puede ser un error que mando el backend o un problema de conexion.
public class ServidorException extends RuntimeException {

    // Solo el mensaje. Se usa cuando el backend manda un error: del lado del
    // frontend no hay ninguna excepcion original, solo el texto de la Respuesta.
    public ServidorException(String mensaje) {
        super(mensaje);
    }
    // Mensaje y excepcion original. Se usa cuando falla la red .
    // La causa no la ve el usuario, es solo para depurar.
    public ServidorException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}