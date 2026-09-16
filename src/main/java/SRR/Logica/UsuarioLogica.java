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

    public UsuarioLogica(String rutaTemporal){
        this.datos = new UsuarioDatos(rutaTemporal);
    }

    private String generarId() {
        int mayor = 0;
        for (UsuarioDTO usuario : datos.listar()) {
            String id = usuario.getId();
            if (id != null && id.startsWith("US-")) {
                try {
                    mayor = Math.max(mayor, Integer.parseInt(id.substring(4)));
                } catch (NumberFormatException e) {
                    // formato distinto, se ignora
                }
            }
        }
        return String.format("US-%06d", mayor + 1);
    }

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
        if(usuario.getId() == null){
            usuario.setId(generarId());
            datos.agregar(usuario);
            return 1;
        }
        if (buscarPorId(usuario.getId()) != null) {
            datos.modificar(usuario);
            return 2;
        }
        return -1;
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
