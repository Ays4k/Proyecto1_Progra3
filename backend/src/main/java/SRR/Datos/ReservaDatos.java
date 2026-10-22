package SRR.Datos;

import SRR.DTO.ReservaDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReservaDatos {

    // codigos de error de MySQL
    private static final int ERROR_FUNCIONARIO_INEXISTENTE = 1048; // la subconsulta de usuario devolvio NULL
    private static final int ERROR_RECURSO_INEXISTENTE = 1452;     // llave foranea: el recurso no existe

    public List<ReservaDTO> listar() {
        List<ReservaDTO> lista = new ArrayList<>();
        String sql = "SELECT r.id, u.identificacion, r.descripcion, r.fecha, r.hora_inicio, r.hora_fin, r.estado "
                   + "FROM reserva r JOIN usuario u ON u.id = r.id_usuario ORDER BY r.id";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(convertir(rs));
            }
            cargarRecursos(c, lista);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron obtener las reservas", e);
        }
        return lista;
    }

    // devuelve la reserva con ese id, o null si no existe
    public ReservaDTO buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        long idNumerico;
        try {
            idNumerico = Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            return null;   // un id que no es numero no puede existir
        }

        String sql = "SELECT r.id, u.identificacion, r.descripcion, r.fecha, r.hora_inicio, r.hora_fin, r.estado "
                   + "FROM reserva r JOIN usuario u ON u.id = r.id_usuario WHERE r.id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, idNumerico);

            List<ReservaDTO> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(convertir(rs));
                }
            }
            cargarRecursos(c, lista);
            return lista.isEmpty() ? null : lista.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar la reserva", e);
        }
    }

    // reservas de un funcionario, por su cedula
    public List<ReservaDTO> listarPorFuncionario(String cedula) {
        List<ReservaDTO> lista = new ArrayList<>();
        if (cedula == null || cedula.isBlank()) {
            return lista;
        }
        String sql = "SELECT r.id, u.identificacion, r.descripcion, r.fecha, r.hora_inicio, r.hora_fin, r.estado "
                   + "FROM reserva r JOIN usuario u ON u.id = r.id_usuario "
                   + "WHERE u.identificacion = ? ORDER BY r.id";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cedula.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(convertir(rs));
                }
            }
            cargarRecursos(c, lista);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron obtener las reservas del funcionario", e);
        }
        return lista;
    }

    // true si el recurso esta en una reserva ACTIVA que se traslapa con ese rango
    public boolean recursoOcupado(String idRecurso, String fecha, String horaInicio, String horaFin) {
        String sql = "SELECT 1 FROM reserva_recurso rr JOIN reserva r ON r.id = rr.id_reserva "
                   + "WHERE rr.id_recurso = ? AND r.estado = 'ACTIVA' AND r.fecha = ? "
                   + "AND ? < r.hora_fin AND r.hora_inicio < ? LIMIT 1";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(idRecurso));
            ps.setString(2, fecha);
            ps.setString(3, horaInicio);
            ps.setString(4, horaFin);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo revisar la disponibilidad del recurso", e);
        }
    }

    // true si el recurso esta en alguna reserva ACTIVA, sin importar la fecha
    public boolean recursoEnReservaActiva(String idRecurso) {
        String sql = "SELECT 1 FROM reserva_recurso rr JOIN reserva r ON r.id = rr.id_reserva "
                   + "WHERE rr.id_recurso = ? AND r.estado = 'ACTIVA' LIMIT 1";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(idRecurso));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo revisar las reservas del recurso", e);
        }
    }

    // arma el DTO con los datos de la reserva; los recursos se cargan aparte
    private ReservaDTO convertir(ResultSet rs) throws SQLException {
        return new ReservaDTO(
                String.valueOf(rs.getLong("id")),
                rs.getString("identificacion"),
                new ArrayList<>(),
                rs.getString("descripcion"),
                rs.getString("fecha"),                          // yyyy-MM-dd
                rs.getString("hora_inicio").substring(0, 5),    // HH:mm:ss -> HH:mm
                rs.getString("hora_fin").substring(0, 5),
                rs.getString("estado"));
    }

    // llena idsRecursos de cada reserva con una consulta a reserva_recurso
    private void cargarRecursos(Connection c, List<ReservaDTO> reservas) throws SQLException {
        String sql = "SELECT id_recurso FROM reserva_recurso WHERE id_reserva = ? ORDER BY id_recurso";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ReservaDTO reserva : reservas) {
                ps.setLong(1, Long.parseLong(reserva.getId()));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        reserva.getIdsRecursos().add(String.valueOf(rs.getLong("id_recurso")));
                    }
                }
            }
        }
    }

    // guarda la reserva y sus recursos en una transaccion: o se guarda todo o nada
    public void agregar(ReservaDTO reserva) {
        String sql = "INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) "
                   + "VALUES (?, ?, ?, ?, (SELECT id FROM usuario WHERE identificacion = ?), ?)";

        try (Connection c = ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                long idNuevo;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, reserva.getActividad());
                    ps.setString(2, reserva.getFecha());
                    ps.setString(3, reserva.getHoraInicio());
                    ps.setString(4, reserva.getHoraFin());
                    ps.setString(5, reserva.getIdFuncionario());
                    ps.setString(6, reserva.getEstado());
                    ps.executeUpdate();

                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        idNuevo = claves.getLong(1);
                    }
                }
                insertarRecursos(c, idNuevo, reserva.getIdsRecursos());
                c.commit();
                reserva.setId(String.valueOf(idNuevo));   // id que genero la base
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "No se pudo agregar la reserva");
        }
    }

    // actualiza la reserva y vuelve a escribir sus recursos, en una transaccion
    public void modificar(ReservaDTO reserva) {
        String sql = "UPDATE reserva SET descripcion = ?, fecha = ?, hora_inicio = ?, hora_fin = ?, "
                   + "estado = ? WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                long id = Long.parseLong(reserva.getId());

                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, reserva.getActividad());
                    ps.setString(2, reserva.getFecha());
                    ps.setString(3, reserva.getHoraInicio());
                    ps.setString(4, reserva.getHoraFin());
                    ps.setString(5, reserva.getEstado());
                    ps.setLong(6, id);

                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException("No existe la reserva " + reserva.getId() + ".");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM reserva_recurso WHERE id_reserva = ?")) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                insertarRecursos(c, id, reserva.getIdsRecursos());
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "No se pudo modificar la reserva");
        }
    }

    // sus filas de reserva_recurso se borran solas (ON DELETE CASCADE)
    public void borrar(String id) {
        String sql = "DELETE FROM reserva WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(id));

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe la reserva " + id + ".");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar la reserva", e);
        }
    }

    // una fila en reserva_recurso por recurso, con la misma conexion de la transaccion
    private void insertarRecursos(Connection c, long idReserva, List<String> idsRecursos) throws SQLException {
        if (idsRecursos == null || idsRecursos.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES (?, ?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (String idRecurso : idsRecursos) {
                ps.setLong(1, idReserva);
                ps.setLong(2, Long.parseLong(idRecurso));
                ps.executeUpdate();
            }
        }
    }

    private RuntimeException traducir(SQLException e, String mensaje) {
        if (e.getErrorCode() == ERROR_FUNCIONARIO_INEXISTENTE) {
            return new IllegalArgumentException("El funcionario de la reserva no existe.");
        }
        if (e.getErrorCode() == ERROR_RECURSO_INEXISTENTE) {
            return new IllegalArgumentException("Alguno de los recursos de la reserva no existe.");
        }
        return new RuntimeException(mensaje, e);
    }
}
