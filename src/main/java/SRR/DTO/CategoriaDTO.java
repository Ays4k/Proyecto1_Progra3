package SRR.DTO;

import java.io.Serializable;

public class CategoriaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String descripcion;

    public CategoriaDTO(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public CategoriaDTO() {

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

}
