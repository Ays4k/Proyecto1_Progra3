package SRR.Servicio;
import SRR.Logica.CategoriaLogica;
import SRR.DTO.CategoriaDTO;

import java.util.List;

public class CategoriaServicio {



    private final CategoriaLogica logica = new CategoriaLogica();
    public List<CategoriaDTO> obtenerCategorias() {
        return logica.obtenerCategorias();
    }

    public int servicioCategorias(CategoriaDTO categoria) {
        return logica.agregarCategoria(categoria);
    }

    public void eliminarCategoria(String id) {
        logica.eliminarCategoria(id);
    }
}
