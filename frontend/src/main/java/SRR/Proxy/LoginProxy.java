package SRR.Proxy;

import SRR.DTO.LoginDTO;
import SRR.DTO.Protocolo;
import SRR.DTO.UsuarioDTO;

// Representa en el frontend al LoginServicio del backend
public class LoginProxy extends ProxyBase {

    public UsuarioDTO iniciarSesion(LoginDTO login) {
        return enviar(Protocolo.INICIAR_SESION, login);
    }

    public void cambiarClave(String id, String actual, String nueva, String confirmacion) {
        enviar(Protocolo.CAMBIAR_CLAVE, id, actual, nueva, confirmacion);
    }
}