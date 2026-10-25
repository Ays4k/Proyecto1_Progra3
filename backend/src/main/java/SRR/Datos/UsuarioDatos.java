package SRR.Datos;

import SRR.DTO.UsuarioDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDatos {


    public UsuarioDatos() {
    }


    public List<UsuarioDTO> listar() {
        List<UsuarioDTO> listaUsuarios = new ArrayList<>();
        String sql = "SELECT u.identificacion, u.nombre, u.telefono, r.nombre AS rol "+
                "FROM usuario u "+
                "Join rol r ON r.id = u.id_rol " +
                "ORDER BY u.id";

        try(Connection c = ConexionBD.obtenerConexion();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            while (rs.next()){
                listaUsuarios.add(new UsuarioDTO(
                        rs.getString("identificacion"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        null,
                        rs.getString("rol")));
            }

        }catch (SQLException e){
            throw new RuntimeException("No se pudo obtener los usuarios",e);
        }
        return listaUsuarios;
    }

    public UsuarioDTO buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String sql = "SELECT u.identificacion, u.nombre, u.telefono, r.nombre AS rol "
                + "FROM usuario u JOIN rol r ON r.id = u.id_rol "
                + "WHERE u.identificacion = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UsuarioDTO(
                            rs.getString("identificacion"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            null,
                            rs.getString("rol"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el usuario", e);
        }
    }

    public String validarCredenciales(String id, String contrasena) {
        if (id == null || contrasena == null) {
            return null;
        }
        String sql = "SELECT u.identificacion FROM usuario u "
                + "WHERE u.identificacion = ? "
                + "AND COALESCE(u.clave, u.identificacion) = BINARY ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, contrasena);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("identificacion");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al validar credenciales",e);
        }
        return null;
    }

    public void cambiarContrasena(String id, String nuevaContrasena) {
        String sql = "UPDATE usuario SET clave = ? WHERE identificacion = ?";

        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nuevaContrasena);
            ps.setString(2, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Usuario no encontrado");
            }

        } catch (SQLException e) {
            throw new RuntimeException("No se pudo cambiar la contraseña", e);
        }
    }
    public void agregar(UsuarioDTO funcionario) {
        String sql = "INSERT INTO usuario (identificacion, nombre, clave, telefono, id_rol) VALUES (?,?,?,?,(SELECT id FROM rol WHERE nombre = ?))";
        try(Connection c = ConexionBD.obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,funcionario.getId());
            ps.setString(2,funcionario.getNombre());
            ps.setString(3,funcionario.getContrasena());
            ps.setString(4,funcionario.getTelefono());
            ps.setString(5,funcionario.getRol());

            ps.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException("No se pudo agregar el usuario", e);
        }
    }

    public void modificar(UsuarioDTO funcionario) {
        String sql = "UPDATE usuario SET nombre = ?, telefono = ?, id_rol = (SELECT r.id FROM rol AS r WHERE r.nombre = ?)" +
                " WHERE identificacion = ?";
        try(Connection c = ConexionBD.obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,funcionario.getNombre());
            ps.setString(2,funcionario.getTelefono());
            ps.setString(3,funcionario.getRol());
            ps.setString(4,funcionario.getId());


            ps.executeUpdate();


        }catch (SQLException e){
            throw new RuntimeException("No se pudo modificar el usuario", e);
        }
    }

    public void borrar(String id) {
        String sql = "DELETE FROM usuario WHERE  identificacion = ?";
        try(Connection c = ConexionBD.obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,id);
            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("No se pudo eliminar el usuario", e);
        }
    }

}
