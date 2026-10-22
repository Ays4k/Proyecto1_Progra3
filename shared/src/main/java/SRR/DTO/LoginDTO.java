package SRR.DTO;

public class LoginDTO {
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