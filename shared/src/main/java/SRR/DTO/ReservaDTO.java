package SRR.DTO;

import java.util.List;

public class ReservaDTO {
    private String id;
    private String idFuncionario;
    private List<String> idsRecursos;
    private String actividad;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String estado; // "ACTIVA", "CANCELADA"

    public ReservaDTO() {
    }

    public ReservaDTO(String id, String idFuncionario, List<String> idsRecursos, String actividad, String fecha, String horaInicio, String horaFin, String estado) {
        this.id = id;
        this.idFuncionario = idFuncionario;
        this.idsRecursos = idsRecursos;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(String idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public List<String> getIdsRecursos() {return idsRecursos;}

    public void setIdsRecursos(List<String> idsRecursos) {this.idsRecursos = idsRecursos;}

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {this.actividad = actividad;}

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}