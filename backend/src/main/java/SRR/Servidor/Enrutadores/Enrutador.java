package SRR.Servidor.Enrutadores;

import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;

public interface Enrutador {

    boolean maneja(String metodo);

    Respuesta atender(Peticion peticion) throws Exception;
}
