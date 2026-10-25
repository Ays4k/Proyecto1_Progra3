package SRR.Proxy;

import SRR.DTO.Protocolo;
import SRR.DTO.RecursoDTO;

import java.util.List;

// Representa en el frontend al RecursoServicio del backend
public class RecursoProxy extends ProxyBase {

    public List<RecursoDTO> obtenerRecursos() {
        return enviarLista(Protocolo.OBTENER_RECURSOS);
    }

    public int guardarRecurso(RecursoDTO recurso) {
        Integer resultado = enviar(Protocolo.GUARDAR_RECURSO, recurso);
        return resultado;
    }

    public void eliminarRecurso(String id) {
        enviar(Protocolo.ELIMINAR_RECURSO, id);
    }

    public RecursoDTO buscarPorId(String id) {
        return enviar(Protocolo.BUSCAR_RECURSO_POR_ID, id);
    }

    public List<RecursoDTO> buscarPorDescripcion(String descripcion) {
        return enviarLista(Protocolo.BUSCAR_RECURSO_POR_DESCRIPCION, descripcion);
    }

    public List<RecursoDTO> obtenerRecursosPorCategoria(String idCategoria) {
        return enviarLista(Protocolo.OBTENER_RECURSOS_POR_CATEGORIA, idCategoria);
    }

    public List<RecursoDTO> filtrarRecursos(String idCategoria, String descripcion) {
        return enviarLista(Protocolo.FILTRAR_RECURSOS, idCategoria, descripcion);
    }
}