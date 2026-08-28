package SRR.Datos;

import SRR.DTO.RecursoDTO;
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

public class RecursoDatos {
    private String rutaArchivo;
    private Map<String, RecursoDTO> mapaRecursos = new LinkedHashMap<>();

    public RecursoDatos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        deserializar();
    }

    public RecursoDatos() {
        this("datos/recursos.json");
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
        contenedor.recursos = new ArrayList<>(this.mapaRecursos.values());

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
        mapaRecursos.clear();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }

        Gson gson = new Gson();
        try (FileReader lector = new FileReader(archivo)) {
            Contenedor contenedor = gson.fromJson(lector, Contenedor.class);
            if (contenedor == null || contenedor.recursos == null) {
                return;
            }
            for (RecursoDTO recurso : contenedor.recursos) {
                mapaRecursos.put(recurso.getId(), recurso);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + rutaArchivo, e);
        }
    }

    public RecursoDTO buscarPorId(String id) {
        return mapaRecursos.get(id);
    }

    public List<RecursoDTO> listar() {
        return new ArrayList<>(mapaRecursos.values());
    }

    public void agregar(RecursoDTO recurso) {
        mapaRecursos.put(recurso.getId(), recurso);
        serializar();
    }

    public void modificar(RecursoDTO recurso) {
        mapaRecursos.put(recurso.getId(), recurso);
        serializar();
    }

    public void borrar(String id) {
        mapaRecursos.remove(id);
        serializar();
    }

    public List<RecursoDTO> buscarPorNombre(String texto) {
        List<RecursoDTO> resultado = new ArrayList<>();
        String busqueda = texto.toLowerCase();
        for (RecursoDTO recurso : mapaRecursos.values()) {
            if (recurso.getNombre().toLowerCase().contains(busqueda)) {
                resultado.add(recurso);
            }
        }
        return resultado;
    }

    private static class Contenedor {
        List<RecursoDTO> recursos = new ArrayList<>();
    }
}