package SRR.DTO;

import java.io.Serializable;

public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String contrasena;

    public LoginDTO() {
    }

    public LoginDTO(String id, String contrasena) {
        this.id = id;
        this.contrasena = contrasena;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}