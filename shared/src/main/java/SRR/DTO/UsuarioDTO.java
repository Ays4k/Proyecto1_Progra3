package SRR.DTO;

public class UsuarioDTO {
    private String id;
    private String nombre;
    private String telefono;
    private String contrasena;
    private String rol; // "ADMINISTRADOR" o "FUNCIONARIO"

    public UsuarioDTO() {
    }

    public UsuarioDTO(String id, String nombre, String telefono, String contrasena, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public UsuarioDTO(UsuarioDTO otro) {
        this.id = otro.id;
        this.nombre = otro.nombre;
        this.telefono = otro.telefono;
        this.rol = otro.rol;
        this.contrasena = null;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}