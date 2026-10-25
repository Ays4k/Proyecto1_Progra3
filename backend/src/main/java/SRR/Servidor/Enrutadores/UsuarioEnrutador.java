package SRR.Servidor.Enrutadores;

import SRR.DTO.Protocolo;
import SRR.DTO.UsuarioDTO;
import SRR.Servicio.UsuarioServicio;

public class UsuarioEnrutador extends EnrutadorBase {

    private final UsuarioServicio servicio = new UsuarioServicio();

    public UsuarioEnrutador() {
        registrar(Protocolo.OBTENER_USUARIOS,
                p -> servicio.obtenerUsuarios());

        registrar(Protocolo.BUSCAR_USUARIO_POR_ID,
                p -> servicio.buscarPorId((String) p[0]));

        registrar(Protocolo.NOMBRE_DE_USUARIO,
                p -> servicio.nombreDe((String) p[0]));

        registrar(Protocolo.BUSCAR_USUARIO_POR_NOMBRE,
                p -> servicio.buscarPorNombre((String) p[0]));

        registrar(Protocolo.CAMBIOS_USUARIO,
                p -> servicio.cambiosUsuario((UsuarioDTO) p[0]));

        registrar(Protocolo.ELIMINAR_USUARIO,
                p -> servicio.eliminarUsuario((String) p[0]));
    }
}
