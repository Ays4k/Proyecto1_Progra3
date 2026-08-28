package SRR.Datos;

import SRR.DTO.UsuarioDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    public UsuarioDTO buscarPorId(String id) {
        return mapaUsuarios.get(id);
    }

    public List<UsuarioDTO> listar() {
        return new ArrayList<>(mapaUsuarios.values());
    }

    public void agregar(UsuarioDTO funcionario) {
        mapaUsuarios.put(funcionario.getId(), funcionario);
        serializar();
    }

    public void modificar(UsuarioDTO funcionario) {
        mapaUsuarios.put(funcionario.getId(), funcionario);
        serializar();
    }

    public void borrar(String id) {
        mapaUsuarios.remove(id);
        serializar();
    }

    public List<UsuarioDTO> buscarPorNombre(String texto) {
        List<UsuarioDTO> resultado = new ArrayList<>();
        String busqueda = texto.toLowerCase();
        for (UsuarioDTO usuario : mapaUsuarios.values()) {
            String nombre = usuario.getNombre();
            if (nombre != null && nombre.toLowerCase().contains(busqueda)) {
                resultado.add(usuario);
            }
        }
        return resultado;
    }

    private static class Contenedor {
        List<UsuarioDTO> usuarios = new ArrayList<>();
    }
}