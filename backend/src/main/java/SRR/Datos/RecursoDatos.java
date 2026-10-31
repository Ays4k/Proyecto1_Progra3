package SRR.Datos;

import SRR.DTO.RecursoDTO;
import SRR.Excepciones.RecursoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RecursoDatos {

    private static final int ERROR_CATEGORIA_INEXISTENTE = 1452;
    private static final int ERROR_RECURSO_EN_USO = 1451;

    public List<RecursoDTO> listar() {
        List<RecursoDTO> lista = new ArrayList<>();
        String sql = "SELECT id, descripcion, id_categoria FROM recurso ORDER BY id";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(convertir(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron obtener los recursos", e);
        }
        return lista;
    }

    public RecursoDTO buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        long idNumerico;
        try {
            idNumerico = Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            return null;
        }

        String sql = "SELECT id, descripcion, id_categoria FROM recurso WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, idNumerico);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el recurso", e);
        }
    }

    public List<RecursoDTO> listarPorCategoria(String idCategoria) {
        return filtrar(idCategoria, null);
    }

    public List<RecursoDTO> filtrar(String idCategoria, String descripcion) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, descripcion, id_categoria FROM recurso WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (idCategoria != null && !idCategoria.isBlank()) {
            try {
                parametros.add(Long.parseLong(idCategoria.trim()));
            } catch (NumberFormatException e) {
                return new ArrayList<>();
            }
            sql.append(" AND id_categoria = ?");
        }
        if (descripcion != null && !descripcion.isBlank()) {
            parametros.add("%" + escaparLike(descripcion) + "%");
            sql.append(" AND descripcion LIKE ?");
        }
        sql.append(" ORDER BY id");

        List<RecursoDTO> lista = new ArrayList<>();

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(convertir(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron filtrar los recursos", e);
        }
        return lista;
    }

    private RecursoDTO convertir(ResultSet rs) throws SQLException {
        return new RecursoDTO(
                String.valueOf(rs.getLong("id")),
                rs.getString("descripcion"),
                rs.getString("id_categoria"));
    }

    private String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public void agregar(RecursoDTO recurso) {
        String sql = "INSERT INTO recurso (descripcion, id_categoria) VALUES (?, ?)";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, recurso.getDescripcion());
            ps.setLong(2, Long.parseLong(recurso.getIdCategoria()));
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    recurso.setId(String.valueOf(claves.getLong(1)));
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_CATEGORIA_INEXISTENTE) {
                throw new RecursoException("La categoría seleccionada no existe.");
            }
            throw new RuntimeException("No se pudo agregar el recurso", e);
        }
    }

    public void modificar(RecursoDTO recurso) {
        String sql = "UPDATE recurso SET descripcion = ?, id_categoria = ? WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, recurso.getDescripcion());
            ps.setLong(2, Long.parseLong(recurso.getIdCategoria()));
            ps.setLong(3, Long.parseLong(recurso.getId()));

            if (ps.executeUpdate() == 0) {
                throw new RecursoException("No existe el recurso " + recurso.getId() + ".");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_CATEGORIA_INEXISTENTE) {
                throw new RecursoException("La categoría seleccionada no existe.");
            }
            throw new RuntimeException("No se pudo modificar el recurso", e);
        }
    }

    public void borrar(String id) {
        String sql = "DELETE FROM recurso WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(id));

            if (ps.executeUpdate() == 0) {
                throw new RecursoException("No existe el recurso " + id + ".");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_RECURSO_EN_USO) {
                throw new RecursoException(
                        "No se puede eliminar el recurso porque está asignado a una reserva.");
            }
            throw new RuntimeException("No se pudo eliminar el recurso", e);
        }
    }
}
