package SRR.Servidor.Enrutadores;

import SRR.DTO.AlertaReservaDTO;
import SRR.DTO.Peticion;
import SRR.DTO.Respuesta;

import java.util.List;
import java.util.function.Consumer;

public class EnrutadorPrincipal implements Enrutador {

    private final List<Enrutador> enrutadores;

    public EnrutadorPrincipal(Consumer<AlertaReservaDTO> obs){
        enrutadores = List.of(
                new LoginEnrutador(),
                new UsuarioEnrutador(),
                new CategoriaEnrutador(),
                new RecursoEnrutador(),
                new ReservaEnrutador(obs)
        );
    }

    @Override
    public boolean maneja(String metodo) {
        for (Enrutador e : enrutadores) {
            if (e.maneja(metodo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Respuesta atender(Peticion peticion) throws Exception {
        for (Enrutador e : enrutadores) {
            if (e.maneja(peticion.getMetodo())) {
                return e.atender(peticion);
            }
        }
        return Respuesta.error("Método desconocido: " + peticion.getMetodo());
    }
}
