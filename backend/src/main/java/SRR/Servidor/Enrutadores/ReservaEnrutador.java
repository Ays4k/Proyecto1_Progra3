package SRR.Servidor.Enrutadores;

import SRR.DTO.AlertaReservaDTO;
import SRR.DTO.Protocolo;
import SRR.Servicio.ReservaServicio;

import java.util.List;
import java.util.function.Consumer;

public class ReservaEnrutador extends EnrutadorBase {
    private final ReservaServicio servicio;


    @SuppressWarnings("unchecked")
    public ReservaEnrutador(Consumer<AlertaReservaDTO> obs) {
        servicio = new ReservaServicio(obs);
        registrar(Protocolo.CREAR_RESERVA,
                p -> servicio.crearReserva((String) p[0], (String) p[1], (String) p[2],
                        (String) p[3], (String) p[4], (List<String>) p[5]));

        registrar(Protocolo.CANCELAR_RESERVA, p -> {
            servicio.cancelarReserva((String) p[0]);
            return null;
        });

        registrar(Protocolo.GENERAR_RESERVA_AI,
                p -> servicio.generarReservaAi((String) p[0]));

        registrar(Protocolo.RESERVAS_ACTIVAS_DE,
                p -> servicio.reservasActivasDe((String) p[0]));

        registrar(Protocolo.RECURSO_DISPONIBLE,
                p -> servicio.estaDisponible((String) p[0]));

        registrar(Protocolo.RECURSO_DISPONIBLE_EN_HORARIO,
                p -> servicio.estaDisponible((String) p[0], (String) p[1],
                        (String) p[2], (String) p[3]));

        registrar(Protocolo.RECURSOS_DISPONIBLES,
                p -> servicio.recursosDisponibles((String) p[0], (String) p[1],
                        (String) p[2], (String) p[3]));

        registrar(Protocolo.OBTENER_TODAS_LAS_RESERVAS,
                p -> servicio.obtenerTodasLasReservas());
    }
}
