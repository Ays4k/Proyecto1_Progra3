package SRR.Proxy;

import SRR.DTO.Protocolo;
import SRR.DTO.UsuarioDTO;

import java.util.List;

// Representa en el frontend al UsuarioServicio del backend
public class UsuarioProxy extends ProxyBase {

    public List<UsuarioDTO> obtenerUsuarios() {
        return enviarLista(Protocolo.OBTENER_USUARIOS);
    }

    public UsuarioDTO buscarPorId(String id) {
        return enviar(Protocolo.BUSCAR_USUARIO_POR_ID, id);
    }

    public String nombreDe(String id) {
        return enviar(Protocolo.NOMBRE_DE_USUARIO, id);
    }

    public List<UsuarioDTO> buscarPorNombre(String texto) {
        return enviarLista(Protocolo.BUSCAR_USUARIO_POR_NOMBRE, texto);
    }

    public int cambiosUsuario(UsuarioDTO usuario) {
        Integer resultado = enviar(Protocolo.CAMBIOS_USUARIO, usuario);
        return resultado;
    }

    public boolean eliminarUsuario(String id) {
        Boolean resultado = enviar(Protocolo.ELIMINAR_USUARIO, id);
        return resultado;
    }
}