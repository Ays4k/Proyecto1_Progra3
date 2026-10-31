package SRR.Proxy;

import SRR.Comunicacion.Conexion;
import SRR.Comunicacion.ServidorException;

import SRR.DTO.Peticion;
import SRR.DTO.Protocolo;
import SRR.DTO.Respuesta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Lo que tienen en comun los cinco proxies: armar la Peticion, enviarla por la Conexion
// y revisar la Respuesta. Si el backend mando un error, se lanza como ServidorException
// para que el controlador muestre el mensaje.
abstract class ProxyBase {

    // Metodos que solo leen datos: si se repiten no cambia nada, asi que es seguro
    // reintentarlos una vez si se corto la conexion (ver Conexion.enviarConsulta).
    private static final Set<String> CONSULTAS = Set.of(
            Protocolo.INICIAR_SESION,
            Protocolo.OBTENER_USUARIOS, Protocolo.BUSCAR_USUARIO_POR_ID,
            Protocolo.NOMBRE_DE_USUARIO, Protocolo.BUSCAR_USUARIO_POR_NOMBRE,
            Protocolo.OBTENER_CATEGORIAS, Protocolo.BUSCAR_CATEGORIA_POR_ID,
            Protocolo.OBTENER_RECURSOS, Protocolo.BUSCAR_RECURSO_POR_ID,
            Protocolo.BUSCAR_RECURSO_POR_DESCRIPCION, Protocolo.OBTENER_RECURSOS_POR_CATEGORIA,
            Protocolo.FILTRAR_RECURSOS,
            Protocolo.RESERVAS_ACTIVAS_DE, Protocolo.RECURSO_DISPONIBLE,
            Protocolo.RECURSO_DISPONIBLE_EN_HORARIO, Protocolo.RECURSOS_DISPONIBLES,
            Protocolo.OBTENER_TODAS_LAS_RESERVAS
    );

    protected <T> T enviar(String metodo, Object... parametros) {
        Conexion conexion = Conexion.getInstance();
        // Si la IA esta usando la conexion, esta peticion tendria que esperar a que termine
        // y la ventana se congelaria. En vez de eso se avisa de inmediato.
        if (conexion.isEsperandoIa()) {
            throw new ServidorException("La IA todavía está procesando su solicitud. "
                    + "Espere a que termine e intente de nuevo.");
        }
        Peticion peticion = new Peticion(metodo, parametros);
        Respuesta respuesta = CONSULTAS.contains(metodo)
                ? conexion.enviarConsulta(peticion)
                : conexion.enviar(peticion);
        return revisar(respuesta);
    }

    // Para la IA: usa un tiempo de espera mas largo y marca la conexion como ocupada
    // mientras dura, para que las demas peticiones avisen en vez de quedarse esperando.
    protected <T> T enviarConEspera(int tiempoMs, String metodo, Object... parametros) {
        Conexion conexion = Conexion.getInstance();
        conexion.setEsperandoIa(true);
        try {
            return revisar(conexion.enviar(new Peticion(metodo, parametros), tiempoMs));
        } finally {
            conexion.setEsperandoIa(false);   // siempre se libera, aunque falle
        }
    }

    // Devuelve siempre un ArrayList que se puede modificar. La lista que llega por el
    // socket puede ser inmutable, y los controladores le hacen add/remove.
    protected <E> List<E> enviarLista(String metodo, Object... parametros) {
        List<E> lista = enviar(metodo, parametros);
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    // Si la Respuesta trae error lo lanza, y si no, devuelve el resultado.
    // El cast a T da una advertencia porque Java no puede verificarlo, pero es seguro, ya que
    // cada proxy pide el tipo que el backend devuelve segun Protocolo.
    @SuppressWarnings("unchecked")
    private <T> T revisar(Respuesta respuesta) {
        if (respuesta.esError()) {
            throw new ServidorException(respuesta.getError());
        }
        return (T) respuesta.getResultado();
    }
}