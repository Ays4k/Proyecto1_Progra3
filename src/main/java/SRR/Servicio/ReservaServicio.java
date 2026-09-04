package SRR.Servicio;

import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaAiDTO;
import SRR.DTO.ReservaDTO;
import SRR.Excepciones.CategoriasNoDisponiblesException;
import SRR.Logica.ReservaLogica;

import java.util.List;

public class ReservaServicio {

    private final ReservaLogica reservaLogica = new ReservaLogica();

    public ReservaDTO crearReserva(String idFuncionario, String actividad, String fecha,
                                   String horaInicio, String horaFin,
                                   List<String> idsCategorias) throws CategoriasNoDisponiblesException {
        return reservaLogica.crearReserva(idFuncionario, actividad, fecha,
                horaInicio, horaFin, idsCategorias);
    }

    public void cancelarReserva(String idReserva) {
        reservaLogica.cancelarReserva(idReserva);
    }

    public ReservaAiDTO generarReservaAi(String prompt){
        return reservaLogica.crearReservaAi(prompt);
    }

    public List<ReservaDTO> reservasActivasDe(String idFuncionario) {
        return reservaLogica.reservasActivasDe(idFuncionario);
    }

    public boolean estaDisponible(String idRecurso, String fecha,
                                  String horaInicio, String horaFin) {
        return reservaLogica.estaDisponible(idRecurso, fecha, horaInicio, horaFin);
    }

    public List<RecursoDTO> recursosDisponibles(String idCategoria, String fecha,
                                                String horaInicio, String horaFin) {
        return reservaLogica.recursosDisponibles(idCategoria, fecha, horaInicio, horaFin);
    }

    public List<ReservaDTO> obtenerTodasLasReservas() {
        return reservaLogica.obtenerTodasLasReservas();
    }
}