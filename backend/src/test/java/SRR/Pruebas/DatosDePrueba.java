package SRR.Pruebas;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaDTO;
import SRR.DTO.UsuarioDTO;
import SRR.Datos.CategoriaDatos;
import SRR.Datos.ConexionBD;
import SRR.Datos.RecursoDatos;
import SRR.Datos.ReservaDatos;
import SRR.Datos.UsuarioDatos;
import org.junit.jupiter.api.Assumptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DatosDePrueba {

    public static final String CLAVE = "Clave123";
    public static final String BASE_DE_PRUEBA = "srr_test";

    private final UsuarioDatos usuarios = new UsuarioDatos();
    private final CategoriaDatos categorias = new CategoriaDatos();
    private final RecursoDatos recursos = new RecursoDatos();
    private final ReservaDatos reservas = new ReservaDatos();

    private final List<String> idsUsuarios = new ArrayList<>();
    private final List<String> idsCategorias = new ArrayList<>();
    private final List<String> idsRecursos = new ArrayList<>();

    public static void verificarBaseDePrueba() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            String base = conexion.getCatalog();
            if (!BASE_DE_PRUEBA.equals(base)) {
                throw new IllegalStateException("Las pruebas de integración solo pueden correr contra "
                        + BASE_DE_PRUEBA + " y la conexión apunta a: " + base);
            }
        } catch (SQLException e) {
            Assumptions.assumeTrue(false, "No se pudo conectar a " + BASE_DE_PRUEBA
                    + ", se omiten las pruebas de integración: " + e.getMessage());
        }
    }

    public static String cedulaNueva() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(900_000_000L, 999_999_999L));
    }

    public static String textoUnico() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L));
    }

    public static String fechaFutura() {
        return LocalDate.now().plusDays(90).toString();
    }

    public UsuarioDTO usuario(String rol) {
        String id = cedulaNueva();
        usuarios.agregar(new UsuarioDTO(id, "Prueba " + id, "8888-0000", CLAVE, rol));
        idsUsuarios.add(id);
        return usuarios.buscarPorId(id);
    }

    public CategoriaDTO categoria() {
        CategoriaDTO categoria = new CategoriaDTO(null, "Cat prueba " + textoUnico());
        categorias.agregar(categoria);
        idsCategorias.add(categoria.getId());
        return categoria;
    }

    public RecursoDTO recurso(CategoriaDTO categoria) {
        RecursoDTO recurso = new RecursoDTO(null, "Rec prueba " + textoUnico(), categoria.getId());
        recursos.agregar(recurso);
        idsRecursos.add(recurso.getId());
        return recurso;
    }

    public ReservaDTO reserva(String idFuncionario, List<String> idsDeRecursos, String fecha,
                              String horaInicio, String horaFin, String estado) {
        ReservaDTO reserva = new ReservaDTO(null, idFuncionario, new ArrayList<>(idsDeRecursos),
                "Actividad prueba", fecha, horaInicio, horaFin, estado);
        reservas.agregar(reserva);
        return reserva;
    }

    public void registrarUsuario(String id) {
        idsUsuarios.add(id);
    }

    public void registrarCategoria(String id) {
        idsCategorias.add(id);
    }

    public void registrarRecurso(String id) {
        idsRecursos.add(id);
    }

    public void limpiar() {
        for (String idUsuario : idsUsuarios) {
            intentar(() -> {
                for (ReservaDTO reserva : reservas.listarPorFuncionario(idUsuario)) {
                    intentar(() -> reservas.borrar(reserva.getId()));
                }
            });
        }
        for (String id : idsRecursos) {
            intentar(() -> recursos.borrar(id));
        }
        for (String id : idsCategorias) {
            intentar(() -> categorias.borrar(id));
        }
        for (String id : idsUsuarios) {
            intentar(() -> usuarios.borrar(id));
        }
    }

    private static void intentar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException ignorada) {
        }
    }
}
