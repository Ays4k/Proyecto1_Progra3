package SRR.Proxy;

import SRR.Comunicacion.Conexion;

import SRR.DTO.Protocolo;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaAiDTO;
import SRR.DTO.ReservaDTO;

import java.util.ArrayList;
import java.util.List;

// Representa en el frontend al ReservaServicio del backend
public class ReservaProxy extends ProxyBase {

    public ReservaDTO crearReserva(String idFuncionario, String actividad, String fecha,
                                   String horaInicio, String horaFin, List<String> idsCategorias) {
        // se copia a ArrayList para asegurar que la lista se pueda serializar
        return enviar(Protocolo.CREAR_RESERVA, idFuncionario, actividad, fecha,
                horaInicio, horaFin, new ArrayList<>(idsCategorias));
    }

    public void cancelarReserva(String idReserva) {
        enviar(Protocolo.CANCELAR_RESERVA, idReserva);
    }

    // Gemini puede tardar varios segundos, por eso usa un tiempo de espera mas largo
    public ReservaAiDTO generarReservaAi(String prompt) {
        return enviarConEspera(Conexion.getInstance().getTiempoEsperaIa(),
                Protocolo.GENERAR_RESERVA_AI, prompt);
    }

    public List<ReservaDTO> reservasActivasDe(String idFuncionario) {
        return enviarLista(Protocolo.RESERVAS_ACTIVAS_DE, idFuncionario);
    }

    public boolean estaDisponible(String idRecurso, String fecha, String horaInicio, String horaFin) {
        Boolean resultado = enviar(Protocolo.RECURSO_DISPONIBLE_EN_HORARIO, idRecurso, fecha, horaInicio, horaFin);
        return resultado;
    }

    public boolean estaDisponible(String idRecurso) {
        Boolean resultado = enviar(Protocolo.RECURSO_DISPONIBLE, idRecurso);
        return resultado;
    }

    public List<RecursoDTO> recursosDisponibles(String idCategoria, String fecha,
                                                String horaInicio, String horaFin) {
        return enviarLista(Protocolo.RECURSOS_DISPONIBLES, idCategoria, fecha, horaInicio, horaFin);
    }

    public List<ReservaDTO> obtenerTodasLasReservas() {
        return enviarLista(Protocolo.OBTENER_TODAS_LAS_RESERVAS);
    }
}