package SRR.Servicio;

import SRR.DTO.ReservaDTO;
import SRR.Logica.ReservaLogica;

public class ReservaServicio {
    ReservaLogica logica;
    public ReservaDTO crearReserva(ReservaDTO reserva){
        return logica.crearReserva(reserva);
    }
}
