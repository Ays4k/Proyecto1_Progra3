package SRR.Logica;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;
import SRR.Datos.CategoriaDatos;
import SRR.Datos.RecursoDatos;
import SRR.Datos.ReservaDatos;
import SRR.Excepciones.CategoriasNoDisponiblesException;

import javax.swing.text.html.ListView;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ReservaLogica {

    private final ReservaDatos datos = new ReservaDatos();
    private final RecursoDatos recursoDatos = new RecursoDatos();
    private final CategoriaDatos categoriaDatos = new CategoriaDatos();

     //Un recurso esta libre si ninguna reserva ACTIVA lo usa en esa fecha
     // dentro de un rango que se traslape con el pedido.

    public boolean estaDisponible(String idRecurso, String fecha,
                                  String horaInicio, String horaFin) {
        LocalTime inicioPedido = LocalTime.parse(horaInicio);
        LocalTime finPedido = LocalTime.parse(horaFin);

        for (ReservaDTO reserva : datos.listar()) {
            if (!"ACTIVA".equalsIgnoreCase(reserva.getEstado())) {
                continue;
            }
            if (!fecha.equals(reserva.getFecha())) {
                continue;
            }
            if (reserva.getIdsRecursos() == null
                    || !reserva.getIdsRecursos().contains(idRecurso)) {
                continue;
            }

            LocalTime inicioExistente = LocalTime.parse(reserva.getHoraInicio());
            LocalTime finExistente = LocalTime.parse(reserva.getHoraFin());

            // se traslapan si cada uno empieza antes de que el otro termine
            if (inicioPedido.isBefore(finExistente) && inicioExistente.isBefore(finPedido)) {
                return false;
            }
        }
        return true;
    }

    public List<RecursoDTO> recursosDisponibles(String idCategoria, String fecha,
                                                String horaInicio, String horaFin) {
        List<RecursoDTO> libres = new ArrayList<>();
        for (RecursoDTO recurso : recursoDatos.buscarPorCategoria(idCategoria)) {
            if (estaDisponible(recurso.getId(), fecha, horaInicio, horaFin)) {
                libres.add(recurso);
            }
        }
        return libres;
    }

    public ReservaDTO crearReserva(String idFuncionario, String actividad, String fecha,
                                   String horaInicio, String horaFin,
                                   List<String> idsCategorias) throws CategoriasNoDisponiblesException {

        validar(idFuncionario, actividad, fecha, horaInicio, horaFin, idsCategorias);

        List<String> recursosAsignados = new ArrayList<>();
        List<String> sinCupo = new ArrayList<>();

        // LinkedHashSet elimina categorias repetidas y conserva el orden
        for (String idCategoria : new LinkedHashSet<>(idsCategorias)) {
            List<RecursoDTO> libres = recursosDisponibles(idCategoria, fecha, horaInicio, horaFin);
            if (libres.isEmpty()) {
                sinCupo.add(descripcionCategoria(idCategoria));
            } else {
                recursosAsignados.add(libres.get(0).getId());   // el primero disponible
            }
        }

        if (!sinCupo.isEmpty()) {
            throw new CategoriasNoDisponiblesException(sinCupo);
        }

        ReservaDTO reserva = new ReservaDTO(generarId(), idFuncionario, recursosAsignados,
                actividad, fecha, horaInicio, horaFin, "ACTIVA");
        datos.agregar(reserva);
        return reserva;
    }

    public void cancelarReserva(String idReserva) {
        ReservaDTO reserva = datos.buscarPorId(idReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe la reserva " + idReserva);
        }
        reserva.setEstado("CANCELADA");
        datos.modificar(reserva);
    }

    public List<ReservaDTO> reservasDe(String idFuncionario) {
        return datos.buscarPorFuncionario(idFuncionario);
    }

    public List<ReservaDTO> reservasActivasDe(String idFuncionario){
        List<ReservaDTO> activas = new ArrayList<ReservaDTO>();
        for(ReservaDTO x : reservasDe(idFuncionario)){
            if(x.getEstado().equals("ACTIVA")){
                activas.add(x);
            }
        }
        return activas;
    }

    private void validar(String idFuncionario, String actividad, String fecha,
                         String horaInicio, String horaFin, List<String> idsCategorias) {
        if (idFuncionario == null || idFuncionario.isBlank()) {
            throw new IllegalArgumentException("No hay un funcionario activo");
        }
        if (actividad == null || actividad.isBlank()) {
            throw new IllegalArgumentException("Debe describir la actividad");
        }
        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("Debe indicar la fecha");
        }
        if (horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Debe indicar las horas");
        }
        if (!LocalTime.parse(horaInicio).isBefore(LocalTime.parse(horaFin))) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio");
        }
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoria");
        }
    }

    private String descripcionCategoria(String idCategoria) {
        CategoriaDTO categoria = categoriaDatos.buscarPorId(idCategoria);
        return categoria == null ? idCategoria : categoria.getDescripcion();
    }

    private String generarId() {
        int mayor = 0;
        for (ReservaDTO reserva : datos.listar()) {
            String id = reserva.getId();
            if (id != null && id.startsWith("RES-")) {
                try {
                    mayor = Math.max(mayor, Integer.parseInt(id.substring(4)));
                } catch (NumberFormatException e) {
                    // formato distinto, se ignora
                }
            }
        }
        return String.format("RES-%06d", mayor + 1);
    }

    public List<ReservaDTO> obtenerTodasLasReservas() {
        return datos.listar();
    }
}