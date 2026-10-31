package SRR.Logica;

import SRR.Datos.RecursoDatos;
import SRR.DTO.RecursoDTO;
import SRR.Datos.ReservaDatos;
import SRR.DTO.CategoriaDTO;
import SRR.Excepciones.RecursoException;

import java.util.ArrayList;
import java.util.List;

public class RecursoLogica {

    private final RecursoDatos datos = new RecursoDatos();
    private final ReservaDatos reservaDatos = new ReservaDatos();
    private final CategoriaLogica categoriaLogica = new CategoriaLogica();


    public List<RecursoDTO> obtenerRecursos() {
        return datos.listar();
    }

    public RecursoDTO buscarPorId(String id) {
        return datos.buscarPorId(id);
    }

    public int guardarRecurso(RecursoDTO recurso) {
        validarRecurso(recurso);

        String id = recurso.getId();

        if (id == null || id.isBlank()) {
            datos.agregar(recurso);
            return 1;
        }

        datos.modificar(recurso);
        return 2;
    }

    private void validarRecurso(RecursoDTO recurso) {
        if (recurso == null) {
            throw new RecursoException(
                    "Debe proporcionar los datos del recurso."
            );
        }


        if (recurso.getDescripcion() == null || recurso.getDescripcion().isBlank()) {
            throw new RecursoException(
                    "Debe indicar la descripción del recurso."
            );
        }

        if (recurso.getIdCategoria() == null || recurso.getIdCategoria().isBlank()) {
            throw new RecursoException(
                    "Debe seleccionar una categoría."
            );
        }

        boolean categoriaExiste = false;

        for (CategoriaDTO categoria : categoriaLogica.obtenerCategorias()) {
            if (recurso.getIdCategoria().equals(categoria.getId())) {
                categoriaExiste = true;
                break;
            }
        }

        if (!categoriaExiste) {
            throw new RecursoException(
                    "La categoría seleccionada no existe."
            );
        }
    }

    public void eliminarRecurso(String id) {
        if (id == null || id.isBlank()) {
            throw new RecursoException("Debe indicar el recurso que desea eliminar.");
        }

        RecursoDTO recurso = buscarPorId(id);

        if (recurso == null) {
            throw new RecursoException("No existe el recurso " + id + ".");
        }

        if (estaAsignadoAReservaActiva(id)) {
            throw new RecursoException(
                    "No se puede eliminar el recurso porque está asignado a una reserva activa."
            );
        }

        datos.borrar(id);
    }

    private boolean estaAsignadoAReservaActiva(String idRecurso) {
        return reservaDatos.recursoEnReservaActiva(idRecurso);
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
        return datos.listarPorCategoria(idCategoria);
    }

    public List<RecursoDTO> filtrarRecursos(String idCategoria, String descripcion) {
        return datos.filtrar(idCategoria, descripcion);
    }
}
