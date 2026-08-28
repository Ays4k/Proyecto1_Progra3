package SRR.DTO;

public class RecursoDTO {
    private String id;
    private String descripcion;
    private String idCategoria;

    public RecursoDTO() {
    }

    public RecursoDTO(String id, String descripcion, String idCategoria) {
        this.id = id;
        this.descripcion = descripcion;
        this.idCategoria = idCategoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }
}