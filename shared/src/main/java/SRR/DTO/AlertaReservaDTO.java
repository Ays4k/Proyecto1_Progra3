package SRR.DTO;

import java.io.Serializable;
import java.util.List;

/**
 * Notificación que el backend envía a todos los clientes conectados
 * cuando se crea una reserva nueva.
 */
public class AlertaReservaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idReserva;
    private String idFuncionario;
    private String encargado;
    private String actividad;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private List<String> nombresRecursos;

    public AlertaReservaDTO() {
    }

    public AlertaReservaDTO(String idReserva, String idFuncionario, String encargado, String actividad, String fecha,
                            String horaInicio, String horaFin, List<String> nombresRecursos) {
        this(idReserva, encargado, actividad, fecha, horaInicio, horaFin, nombresRecursos);
        this.idFuncionario = idFuncionario;
    }

    public AlertaReservaDTO(String idReserva, String encargado, String actividad, String fecha,
                            String horaInicio, String horaFin, List<String> nombresRecursos) {
        this.idReserva = idReserva;
        this.encargado = encargado;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.nombresRecursos = nombresRecursos;
    }

    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }

    public String getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(String idFuncionario) { this.idFuncionario = idFuncionario; }

    public String getEncargado() { return encargado; }
    public void setEncargado(String encargado) { this.encargado = encargado; }

    public String getActividad() { return actividad; }
    public void setActividad(String actividad) { this.actividad = actividad; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public List<String> getNombresRecursos() { return nombresRecursos; }
    public void setNombresRecursos(List<String> nombresRecursos) { this.nombresRecursos = nombresRecursos; }
}
