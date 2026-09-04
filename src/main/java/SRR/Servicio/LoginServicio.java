package SRR.Servicio;

import SRR.DTO.LoginDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Logica.LoginLogica;

public class LoginServicio {

    private final LoginLogica loginLogica = new LoginLogica();

    public UsuarioDTO iniciarSesion(LoginDTO login) {
        return loginLogica.iniciarSesion(login);
    }

    public void cambiarClave(String id, String actual, String nueva, String confirmacion) {
        loginLogica.cambiarClave(id, actual, nueva, confirmacion);
    }
}