package SRR.Servidor.Enrutadores;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.Protocolo;
import SRR.Servicio.CategoriaServicio;

public class CategoriaEnrutador extends EnrutadorBase {

    private final CategoriaServicio servicio = new CategoriaServicio();

    public CategoriaEnrutador() {
        registrar(Protocolo.OBTENER_CATEGORIAS,
                p -> servicio.obtenerCategorias());

        registrar(Protocolo.GUARDAR_CATEGORIA,
                p -> servicio.servicioCategorias((CategoriaDTO) p[0]));

        registrar(Protocolo.ELIMINAR_CATEGORIA, p -> {
            servicio.eliminarCategoria((String) p[0]);
            return null;
        });

        registrar(Protocolo.BUSCAR_CATEGORIA_POR_ID,
                p -> servicio.buscarPorId((String) p[0]));
    }
}
