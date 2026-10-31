package SRR.Servidor.Enrutadores;

import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;

import java.util.HashMap;
import java.util.Map;

public abstract class EnrutadorBase implements Enrutador {

    private final Map<String, Manejador> manejadores = new HashMap<>();

    protected void registrar(String metodo, Manejador manejador) {
        manejadores.put(metodo, manejador);
    }

    @Override
    public boolean maneja(String metodo) {
        return manejadores.containsKey(metodo);
    }

    @Override
    public Respuesta atender(Peticion peticion) throws Exception {
        Manejador manejador = manejadores.get(peticion.getMetodo());
        if (manejador == null) {
            return Respuesta.error("Método no soportado: " + peticion.getMetodo());
        }
        return Respuesta.ok(manejador.ejecutar(peticion.getParametros()));
    }
}
