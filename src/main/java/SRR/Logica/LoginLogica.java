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
                return new UsuarioDTO(usuario);
            }
        }

        return null;
    }

    public void cambiarClave(String id, String actual, String nueva, String confirmacion) {
        datos.deserializar();

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe indicar su ID");
        }

        String idValidado = datos.validarCredenciales(id, actual);

        if (idValidado == null) {
            throw new IllegalArgumentException("ID o clave actual incorrectos");
        }

        if (nueva == null || nueva.isBlank()) {
            throw new IllegalArgumentException("La clave nueva no puede estar vacía");
        }

        if (!nueva.equals(confirmacion)) {
            throw new IllegalArgumentException("Las claves nuevas no coinciden");
        }

        if (nueva.equals(actual)) {
            throw new IllegalArgumentException("La clave nueva debe ser distinta de la actual");
        }

        datos.cambiarContrasena(idValidado, nueva);
    }
}