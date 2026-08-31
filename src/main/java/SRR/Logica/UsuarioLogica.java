package SRR.Logica;

import SRR.DTO.UsuarioDTO;
import SRR.Datos.UsuarioDatos;

import java.util.List;

public class UsuarioLogica {

    private final UsuarioDatos datos = new UsuarioDatos();

    public List<UsuarioDTO> obtenerUsuarios() throws IllegalAccessException{
        for(UsuarioDTO usuario : datos.listar()) {
            if(usuario.getContrasena() != null) {
                throw new IllegalAccessException("Error de seguridad");
            }
        }

        return datos.listar();
    }

    public int cambiosUsuario(UsuarioDTO usuario) {
        if(datos.buscarPorId(usuario.getId()) != null) {
            datos.modificar(usuario);
            return 2;
        } else {
            datos.agregar(usuario);
            return 1;
        }
    }

    public boolean eliminarUsuario(String id) {
        if(datos.buscarPorId(id) != null) {
            datos.borrar(id);
            return true; // Usuario eliminado exitosamente
        } else {
            return false; // Usuario no encontrado
        }
    }


}
