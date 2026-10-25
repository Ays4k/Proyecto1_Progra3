package SRR.Proxy;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.Protocolo;

import java.util.List;

// Representa en el frontend al CategoriaServicio del backend
public class CategoriaProxy extends ProxyBase {

    public List<CategoriaDTO> obtenerCategorias() {
        return enviarLista(Protocolo.OBTENER_CATEGORIAS);
    }

    // Mismo nombre que en CategoriaServicio para no cambiar el controlador
    public int servicioCategorias(CategoriaDTO categoria) {
        Integer resultado = enviar(Protocolo.GUARDAR_CATEGORIA, categoria);
        return resultado;
    }

    public void eliminarCategoria(String id) {
        enviar(Protocolo.ELIMINAR_CATEGORIA, id);
    }

    public CategoriaDTO buscarPorId(String id) {
        return enviar(Protocolo.BUSCAR_CATEGORIA_POR_ID, id);
    }
}