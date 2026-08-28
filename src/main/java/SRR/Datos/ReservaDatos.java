package SRR.Datos;

import SRR.DTO.ReservaDTO;
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

public class ReservaDatos {
    private String rutaArchivo;
    private Map<String, ReservaDTO> mapaReservas = new LinkedHashMap<>();

    public ReservaDatos(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        deserializar();
    }

    public ReservaDatos() {
        this("datos/reservas.json");
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
        contenedor.reservas = new ArrayList<>(this.mapaReservas.values());

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
        mapaReservas.clear();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }

        Gson gson = new Gson();
        try (FileReader lector = new FileReader(archivo)) {
            Contenedor contenedor = gson.fromJson(lector, Contenedor.class);
            if (contenedor == null || contenedor.reservas == null) {
                return;
            }
            for (ReservaDTO reserva : contenedor.reservas) {
                mapaReservas.put(reserva.getId(), reserva);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + rutaArchivo, e);
        }
    }

    public ReservaDTO buscarPorId(String id) {
        return mapaReservas.get(id);
    }

    public List<ReservaDTO> listar() {
        return new ArrayList<>(mapaReservas.values());
    }

    public void agregar(ReservaDTO reserva) {
        mapaReservas.put(reserva.getId(), reserva);
        serializar();
    }

    public void modificar(ReservaDTO reserva) {
        mapaReservas.put(reserva.getId(), reserva);
        serializar();
    }

    public void borrar(String id) {
        mapaReservas.remove(id);
        serializar();
    }

    public List<ReservaDTO> buscarPorFuncionario(String idFuncionario) {
        List<ReservaDTO> resultado = new ArrayList<>();
        for (ReservaDTO reserva : mapaReservas.values()) {
            if (idFuncionario.equalsIgnoreCase(reserva.getIdFuncionario())) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    private static class Contenedor {
        List<ReservaDTO> reservas = new ArrayList<>();
    }
}