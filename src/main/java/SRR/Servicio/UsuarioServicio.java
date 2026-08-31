package SRR.Servicio;

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
}
