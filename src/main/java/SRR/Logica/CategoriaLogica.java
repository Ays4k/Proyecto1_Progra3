package SRR.Logica;
import SRR.Datos.CategoriaDatos;
import SRR.DTO.CategoriaDTO;
import java.util.List;

public class CategoriaLogica {

    private CategoriaDatos datos = new CategoriaDatos();
    public List<CategoriaDTO> obtenerCategorias() {
        return datos.listar();
    }

    public int agregarCategoria(CategoriaDTO categoria) {
        CategoriaDTO busqueda = datos.buscarPorId(categoria.getId());

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
