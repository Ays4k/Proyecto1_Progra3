package SRR.Logica;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Datos.UsuarioDatos;

import java.util.ArrayList;
import java.util.List;

public class UsuarioLogica {

    private final UsuarioDatos datos = new UsuarioDatos();

    public List<UsuarioDTO> obtenerUsuarios() {
        return datos.listar(); //los usuarios vienen sin contrasenas
    }

    public UsuarioDTO buscarPorId(String id) {
        for (UsuarioDTO usuario : datos.listar()) {
            if (usuario.getId().equals(id)) {
                return usuario;
            }
        }
        return null;
    }

    public List<UsuarioDTO> buscarPorNombre(String texto) {
        List<UsuarioDTO> resultado = new ArrayList<>();
        if (texto == null) {
            return resultado;
        }
        String busqueda = texto.toLowerCase();
        for (UsuarioDTO usuario : datos.listar()) {
            String nombre = usuario.getNombre();
            if (nombre != null && nombre.toLowerCase().contains(busqueda)) {
                resultado.add(usuario);
            }
        }
        return resultado;
    }

    //devuelve 2 si se modifica un usuario existente, 1 si agrega uno nuevo
    public int cambiosUsuario(UsuarioDTO usuario) {
        if (buscarPorId(usuario.getId()) != null) {
            datos.modificar(usuario);
            return 2;
        } else {
            datos.agregar(usuario);
            return 1;
        }
    }

    public boolean eliminarUsuario(String id) {
        if (buscarPorId(id) != null) {
            datos.borrar(id);
            return true; // Usuario eliminado exitosamente
        } else {
            return false; // Usuario no encontrado
        }
    }
}
