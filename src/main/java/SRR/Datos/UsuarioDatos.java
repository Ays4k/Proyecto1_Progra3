package SRR.Datos;

import SRR.DTO.UsuarioDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UsuarioDatos {
    private String rutaArchivo;
    private Map<String, UsuarioDTO> mapaUsuarios = new LinkedHashMap<>();

    public UsuarioDatos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        deserializar();
    }

    public UsuarioDatos() {
        this("datos/usuarios.json");
    }


    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public void serializar() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Contenedor contenedor = new Contenedor();
        contenedor.usuarios = new ArrayList<>(this.mapaUsuarios.values());

        File archivo = new File(rutaArchivo);
        File carpeta = archivo.getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }

        try (FileWriter escritor = new FileWriter(archivo)) {
            gson.toJson(contenedor, escritor);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir " + rutaArchivo, e);
        }
    }

    public void deserializar() {
        mapaUsuarios.clear();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }

        Gson gson = new Gson();
        try (FileReader lector = new FileReader(archivo)) {
            Contenedor contenedor = gson.fromJson(lector, Contenedor.class);
            if (contenedor == null || contenedor.usuarios == null) {
                return;
            }
            for (UsuarioDTO funcionario : contenedor.usuarios) {
                mapaUsuarios.put(funcionario.getId(), funcionario);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + rutaArchivo, e);
        }
    }

    public List<UsuarioDTO> listar() {
        /*
        List<UsuarioDTO> listaUsuarios = new ArrayList<>();
        for(UsuarioDTO x : mapaUsuarios.values()){
            listaUsuarios.add(new UsuarioDTO(x));
        }
         */

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
            e.printStackTrace();
        }
        return listaUsuarios;
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
            e.printStackTrace();
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
            /*
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    funcionario.setId(String.valueOf(claves.getLong(1)));
                }
            }
            */

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

    private static class Contenedor {
        List<UsuarioDTO> usuarios = new ArrayList<>();
    }
}