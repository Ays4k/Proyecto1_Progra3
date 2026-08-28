package SRR;

import SRR.DTO.*;
import SRR.Datos.*;

import java.util.List;

public class PruebaDatos {

    public static void main(String[] args) {
        probarCategorias();
        probarUsuarios();
        probarRecursos();
        probarReservas();
    }

    static void probarCategorias() {
        CategoriaDatos datos = new CategoriaDatos();
        if (datos.listar().isEmpty()) {
            datos.agregar(new CategoriaDTO("CAT-000001", "Sala para 10 personas"));
            datos.agregar(new CategoriaDTO("CAT-000002", "Laptop windows 11"));
        }
        System.out.println("Categorias: " + new CategoriaDatos().listar().size());
    }

    static void probarUsuarios() {
        UsuarioDatos datos = new UsuarioDatos();
        if (datos.listar().isEmpty()) {
            datos.agregar(new UsuarioDTO("111", "Juan Perez", "3323", "111", "FUNCIONARIO"));
            datos.agregar(new UsuarioDTO("admin", "Administrador", "", "admin", "ADMINISTRADOR"));
        }
        System.out.println("Usuarios: " + new UsuarioDatos().listar().size());
    }

    static void probarRecursos() {
        RecursoDatos datos = new RecursoDatos();
        if (datos.listar().isEmpty()) {
            datos.agregar(new RecursoDTO("238715", "Laptop #238715", "CAT-000002"));
            datos.agregar(new RecursoDTO("34343", "Sala 1 primer piso", "CAT-000001"));
        }
        System.out.println("Recursos: " + new RecursoDatos().listar().size());
        System.out.println("De categoria CAT-000002: "
                + new RecursoDatos().buscarPorCategoria("CAT-000002").size());
    }

    static void probarReservas() {
        ReservaDatos datos = new ReservaDatos();
        if (datos.listar().isEmpty()) {
            datos.agregar(new ReservaDTO("RES-000001", "111",
                    List.of("238715", "34343"),
                    "Sesion de Junta Directiva",
                    "2026-08-05", "09:00", "11:00", "ACTIVA"));
        }
        System.out.println("Reservas: " + new ReservaDatos().listar().size());
        System.out.println("Por funcionario 111: "
                + new ReservaDatos().buscarPorFuncionario("111").size());
    }
}