package SRR;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;
import SRR.Datos.CategoriaDatos;
import SRR.Datos.RecursoDatos;
import SRR.Logica.ReservaLogica;
import SRR.Excepciones.CategoriasNoDisponiblesException;

import java.util.List;

public class PruebaReservas {

    public static void main(String[] args) {
        prepararDatos();

        ReservaLogica logica = new ReservaLogica();

        System.out.println("--- Disponibilidad de la laptop 238715 el 2026-08-05 ---");
        System.out.println("09:00-11:00 (ocupada)      -> " + logica.estaDisponible("238715", "2026-08-05", "09:00", "11:00"));
        System.out.println("14:00-16:00 (otra hora)    -> " + logica.estaDisponible("238715", "2026-08-05", "14:00", "16:00"));
        System.out.println("11:00-13:00 (justo despues)-> " + logica.estaDisponible("238715", "2026-08-05", "11:00", "13:00"));
        System.out.println("10:00-12:00 (se traslapa)  -> " + logica.estaDisponible("238715", "2026-08-05", "10:00", "12:00"));
        System.out.println("09:00-11:00 (otro dia)     -> " + logica.estaDisponible("238715", "2026-08-06", "09:00", "11:00"));

        System.out.println();
        System.out.println("--- Reserva que deberia funcionar ---");
        try {
            ReservaDTO r = logica.crearReserva("111", "Capacitacion", "2026-08-05",
                    "14:00", "16:00", List.of("CAT-000002"));
            System.out.println("OK: " + r.getId() + " con recursos " + r.getIdsRecursos());
        } catch (RuntimeException e) {
            System.out.println("Fallo inesperado: " + e.getMessage());
        }

        System.out.println();
        System.out.println("--- Reserva que deberia fallar por disponibilidad ---");
        try {
            logica.crearReserva("111", "Reunion", "2026-08-05",
                    "09:30", "10:30", List.of("CAT-000002"));
            System.out.println("ERROR: no debio permitirlo");
        } catch (CategoriasNoDisponiblesException e) {
            System.out.println("Correcto -> " + e.getMessage());
            System.out.println("Categorias que fallaron: " + e.getCategorias());
        }

        System.out.println();
        System.out.println("--- Validaciones ---");
        probarValidacion(logica, "111", "Reunion", "2026-08-20", "16:00", "14:00", List.of("CAT-000002"));
        probarValidacion(logica, "111", "",        "2026-08-20", "09:00", "11:00", List.of("CAT-000002"));
        probarValidacion(logica, "111", "Reunion", "2026-08-20", "09:00", "11:00", List.of());

        System.out.println();
        System.out.println("--- Cancelar libera el recurso ---");
        ReservaDTO nueva = logica.crearReserva("111", "Temporal", "2026-08-20",
                "09:00", "11:00", List.of("CAT-000002"));
        System.out.println("Antes de cancelar -> " + logica.estaDisponible(
                nueva.getIdsRecursos().get(0), "2026-08-20", "09:00", "11:00"));
        logica.cancelarReserva(nueva.getId());
        System.out.println("Despues de cancelar -> " + logica.estaDisponible(
                nueva.getIdsRecursos().get(0), "2026-08-20", "09:00", "11:00"));
    }

    private static void probarValidacion(ReservaLogica logica, String id, String actividad,
                                         String fecha, String ini, String fin, List<String> cats) {
        try {
            logica.crearReserva(id, actividad, fecha, ini, fin, cats);
            System.out.println("ERROR: no debio permitirlo");
        } catch (IllegalArgumentException e) {
            System.out.println("Correcto -> " + e.getMessage());
        }
    }

    private static void prepararDatos() {
        CategoriaDatos categorias = new CategoriaDatos();
        if (categorias.buscarPorId("CAT-000002") == null) {
            categorias.agregar(new CategoriaDTO("CAT-000002", "Laptop windows 11"));
        }
        RecursoDatos recursos = new RecursoDatos();
        if (recursos.buscarPorId("238715") == null) {
            recursos.agregar(new RecursoDTO("238715", "Laptop #238715", "CAT-000002"));
        }
    }
}