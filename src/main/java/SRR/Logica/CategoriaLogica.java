package SRR.Logica;
import SRR.Datos.CategoriaDatos;
import SRR.DTO.CategoriaDTO;

import java.util.ArrayList;
import java.util.List;

public class CategoriaLogica {

    private CategoriaDatos datos = new CategoriaDatos();
    
    public List<CategoriaDTO> obtenerCategorias() {
        datos.deserializar();   // relee por si otra pantalla agrego o modifico una categoria
        return datos.listar();
    }

    public CategoriaDTO buscarPorId(String id) {
        for (CategoriaDTO categoria : datos.listar()) {
            if (categoria.getId().equals(id)) {
                return categoria;
            }
        }
        return null;
    }

    public List<CategoriaDTO> buscarPorDescripcion(String texto) {
        List<CategoriaDTO> resultado = new ArrayList<>();
        if (texto == null) {
            return resultado;
        }
        String busqueda = texto.toLowerCase();
        for (CategoriaDTO categoria : datos.listar()) {
            String descripcion = categoria.getDescripcion();
            if (descripcion != null && descripcion.toLowerCase().contains(busqueda)) {
                resultado.add(categoria);
            }
        }
        return resultado;
    }

    public int agregarCategoria(CategoriaDTO categoria) {
        CategoriaDTO busqueda = buscarPorId(categoria.getId());

        if(busqueda == null){
            datos.agregar(categoria);
            return 1; // Categoría agregada exitosamente
        } else {
            datos.modificar(categoria);
            return 2; // Categoría modificada exitosamente
        }
    }

    public void eliminarCategoria(String id) {
        datos.borrar(id);
    }
}
