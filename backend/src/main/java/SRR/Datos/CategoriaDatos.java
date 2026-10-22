package SRR.Datos;

import SRR.DTO.CategoriaDTO;
import SRR.Excepciones.CategoriaEnUsoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDatos {

    // codigos de error de MySQL
    private static final int ERROR_DUPLICADO = 1062;        // UNIQUE: la descripcion ya existe
    private static final int ERROR_CATEGORIA_EN_USO = 1451; // llave foranea: tiene recursos asociados

    public CategoriaDatos() {
    }

    public List<CategoriaDTO> listar() {
        List<CategoriaDTO> lista = new ArrayList<>();
        String sql = "SELECT id, descripcion FROM categoria ORDER BY id";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(convertir(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron listar las categorías", e);
        }
        return lista;
    }

    // devuelve la categoria con ese id, o null si no existe
    public CategoriaDTO buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        long idNumerico;
        try {
            idNumerico = Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            return null;   // un id que no es numero no puede existir
        }

        String sql = "SELECT id, descripcion FROM categoria WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, idNumerico);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar la categoría", e);
        }
    }

    // la descripcion se busca como texto contenido, sin distinguir mayusculas
    public List<CategoriaDTO> buscarPorDescripcion(String texto) {
        List<CategoriaDTO> lista = new ArrayList<>();
        if (texto == null) {
            return lista;
        }
        String sql = "SELECT id, descripcion FROM categoria WHERE descripcion LIKE ? ORDER BY id";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + escaparLike(texto) + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(convertir(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron buscar las categorías", e);
        }
        return lista;
    }

    private CategoriaDTO convertir(ResultSet rs) throws SQLException {
        return new CategoriaDTO(
                String.valueOf(rs.getLong("id")),
                rs.getString("descripcion"));
    }

    // para que % y _ escritos por el usuario se busquen como texto normal
    private String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public void agregar(CategoriaDTO categoria) {
        String sql = "INSERT INTO categoria (descripcion) VALUES (?)";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getDescripcion());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    categoria.setId(String.valueOf(claves.getLong(1)));   // id que genero la base
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_DUPLICADO) {
                throw new IllegalArgumentException("Ya existe una categoría con esa descripción.");
            }
            throw new RuntimeException("No se pudo agregar la categoría", e);
        }
    }

    public void modificar(CategoriaDTO categoria) {
        String sql = "UPDATE categoria SET descripcion = ? WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, categoria.getDescripcion());
            ps.setLong(2, Long.parseLong(categoria.getId()));

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe la categoría " + categoria.getId() + ".");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_DUPLICADO) {
                throw new IllegalArgumentException("Ya existe una categoría con esa descripción.");
            }
            throw new RuntimeException("No se pudo modificar la categoría", e);
        }
    }

    public void borrar(String id) {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(id));

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe la categoría " + id + ".");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_CATEGORIA_EN_USO) {
                throw new CategoriaEnUsoException(
                        "No se puede eliminar la categoría: todavía tiene recursos asociados.");
            }
            throw new RuntimeException("No se pudo eliminar la categoría", e);
        }
    }
}
