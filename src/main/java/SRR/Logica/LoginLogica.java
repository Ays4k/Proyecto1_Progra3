package SRR.Logica;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Datos.UsuarioDatos;

public class LoginLogica {

    private final UsuarioDatos datos;

    public LoginLogica(){
        this.datos = new UsuarioDatos();
    }

    public LoginLogica(String rutaTemporal){
        this.datos = new UsuarioDatos(rutaTemporal);
    }

   //devuelve el usuario sin contrasena si las credenciales son validas, devuelve null si no.
    public UsuarioDTO iniciarSesion(LoginDTO login) {
        datos.deserializar();   // relee por si otra instancia escribio

        if (login == null || login.getId() == null || login.getId().isBlank()) {
            return null;
        }

        String userid = datos.validarCredenciales(login.getId(), login.getContrasena());

        if(userid == null){
            return null;
        }

        for (UsuarioDTO usuario : datos.listar()) {
            if (usuario.getId().equals(userid)) {
                return usuario;
            }
        }
        return null;
    }

    public void cambiarClave(String user, String actual, String nueva, String confirmacion) {
        datos.deserializar();

        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("Debe indicar su id");
        }
        String id = datos.validarCredenciales(user, actual);
        if (id == null) {
            throw new IllegalArgumentException("Id o clave actual incorrectos");
        }
        if (nueva == null || nueva.isBlank()) {
            throw new IllegalArgumentException("La clave nueva no puede estar vacia");
        }
        if (!nueva.equals(confirmacion)) {
            throw new IllegalArgumentException("Las claves nuevas no coinciden");
        }
        if (nueva.equals(actual)) {
            throw new IllegalArgumentException("La clave nueva debe ser distinta de la actual");
        }

        datos.cambiarContrasena(id, nueva);
    }
}