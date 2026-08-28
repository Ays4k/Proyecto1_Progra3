package SRR.Datos;

import SRR.DTO.FuncionarioDTO;
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

public class FuncionarioDatos {
    private String rutaArchivo;
    private Map<String, FuncionarioDTO> mapaFuncionarios = new LinkedHashMap<>();

    public FuncionarioDatos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        deserializar();
    }

    public FuncionarioDatos() {
        this("datos/funcionarios.json");
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
        contenedor.funcionarios = new ArrayList<>(this.mapaFuncionarios.values());

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
        mapaFuncionarios.clear();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }

        Gson gson = new Gson();
        try (FileReader lector = new FileReader(archivo)) {
            Contenedor contenedor = gson.fromJson(lector, Contenedor.class);
            if (contenedor == null || contenedor.funcionarios == null) {
                return;
            }
            for (FuncionarioDTO funcionario : contenedor.funcionarios) {
                mapaFuncionarios.put(funcionario.getId(), funcionario);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + rutaArchivo, e);
        }
    }

    public FuncionarioDTO buscarPorId(String id) {
        return mapaFuncionarios.get(id);
    }

    public List<FuncionarioDTO> listar() {
        return new ArrayList<>(mapaFuncionarios.values());
    }

    public void agregar(FuncionarioDTO funcionario) {
        mapaFuncionarios.put(funcionario.getId(), funcionario);
        serializar();
    }

    public void modificar(FuncionarioDTO funcionario) {
        mapaFuncionarios.put(funcionario.getId(), funcionario);
        serializar();
    }

    public void borrar(String id) {
        mapaFuncionarios.remove(id);
        serializar();
    }

    public List<FuncionarioDTO> buscarPorNombre(String texto) {
        List<FuncionarioDTO> resultado = new ArrayList<>();
        String busqueda = texto.toLowerCase();
        for (FuncionarioDTO funcionario : mapaFuncionarios.values()) {
            if (funcionario.getNombre().toLowerCase().contains(busqueda)) {
                resultado.add(funcionario);
            }
        }
        return resultado;
    }

    private static class Contenedor {
        List<FuncionarioDTO> funcionarios = new ArrayList<>();
    }
}