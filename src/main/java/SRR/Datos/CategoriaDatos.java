package SRR.Datos;

import SRR.DTO.CategoriaDTO;

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

import SRR.Excepciones.CategoriaEnUsoException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CategoriaDatos {


    public CategoriaDatos() {
    }


    public List<CategoriaDTO> listar() {

        List<CategoriaDTO> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.descripcion FROM categoria AS c ORDER BY c.id";

        try(Connection c = ConexionBD.obtenerConexion();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            while (rs.next()){
                lista.add(new CategoriaDTO(rs.getString("id"),rs.getString("descripcion")));
            }
        }catch (SQLException e){
            throw new RuntimeException("No se pudo listar las categorías",e);
        }
        return lista;
    }

    public void agregar(CategoriaDTO categoria) {

        String sql = "INSERT INTO categoria(descripcion) VALUES (?)";

        try(Connection c = ConexionBD.obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,categoria.getDescripcion());
            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("No se pudo agregar la categoría",e);
        }

    }

    public void modificar(CategoriaDTO categoria) {
        String sql = "UPDATE categoria SET descripcion = ? WHERE id = ?";

        try(Connection c = ConexionBD.obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,categoria.getDescripcion());
            ps.setString(2,categoria.getId());
            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("No se pudo modificar la categoría",e);
        }
    }

    public void borrar(String id) {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try(Connection c = ConexionBD.obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1,id);
            ps.executeUpdate();
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("La categoría no existe.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new CategoriaEnUsoException(
                        "No se puede eliminar la categoría: todavía tiene recursos asociados.");
            }
            throw new RuntimeException("No se pudo eliminar la categoría", e);
        }

    }

}