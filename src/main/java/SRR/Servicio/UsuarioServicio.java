package SRR.Servicio;

import SRR.Logica.LogicaUsuarios;
import SRR.DTO.UsuarioDTO;

import java.util.List;

public class UsuarioServicio {

    private final LogicaUsuarios logicaUsuarios = new LogicaUsuarios();

    public List<UsuarioDTO> obtenerUsuarios() throws IllegalAccessException {
        return logicaUsuarios.obtenerUsuarios();
    }

    public int cambiosUsuario(UsuarioDTO usuario) {
        return logicaUsuarios.cambiosUsuario(usuario);
    }

    public boolean eliminarUsuario(String id) {
        return logicaUsuarios.eliminarUsuario(id);
    }
}
