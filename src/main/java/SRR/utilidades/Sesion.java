package SRR.utilidades;

import SRR.DTO.UsuarioDTO;

// Guarda el usuario que hizo el inicio de sesion para que cualquier pantalla pueda
// consultarlo. No valida nada, ya que la validacion ocurre en UsuarioLogica y aqui
// solo se conserva el resultado, porque al cambiar de escena se pierde el
// contexto del LoginController.

public class Sesion {

    private static UsuarioDTO usuarioActual;

    private Sesion() {
    }

    public static void iniciar(UsuarioDTO usuario) {
        usuarioActual = usuario;
    }

    public static void cerrar() {
        usuarioActual = null;
    }

    public static UsuarioDTO getUsuario() {
        return usuarioActual;
    }

    public static String getId() {
        return usuarioActual == null ? null : usuarioActual.getId();
    }

    public static String getRol() {
        return usuarioActual == null ? null : usuarioActual.getRol();
    }

    public static boolean esAdministrador() {
        return "ADMINISTRADOR".equals(getRol());
    }
}
