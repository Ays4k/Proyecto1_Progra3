package SRR.Servicio;

import SRR.DTO.LoginDTO;
import SRR.Logica.UsuarioLogica;
import SRR.DTO.UsuarioDTO;

import java.util.List;

public class UsuarioServicio {

    private final UsuarioLogica usuarioLogica = new UsuarioLogica();

    public List<UsuarioDTO> obtenerUsuarios() throws IllegalAccessException {
        return usuarioLogica.obtenerUsuarios();
    }

    public int cambiosUsuario(UsuarioDTO usuario) {
        return usuarioLogica.cambiosUsuario(usuario);
    }

    public boolean eliminarUsuario(String id) {
        return usuarioLogica.eliminarUsuario(id);
    }

    public UsuarioDTO iniciarSesion(LoginDTO login) { return usuarioLogica.iniciarSesion(login);}

    public void cambiarClave(String id, String actual, String nueva, String confirmacion) { usuarioLogica.cambiarClave(id, actual, nueva, confirmacion);}
}
