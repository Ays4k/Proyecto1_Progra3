package SRR.Proxy;

import SRR.Comunicacion.Conexion;
import SRR.Comunicacion.ServidorException;

import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;

import java.util.ArrayList;
import java.util.List;

// Lo que tienen en comun los cinco proxies: armar la Peticion, enviarla por la Conexion
// y revisar la Respuesta. Si el backend mando un error, se lanza como ServidorException
// para que el controlador muestre el mensaje.
abstract class ProxyBase {

    protected <T> T enviar(String metodo, Object... parametros) {
        return revisar(Conexion.getInstance().enviar(new Peticion(metodo, parametros)));
    }

    // Igual que enviar, pero con un tiempo de espera distinto (se usa para la IA)
    protected <T> T enviarConEspera(int tiempoMs, String metodo, Object... parametros) {
        return revisar(Conexion.getInstance().enviar(new Peticion(metodo, parametros), tiempoMs));
    }

    // Devuelve siempre un ArrayList que se puede modificar. La lista que llega por el
    // socket puede ser inmutable, y los controladores le hacen add/remove.
    protected <E> List<E> enviarLista(String metodo, Object... parametros) {
        List<E> lista = enviar(metodo, parametros);
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    @SuppressWarnings("unchecked")
    private <T> T revisar(Respuesta respuesta) {
        if (respuesta.esError()) {
            throw new ServidorException(respuesta.getError());
        }
        return (T) respuesta.getResultado();
    }
}