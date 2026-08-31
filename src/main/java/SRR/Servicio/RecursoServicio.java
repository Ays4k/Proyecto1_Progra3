package SRR.Servicio;

import SRR.DTO.RecursoDTO;
import SRR.Logica.RecursoLogica;

import java.util.List;

public class RecursoServicio {

    private final RecursoLogica logica = new RecursoLogica();

    public List<RecursoDTO> obtenerRecursos() {
        return logica.obtenerRecursos();
    }

    public int servicioRecursos(RecursoDTO recurso) {
        return logica.guardarRecurso(recurso);
    }

    public boolean eliminarRecurso(String id) {
        return logica.eliminarRecurso(id);
    }

    public List<RecursoDTO> buscarPorDescripcion(String descripcion) {
        return logica.buscarPorDescripcion(descripcion);
    }

    public List<RecursoDTO> obtenerRecursosPorCategoria(String idCategoria) {
        return logica.obtenerRecursosPorCategoria(idCategoria);
    }

    public List<RecursoDTO> filtrarRecursos(String idCategoria, String descripcion) {
        return logica.filtrarRecursos(idCategoria, descripcion);
    }
}