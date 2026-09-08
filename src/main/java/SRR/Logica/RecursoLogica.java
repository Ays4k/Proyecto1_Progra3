package SRR.Logica;

import SRR.Datos.RecursoDatos;
import SRR.DTO.RecursoDTO;
import SRR.Servicio.ReservaServicio;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecursoLogica {

    private final RecursoDatos datos = new RecursoDatos();


    public List<RecursoDTO> obtenerRecursos() {
        datos.deserializar();   // relee por si otra pantalla agrego o modifico un recurso
        return datos.listar();
    }

    public RecursoDTO buscarPorId(String id) {
        for(RecursoDTO recurso : datos.listar()) {
            if(recurso.getId().equals(id)) {
                return recurso;
            }
        }
        return null;
    }

    public int guardarRecurso(RecursoDTO recurso) {
        RecursoDTO busqueda = buscarPorId(recurso.getId());
        if (busqueda == null) {
            datos.agregar(recurso);
            return 1; // Recurso Agregado
        } else {
            datos.modificar(recurso);
            return 2; // Recurso Modificado
        }
    }

    public boolean eliminarRecurso(String id) {
        if (buscarPorId(id) != null) {
            datos.borrar(id);
            return true;
        }
        return false;
    }

    public List<RecursoDTO> buscarPorDescripcion(String texto) {
        List<RecursoDTO> resultado = new ArrayList<>();
        if (texto == null) {
            return resultado;
        }
        String busqueda = texto.toLowerCase();
        for (RecursoDTO recurso : datos.listar()) {
            String descripcion = recurso.getDescripcion();
            if (descripcion != null && descripcion.toLowerCase().contains(busqueda)) {
                resultado.add(recurso);
            }
        }
        return resultado;
    }

    public List<RecursoDTO> obtenerRecursosPorCategoria(String idCategoria) {
        List<RecursoDTO> resultado = new ArrayList<>();
        for (RecursoDTO recurso : datos.listar()) {
            if (idCategoria.equals(recurso.getIdCategoria())) {
                resultado.add(recurso);
            }
        }
        return resultado;
    }

    public List<RecursoDTO> filtrarRecursos(String idCategoria, String descripcion) {
        List<RecursoDTO> lista = datos.listar();
        return lista.stream()
                .filter(r -> idCategoria == null || idCategoria.isEmpty() || r.getIdCategoria().equals(idCategoria))
                .filter(r -> descripcion == null || descripcion.isEmpty() || r.getDescripcion().toLowerCase().contains(descripcion.toLowerCase()))
                .collect(Collectors.toList());
    }
}