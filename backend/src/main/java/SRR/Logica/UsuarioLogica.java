package SRR.Logica;

import SRR.DTO.UsuarioDTO;
import SRR.Datos.UsuarioDatos;
import SRR.Excepciones.UsuarioEnUsoException;

import java.util.ArrayList;
import java.util.List;

public class UsuarioLogica {

    private UsuarioDatos datos;

    public UsuarioLogica(){
        this.datos = new UsuarioDatos();
    }


    // la identificacion es la cedula: solo numeros (se ajusta aqui si cambia el formato)
    private static final String FORMATO_IDENTIFICACION = "\\d{9,12}";

    public List<UsuarioDTO> obtenerUsuarios() {
        return datos.listar(); //los usuarios vienen sin contrasenas
    }

    public UsuarioDTO buscarPorId(String id) {
        return datos.buscarPorId(id);
    }

    // devuelve el nombre del usuario, o un texto de respaldo si el usuario ya no existe
    // lo usan las pantallas que muestran reservas, donde solo se guarda el id
    public String nombreDe(String id) {
        UsuarioDTO usuario = buscarPorId(id);
        return usuario == null || usuario.getNombre() == null
                ? "Usuario desconocido" : usuario.getNombre();
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
        }
        validarIdentificacion(usuario.getId());
        datos.agregar(usuario);
        return 1;
    }

    private void validarIdentificacion(String id) {
        if (id == null || !id.matches(FORMATO_IDENTIFICACION)) {
            throw new IllegalArgumentException(
                    "La cédula debe tener solo números (entre 9 y 12 dígitos).");
        }
    }

    public boolean eliminarUsuario(String id) {
        if (buscarPorId(id) == null) {
            return false;
        }

        // solo no deja borrar cuando el usuario tiene reservas activas
        ReservaLogica reservaLogica = new ReservaLogica();
        if (!reservaLogica.reservasActivasDe(id).isEmpty()) {
            throw new UsuarioEnUsoException(
                    "No se puede eliminar el funcionario: tiene reservas activas.");
        }

        datos.borrar(id);
        return true;
    }
}
