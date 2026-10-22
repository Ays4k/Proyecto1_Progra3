package SRR.Logica;
import SRR.Datos.CategoriaDatos;
import SRR.DTO.CategoriaDTO;
import SRR.Excepciones.CategoriaEnUsoException;

import java.util.List;

public class CategoriaLogica {

    private CategoriaDatos datos = new CategoriaDatos();
    
    public List<CategoriaDTO> obtenerCategorias() {
        return datos.listar();
    }

    public CategoriaDTO buscarPorId(String id) {
        return datos.buscarPorId(id);
    }

    public List<CategoriaDTO> buscarPorDescripcion(String texto) {
        return datos.buscarPorDescripcion(texto);
    }

    // id vacio = categoria nueva (el id lo genera la base); con id = modificar
    public int agregarCategoria(CategoriaDTO categoria) {
        if (categoria.getId() == null || categoria.getId().isBlank()) {
            datos.agregar(categoria);
            return 1; // Categoría agregada exitosamente
        }
        datos.modificar(categoria);
        return 2; // Categoría modificada exitosamente
    }

    public void eliminarCategoria(String id) {
        RecursoLogica recursoLogica = new RecursoLogica();
        if (!recursoLogica.obtenerRecursosPorCategoria(id).isEmpty()) {
            throw new CategoriaEnUsoException(
                    "No se puede eliminar la categoría: todavía tiene recursos asociados. Reasígnelos primero.");
        }
        datos.borrar(id);
    }
}
