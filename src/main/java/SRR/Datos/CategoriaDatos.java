package SRR.Datos;

import SRR.DTO.CategoriaDTO;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CategoriaDatos {
    private String rutaArchivo;
    private Map<String, CategoriaDTO> mapaCategorias = new LinkedHashMap<>();

    public CategoriaDatos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        deserializar();
    }

    public CategoriaDatos() {
        this("datos/categorias.json");
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
        contenedor.categorias = new ArrayList<>(this.mapaCategorias.values());

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
        mapaCategorias.clear();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }

        Gson gson = new Gson();

        try (FileReader lector = new FileReader(archivo)) {
            Contenedor contenedor = gson.fromJson(lector, Contenedor.class);
            if (contenedor == null || contenedor.categorias == null) {
                return;
            }
            for (CategoriaDTO categoria : contenedor.categorias) {
                mapaCategorias.put(categoria.getId(), categoria);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + rutaArchivo, e);
        }
    }

    public CategoriaDTO buscarPorId(String id) {
        return mapaCategorias.get(id);
    }

    public List<CategoriaDTO> listar() {
        return new ArrayList<>(mapaCategorias.values());
    }

    public void agregar(CategoriaDTO categoria) {
        mapaCategorias.put(categoria.getId(), categoria);
        serializar();
    }

    public void modificar(CategoriaDTO categoria) {
        mapaCategorias.put(categoria.getId(), categoria);
        serializar();
    }

    public void borrar(String id) {
        mapaCategorias.remove(id);
        serializar();
    }

    public List<CategoriaDTO> buscarPorDescripcion(String texto) {
        List<CategoriaDTO> resultado = new ArrayList<>();
        String busqueda = texto.toLowerCase();
        for (CategoriaDTO categoria : mapaCategorias.values()) {
            if (categoria.getDescripcion().toLowerCase().contains(busqueda)) {
                resultado.add(categoria);
            }
        }
        return resultado;
    }

    private static class Contenedor {
        List<CategoriaDTO> categorias = new ArrayList<>();
    }
}