package SRR.Comunicacion;

// Unica excepcion que reciben los controladores. Trae el mensaje que hay que mostrarle
// al usuario: puede ser un error que mando el backend o un problema de conexion.
public class ServidorException extends RuntimeException {

    // Solo el mensaje. Se usa cuando el backend manda un error (por ejemplo una
    // validacion), del lado del frontend no hay ninguna excepcion original.
    public ServidorException(String mensaje) {
        super(mensaje);
    }

    // Mensaje y excepcion original. Se usa cuando falla la red. La causa no la ve el
    // usuario, sirve para depurar y para saber que el error fue de conexion.
    public ServidorException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    // true si el error fue de conexion (servidor apagado, tiempo agotado, conexion perdida)
    // y false si fue un error que mando el backend. Los de conexion se crean con causa.
    public boolean esProblemaDeConexion() {
        return getCause() != null;
    }
}