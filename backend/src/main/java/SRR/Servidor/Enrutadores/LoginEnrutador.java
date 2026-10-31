package SRR.Servidor.Enrutadores;

import SRR.DTO.LoginDTO;
import SRR.DTO.Protocolo;
import SRR.Servicio.LoginServicio;

public class LoginEnrutador extends EnrutadorBase {

    private final LoginServicio servicio = new LoginServicio();

    public LoginEnrutador() {
        registrar(Protocolo.INICIAR_SESION,
                p -> servicio.iniciarSesion((LoginDTO) p[0]));

        registrar(Protocolo.CAMBIAR_CLAVE, p -> {
            servicio.cambiarClave((String) p[0], (String) p[1], (String) p[2], (String) p[3]);
            return null;
        });
    }
}
