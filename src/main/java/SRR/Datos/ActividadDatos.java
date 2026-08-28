package SRR.Datos;

import SRR.DTO.ActividadDTO;
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

public class ActividadDatos {
    private String rutaArchivo;
    private Map<String, ActividadDTO> mapaActividades = new LinkedHashMap<>();

    public ActividadDatos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        deserializar();
    }

    public ActividadDatos() {
        this("datos/actividades.json");
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
        contenedor.actividades = new ArrayList<>(this.mapaActividades.values());

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
        mapaActividades.clear();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }

        Gson gson = new Gson();
        try (FileReader lector = new FileReader(archivo)) {
            Contenedor contenedor = gson.fromJson(lector, Contenedor.class);
            if (contenedor == null || contenedor.actividades == null) {
                return;
            }
            for (ActividadDTO actividad : contenedor.actividades) {
                mapaActividades.put(actividad.getId(), actividad);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + rutaArchivo, e);
        }
    }

    public ActividadDTO buscarPorId(String id) {
        return mapaActividades.get(id);
    }

    public List<ActividadDTO> listar() {
        return new ArrayList<>(mapaActividades.values());
    }

    public void agregar(ActividadDTO actividad) {
        mapaActividades.put(actividad.getId(), actividad);
        serializar();
    }

    public void modificar(ActividadDTO actividad) {
        mapaActividades.put(actividad.getId(), actividad);
        serializar();
    }

    public void borrar(String id) {
        mapaActividades.remove(id);
        serializar();
    }

    public List<ActividadDTO> buscarPorNombre(String texto) {
        List<ActividadDTO> resultado = new ArrayList<>();
        String busqueda = texto.toLowerCase();
        for (ActividadDTO actividad : mapaActividades.values()) {
            if (actividad.getNombre().toLowerCase().contains(busqueda)) {
                resultado.add(actividad);
            }
        }
        return resultado;
    }

    private static class Contenedor {
        List<ActividadDTO> actividades = new ArrayList<>();
    }
}