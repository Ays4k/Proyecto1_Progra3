package SRR.Servidor.Enrutadores;

import SRR.DTO.Protocolo;
import SRR.DTO.RecursoDTO;
import SRR.Servicio.RecursoServicio;

public class RecursoEnrutador extends EnrutadorBase {

    private final RecursoServicio servicio = new RecursoServicio();

    public RecursoEnrutador() {
        registrar(Protocolo.OBTENER_RECURSOS,
                p -> servicio.obtenerRecursos());

        registrar(Protocolo.GUARDAR_RECURSO,
                p -> servicio.guardarRecurso((RecursoDTO) p[0]));

        registrar(Protocolo.ELIMINAR_RECURSO, p -> {
            servicio.eliminarRecurso((String) p[0]);
            return null;
        });

        registrar(Protocolo.BUSCAR_RECURSO_POR_ID,
                p -> servicio.buscarPorId((String) p[0]));

        registrar(Protocolo.BUSCAR_RECURSO_POR_DESCRIPCION,
                p -> servicio.buscarPorDescripcion((String) p[0]));

        registrar(Protocolo.OBTENER_RECURSOS_POR_CATEGORIA,
                p -> servicio.obtenerRecursosPorCategoria((String) p[0]));

        registrar(Protocolo.FILTRAR_RECURSOS,
                p -> servicio.filtrarRecursos((String) p[0], (String) p[1]));
    }
}
