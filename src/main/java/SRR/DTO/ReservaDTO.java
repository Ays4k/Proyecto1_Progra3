package SRR.DTO;

public class ReservaDTO {
    private String id;
    private String idFuncionario;
    private String idRecurso;
    private String idActividad;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String estado; // "PENDIENTE", "APROBADA", "CANCELADA"

    public ReservaDTO() {
    }

    public ReservaDTO(String id, String idFuncionario, String idRecurso, String idActividad, String fecha, String horaInicio, String horaFin, String estado) {
        this.id = id;
        this.idFuncionario = idFuncionario;
        this.idRecurso = idRecurso;
        this.idActividad = idActividad;
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

    public String getIdRecurso() {
        return idRecurso;
    }

    public void setIdRecurso(String idRecurso) {
        this.idRecurso = idRecurso;
    }

    public String getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(String idActividad) {
        this.idActividad = idActividad;
    }

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