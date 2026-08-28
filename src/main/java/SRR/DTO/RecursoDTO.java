package SRR.DTO;

public class RecursoDTO {
    private String id;
    private String nombre;
    private String idCategoria;
    private boolean disponible;

    public RecursoDTO() {
    }

    public RecursoDTO(String id, String nombre, String idCategoria, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.disponible = disponible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}