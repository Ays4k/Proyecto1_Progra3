package SRR.Logica;

import SRR.DTO.AlertaReservaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NotificacionLogica{
    private final UsuarioLogica us = new UsuarioLogica();
    private final RecursoLogica rec = new RecursoLogica();
    private final Consumer<AlertaReservaDTO> observador;

    public NotificacionLogica(Consumer<AlertaReservaDTO> obs){
        if(obs == null){
            throw new NullPointerException("No se puede aceptar un consumer null");
        }
        this.observador = obs;
    }

    public void notificar(ReservaDTO res){
        List<String> recursos = new ArrayList<>();
        try {
            List<String> idRecursos = res.getIdsRecursos();
            if(idRecursos != null) {
                for (String x : idRecursos) {
                    RecursoDTO recurso = rec.buscarPorId(x);
                    if (recurso != null) {
                        recursos.add(recurso.getDescripcion());
                    }
                }
            }
            AlertaReservaDTO alerta = new AlertaReservaDTO(res.getId(), res.getIdFuncionario(), us.nombreDe(res.getIdFuncionario()),
                    res.getActividad(), res.getFecha(), res.getHoraInicio(), res.getHoraFin(), recursos);
            observador.accept(alerta);
        } catch (Exception e){
            System.out.println("No se ha podido realizar la alerta de la reserva: "+res.getId());
            e.printStackTrace();
        }
    }
}
