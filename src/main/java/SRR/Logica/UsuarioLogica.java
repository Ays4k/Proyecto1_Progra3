package SRR.Logica;

import SRR.DTO.LoginDTO;
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

    public UsuarioDTO iniciarSesion(LoginDTO login) {
        if (login == null || login.getId() == null || login.getId().isBlank()) {
            return null;
        }
        datos.deserializar();
        UsuarioDTO usuario = datos.buscarPorId(login.getId());
        if (usuario == null) {
            return null;
        }

        if (usuario.getContrasena() == null
                || !usuario.getContrasena().equals(login.getContrasena())) {
            return null;
        }

        // se devuelve una copia sin la contrasena
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(),
                usuario.getTelefono(), null, usuario.getRol());
    }

    public void cambiarClave(String id, String actual, String nueva, String confirmacion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe indicar su id");
        }

        UsuarioDTO usuario = datos.buscarPorId(id);
        if (usuario == null || usuario.getContrasena() == null
                || !usuario.getContrasena().equals(actual)) {
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
        datos.deserializar();
    }

}
