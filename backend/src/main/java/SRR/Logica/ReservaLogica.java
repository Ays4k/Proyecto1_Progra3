package SRR.Logica;

import SRR.DTO.*;
import SRR.Datos.ReservaDatos;
import SRR.Excepciones.CategoriasNoDisponiblesException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

public class ReservaLogica {

    private final ReservaDatos datos;
    private final RecursoLogica recursoLogica;
    private final CategoriaLogica categoriaLogica = new CategoriaLogica();
    private  GeminiService ai;
    private boolean funcionalidadai;
    private final NotificacionLogica notificaciones;
    public ReservaLogica(Consumer<AlertaReservaDTO> obs){
        try {
            this.ai = new GeminiService();
            funcionalidadai = true;
        }catch (Exception e){
            this.ai = null;
            funcionalidadai = false;
        }
        notificaciones = new NotificacionLogica(obs);
        datos = new ReservaDatos();
        recursoLogica = new RecursoLogica();
    }
    public ReservaDTO buscarPorId(String id) {
        return datos.buscarPorId(id);
    }

    public List<ReservaDTO> buscarPorFuncionario(String idFuncionario) {
        return datos.listarPorFuncionario(idFuncionario);
    }


    public boolean estaDisponible(String idRecurso, String fecha,
                                  String horaInicio, String horaFin) {
        return !datos.recursoOcupado(idRecurso, fecha, horaInicio, horaFin);
    }

    public boolean estaDisponible(String idRecurso) {
        return !datos.recursoEnReservaActiva(idRecurso);
    }

    public List<RecursoDTO> recursosDisponibles(String idCategoria, String fecha,
                                                String horaInicio, String horaFin) {
        List<RecursoDTO> libres = new ArrayList<>();
        for (RecursoDTO recurso : recursoLogica.obtenerRecursosPorCategoria(idCategoria)) {
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

        for (String idCategoria : new LinkedHashSet<>(idsCategorias)) {
            List<RecursoDTO> libres = recursosDisponibles(idCategoria, fecha, horaInicio, horaFin);
            if (libres.isEmpty()) {
                sinCupo.add(descripcionCategoria(idCategoria));
            } else {
                recursosAsignados.add(libres.get(0).getId());
            }
        }

        if (!sinCupo.isEmpty()) {
            throw new CategoriasNoDisponiblesException(sinCupo);
        }

        ReservaDTO reserva = new ReservaDTO(null, idFuncionario, recursosAsignados,
                actividad, fecha, horaInicio, horaFin, "ACTIVA");
        datos.agregar(reserva);
        notificaciones.notificar(reserva);

        return reserva;
    }

    public void cancelarReserva(String idReserva) {
        ReservaDTO reserva = buscarPorId(idReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe la reserva " + idReserva);
        }
        if (java.time.LocalDate.parse(reserva.getFecha()).isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("No se puede cancelar una reserva que ya pasó.");
        }
        reserva.setEstado("CANCELADA");
        datos.modificar(reserva);
    }

    public ReservaAiDTO crearReservaAi(String prompt){
        if(!funcionalidadai){
            throw new RuntimeException("Actualmente no se encuentra en funcionamiento el servicio de IA, por favor, revise su configuración e intentelo más tarde");
        }
        try{
            String[] datos = ai.enviarMensaje(prompt).split(",");
            if(!datos[0].contains("COMPLETO")){
                throw new IOException(String.join("",datos));
            }
            ReservaAiDTO res = new ReservaAiDTO(datos[1], LocalDate.parse(datos[2])
                    ,LocalTime.parse(datos[3]),LocalTime.parse(datos[4]),
                    new ArrayList<String>(Arrays.asList(datos[5].split("#"))));

            return res;

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }


    public List<ReservaDTO> reservasActivasDe(String idFuncionario){
        List<ReservaDTO> activas = new ArrayList<ReservaDTO>();
        for(ReservaDTO x : buscarPorFuncionario(idFuncionario)){
            if(x.getEstado().equals("ACTIVA")){
                activas.add(x);
            }
        }
        return activas;
    }

    private void validar(String idFuncionario,
                         String actividad,
                         String fecha,
                         String horaInicio,
                         String horaFin,
                         List<String> idsCategorias) {

        if (idFuncionario == null || idFuncionario.isBlank()) {
            throw new IllegalArgumentException(
                    "No hay un funcionario activo"
            );
        }

        if (actividad == null || actividad.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe describir la actividad"
            );
        }

        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha"
            );
        }

        if (horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException(
                    "Debe indicar las horas"
            );
        }

        LocalDate fechaReserva;
        LocalTime inicio;
        LocalTime fin;

        try {
            fechaReserva = LocalDate.parse(fecha);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "La fecha no tiene un formato válido"
            );
        }

        try {
            inicio = LocalTime.parse(horaInicio);
            fin = LocalTime.parse(horaFin);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Las horas no tienen un formato válido"
            );
        }

        if (fechaReserva.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede reservar en una fecha pasada"
            );
        }

        if (fechaReserva.equals(LocalDate.now())
                && !inicio.isAfter(LocalTime.now())) {

            throw new IllegalArgumentException(
                    "La hora de inicio ya pasó"
            );
        }

        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la de inicio"
            );
        }

        if (inicio.isBefore(LocalTime.of(8, 0))
                || fin.isAfter(LocalTime.of(17, 0))) {

            throw new IllegalArgumentException(
                    "Las reservas deben realizarse entre las 08:00 y las 17:00"
            );
        }

        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos una categoría"
            );
        }
    }

    private String descripcionCategoria(String idCategoria) {
        CategoriaDTO categoria = categoriaLogica.buscarPorId(idCategoria);
        return categoria == null ? idCategoria : categoria.getDescripcion();
    }

    public List<ReservaDTO> obtenerTodasLasReservas() {
        return datos.listar();
    }
}
