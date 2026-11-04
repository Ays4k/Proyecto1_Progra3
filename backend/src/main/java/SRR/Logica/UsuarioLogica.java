package SRR.Logica;

import SRR.DTO.ReservaDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Datos.ReservaDatos;
import SRR.Datos.UsuarioDatos;
import SRR.Excepciones.UsuarioEnUsoException;

import java.util.ArrayList;
import java.util.List;

public class UsuarioLogica {

    private final UsuarioDatos datos;
    private final ReservaDatos reservaDatos = new ReservaDatos();

    public UsuarioLogica(){
        this.datos = new UsuarioDatos();
    }


    private static final String FORMATO_IDENTIFICACION = "\\d{9,12}";

    public List<UsuarioDTO> obtenerUsuarios() {
        return datos.listar();
    }

    public UsuarioDTO buscarPorId(String id) {
        return datos.buscarPorId(id);
    }

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

    public int cambiosUsuario(UsuarioDTO usuario) {
        validarDatos(usuario);
        if (buscarPorId(usuario.getId()) != null) {
            datos.modificar(usuario);
            return 2;
        }
        validarIdentificacion(usuario.getId());
        datos.agregar(usuario);
        return 1;
    }

    private void validarDatos(UsuarioDTO usuario) {
        String nombre = usuario.getNombre();
        String telefono = usuario.getTelefono();
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (nombre.length() > 50) {
            throw new IllegalArgumentException("El nombre no puede tener más de 50 caracteres.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío.");
        }
        if (telefono.length() > 15) {
            throw new IllegalArgumentException("El teléfono no puede tener más de 15 caracteres.");
        }
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

        if (tieneReservasActivas(id)) {
            throw new UsuarioEnUsoException(
                    "No se puede eliminar el funcionario: tiene reservas activas.");
        }

        datos.borrar(id);
        return true;
    }

    private boolean tieneReservasActivas(String idFuncionario) {
        for (ReservaDTO reserva : reservaDatos.listarPorFuncionario(idFuncionario)) {
            if ("ACTIVA".equals(reserva.getEstado())) {
                return true;
            }
        }
        return false;
    }
}
