package SRR.Datos;

import SRR.DTO.ReservaDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReservaDatos {

    // codigos de error de MySQL
    private static final int ERROR_NULO = 1048;             // el funcionario no existe (la subconsulta devolvio NULL)
    private static final int ERROR_LLAVE_INEXISTENTE = 1452; // el recurso no existe

    // una reserva ocupa varias filas (una por recurso), por eso el LEFT JOIN;
    // las demas consultas solo le agregan un WHERE
    private static final String SELECT_RESERVAS =
            "SELECT r.id, u.identificacion, r.descripcion, r.fecha, r.hora_inicio, r.hora_fin, "
          + "r.estado, rr.id_recurso "
          + "FROM reserva r "
          + "JOIN usuario u ON u.id = r.id_usuario "
          + "LEFT JOIN reserva_recurso rr ON rr.id_reserva = r.id ";
    private static final String ORDEN = " ORDER BY r.id, rr.id_recurso";

    public ReservaDatos() {
    }

    // ------------------------------------------------------------------ consultas

    public List<ReservaDTO> listar() {
        return consultar(SELECT_RESERVAS + ORDEN, "No se pudieron obtener las reservas");
    }

    // devuelve la reserva con ese id, o null si no existe
    public ReservaDTO buscarPorId(String id) {
        long idNumerico;
        try {
            idNumerico = Long.parseLong(id.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;   // un id que no es numero no puede existir
        }
        List<ReservaDTO> lista = consultar(SELECT_RESERVAS + "WHERE r.id = ?" + ORDEN,
                "No se pudo buscar la reserva", idNumerico);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // reservas de un funcionario, por su cedula
    public List<ReservaDTO> listarPorFuncionario(String cedula) {
        if (cedula == null) {
            return new ArrayList<>();
        }
        return consultar(SELECT_RESERVAS + "WHERE u.identificacion = ?" + ORDEN,
                "No se pudieron obtener las reservas del funcionario", cedula);
    }

    // true si el recurso esta en alguna reserva ACTIVA que se traslapa con el rango pedido
    public boolean recursoOcupado(String idRecurso, String fecha, String horaInicio, String horaFin) {
        String sql = "SELECT 1 FROM reserva_recurso rr "
                   + "JOIN reserva r ON r.id = rr.id_reserva "
                   + "WHERE rr.id_recurso = ? AND r.estado = 'ACTIVA' AND r.fecha = ? "
                   + "AND ? < r.hora_fin AND r.hora_inicio < ? LIMIT 1";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(idRecurso));
            ps.setObject(2, LocalDate.parse(fecha));
            ps.setObject(3, LocalTime.parse(horaInicio));
            ps.setObject(4, LocalTime.parse(horaFin));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo revisar la disponibilidad del recurso", e);
        }
    }

    // true si el recurso esta en alguna reserva ACTIVA, sin importar la fecha
    public boolean recursoEnReservaActiva(String idRecurso) {
        String sql = "SELECT 1 FROM reserva_recurso rr "
                   + "JOIN reserva r ON r.id = rr.id_reserva "
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

    private List<ReservaDTO> consultar(String sql, String mensajeError, Object... parametros) {
        // LinkedHashMap: conserva el orden y junta las filas de una misma reserva
        Map<Long, ReservaDTO> reservas = new LinkedHashMap<>();

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    ReservaDTO reserva = reservas.get(id);
                    if (reserva == null) {
                        reserva = new ReservaDTO(
                                String.valueOf(id),
                                rs.getString("identificacion"),
                                new ArrayList<>(),
                                rs.getString("descripcion"),
                                rs.getObject("fecha", LocalDate.class).toString(),          // yyyy-MM-dd
                                rs.getObject("hora_inicio", LocalTime.class).toString(),    // HH:mm
                                rs.getObject("hora_fin", LocalTime.class).toString(),
                                rs.getString("estado"));
                        reservas.put(id, reserva);
                    }
                    long idRecurso = rs.getLong("id_recurso");
                    if (!rs.wasNull()) {   // LEFT JOIN: una reserva sin recursos trae NULL
                        reserva.getIdsRecursos().add(String.valueOf(idRecurso));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(mensajeError, e);
        }
        return new ArrayList<>(reservas.values());
    }

    // ------------------------------------------------------------------ escritura

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
                    ps.setObject(2, LocalDate.parse(reserva.getFecha()));
                    ps.setObject(3, LocalTime.parse(reserva.getHoraInicio()));
                    ps.setObject(4, LocalTime.parse(reserva.getHoraFin()));
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

    // actualiza la reserva y vuelve a escribir sus recursos, todo en una transaccion
    public void modificar(ReservaDTO reserva) {
        String sql = "UPDATE reserva SET descripcion = ?, fecha = ?, hora_inicio = ?, hora_fin = ?, "
                   + "estado = ? WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                long id = Long.parseLong(reserva.getId());

                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, reserva.getActividad());
                    ps.setObject(2, LocalDate.parse(reserva.getFecha()));
                    ps.setObject(3, LocalTime.parse(reserva.getHoraInicio()));
                    ps.setObject(4, LocalTime.parse(reserva.getHoraFin()));
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

    // una fila en reserva_recurso por cada recurso, con la misma conexion de la transaccion
    private void insertarRecursos(Connection c, long idReserva, List<String> idsRecursos) throws SQLException {
        if (idsRecursos == null || idsRecursos.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES (?, ?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (String idRecurso : idsRecursos) {
                ps.setLong(1, idReserva);
                ps.setLong(2, Long.parseLong(idRecurso));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private RuntimeException traducir(SQLException e, String mensaje) {
        if (e.getErrorCode() == ERROR_NULO) {
            return new IllegalArgumentException("El funcionario de la reserva no existe.");
        }
        if (e.getErrorCode() == ERROR_LLAVE_INEXISTENTE) {
            return new IllegalArgumentException("Alguno de los recursos de la reserva no existe.");
        }
        return new RuntimeException(mensaje, e);
    }
}
