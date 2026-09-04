package SRR.Logica;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Datos.UsuarioDatos;

public class LoginLogica {

    private final UsuarioDatos datos = new UsuarioDatos();

   //devuelve el usuario sin contrasena si las credenciales son validas, devuelve null si no.
    public UsuarioDTO iniciarSesion(LoginDTO login) {
        datos.deserializar();   // relee por si otra instancia escribio

        if (login == null || login.getId() == null || login.getId().isBlank()) {
            return null;
        }

        if (!datos.validarCredenciales(login.getId(), login.getContrasena())) {
            return null;
        }

        for (UsuarioDTO usuario : datos.listar()) {
            if (usuario.getId().equals(login.getId())) {
                return usuario;
            }
        }
        return null;
    }

    public void cambiarClave(String id, String actual, String nueva, String confirmacion) {
        datos.deserializar();

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe indicar su id");
        }
        if (!datos.validarCredenciales(id, actual)) {
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