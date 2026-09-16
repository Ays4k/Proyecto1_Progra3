package SRR.Logica;

import SRR.Datos.RecursoDatos;
import SRR.DTO.RecursoDTO;
import SRR.Datos.ReservaDatos;
import SRR.DTO.CategoriaDTO;
import SRR.DTO.ReservaDTO;
import SRR.Excepciones.RecursoException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecursoLogica {

    private final RecursoDatos datos = new RecursoDatos();
    private final ReservaDatos reservaDatos = new ReservaDatos();
    private final CategoriaLogica categoriaLogica = new CategoriaLogica();


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
        validarRecurso(recurso);

        RecursoDTO recursoExistente = buscarPorId(recurso.getId());

        if (recursoExistente == null) {
            datos.agregar(recurso);
            return 1; //recurso agregado
        }

        datos.modificar(recurso);
        return 2; //recurso modificado
    }

    private void validarRecurso(RecursoDTO recurso) {
        if (recurso == null) {
            throw new RecursoException(
                    "Debe proporcionar los datos del recurso."
            );
        }

        if (recurso.getId() == null || recurso.getId().isBlank()) {
            throw new RecursoException(
                    "Debe indicar el ID o número de activo."
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
        reservaDatos.deserializar();

        for (ReservaDTO reserva : reservaDatos.listar()) {
            if (!"ACTIVA".equalsIgnoreCase(reserva.getEstado())) {
                continue;
            }

            if (reserva.getIdsRecursos() != null
                    && reserva.getIdsRecursos().contains(idRecurso)) {

                return true;
            }
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