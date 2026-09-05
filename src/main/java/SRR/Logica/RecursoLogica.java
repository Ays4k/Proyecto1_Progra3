package SRR.Logica;

import SRR.Datos.RecursoDatos;
import SRR.DTO.RecursoDTO;

import java.util.List;
import java.util.stream.Collectors;

public class RecursoLogica {

    private final RecursoDatos datos = new RecursoDatos();

    public List<RecursoDTO> obtenerRecursos() {
        datos.deserializar();   // relee por si otra pantalla agrego o modifico un recurso
        return datos.listar();
    }

    public int guardarRecurso(RecursoDTO recurso) {
        RecursoDTO busqueda = datos.buscarPorId(recurso.getId());
        if (busqueda == null) {
            datos.agregar(recurso);
            return 1; // Recurso Agregado
        } else {
            datos.modificar(recurso);
            return 2; // Recurso Modificado
        }
    }

    public boolean eliminarRecurso(String id) {
        if (datos.buscarPorId(id) != null) {
            datos.borrar(id);
            return true;
        }
        return false;
    }

    public List<RecursoDTO> buscarPorDescripcion(String descripcion) {
        return datos.buscarPorDescripcion(descripcion);
    }

    public List<RecursoDTO> obtenerRecursosPorCategoria(String idCategoria) {
        return datos.buscarPorCategoria(idCategoria);
    }

    public List<RecursoDTO> filtrarRecursos(String idCategoria, String descripcion) {
        List<RecursoDTO> lista = datos.listar();
        return lista.stream()
                .filter(r -> idCategoria == null || idCategoria.isEmpty() || r.getIdCategoria().equals(idCategoria))
                .filter(r -> descripcion == null || descripcion.isEmpty() || r.getDescripcion().toLowerCase().contains(descripcion.toLowerCase()))
                .collect(Collectors.toList());
    }
}