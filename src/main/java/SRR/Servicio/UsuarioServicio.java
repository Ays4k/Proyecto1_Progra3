package SRR.Servicio;

import SRR.DTO.LoginDTO;
import SRR.Logica.UsuarioLogica;
import SRR.DTO.UsuarioDTO;

import java.util.List;

public class UsuarioServicio {

    private final UsuarioLogica usuarioLogica;

    public UsuarioServicio(){
        this.usuarioLogica = new UsuarioLogica();
    }

    public UsuarioServicio(String rutaTemporal){
        this.usuarioLogica = new UsuarioLogica(rutaTemporal);
    }

    public List<UsuarioDTO> obtenerUsuarios() {
        return usuarioLogica.obtenerUsuarios();
    }

    public UsuarioDTO buscarPorId(String id) {
        return usuarioLogica.buscarPorId(id);
    }

    public List<UsuarioDTO> buscarPorNombre(String texto) {
        return usuarioLogica.buscarPorNombre(texto);
    }

    public int cambiosUsuario(UsuarioDTO usuario) {
        return usuarioLogica.cambiosUsuario(usuario);
    }

    public boolean eliminarUsuario(String id) {
        return usuarioLogica.eliminarUsuario(id);
    }
}