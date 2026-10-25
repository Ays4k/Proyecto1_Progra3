package SRR.Comunicacion;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.LoginDTO;
import SRR.DTO.Peticion;
import SRR.DTO.Protocolo;
import SRR.DTO.RecursoDTO;
import SRR.DTO.ReservaAiDTO;
import SRR.DTO.ReservaDTO;
import SRR.DTO.Respuesta;
import SRR.DTO.UsuarioDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Servidor de mentira para probar el frontend sin el backend real.
// Responde los mismos metodos de Protocolo con datos guardados en memoria
// (no usa MySQL) e imita las validaciones y mensajes de error del backend.
// Los cambios (crear, modificar, eliminar) se pierden al cerrarlo.
//
// Uso manual: correr el main y despues abrir el frontend normalmente.
// Uso en pruebas: new ServidorFalso(puerto).iniciar() y al final detener().
public class ServidorFalso {

    private final int puerto;
    private ServerSocket servidor;
    private volatile int retrasoMs = 0;   // para simular un backend lento en las pruebas

    private final List<UsuarioDTO> usuarios = new ArrayList<>();
    private final List<CategoriaDTO> categorias = new ArrayList<>();
    private final List<RecursoDTO> recursos = new ArrayList<>();
    private final List<ReservaDTO> reservas = new ArrayList<>();
    private int siguienteId = 100;   // ids para lo que se cree mientras corre

    public static void main(String[] args) throws IOException {
        ServidorFalso falso = new ServidorFalso(Protocolo.PUERTO_PETICIONES);
        falso.iniciar();
        System.out.println("ServidorFalso escuchando en el puerto " + Protocolo.PUERTO_PETICIONES
                + ". Usuarios: admin/admin (ADMINISTRADOR), user/user (FUNCIONARIO).");
    }

    public ServidorFalso(int puerto) {
        this.puerto = puerto;
        cargarDatos();
    }

    // Abre el puerto y atiende a cada cliente en su propio hilo
    public void iniciar() throws IOException {
        servidor = new ServerSocket(puerto);
        Thread aceptador = new Thread(() -> {
            while (!servidor.isClosed()) {
                try {
                    Socket cliente = servidor.accept();
                    new Thread(() -> atender(cliente)).start();
                } catch (IOException e) {
                    // el servidor se cerro con detener()
                }
            }
        });
        aceptador.setDaemon(true);
        aceptador.start();
    }

    public void detener() {
        try {
            if (servidor != null) {
                servidor.close();
            }
        } catch (IOException e) {
            // ya estaba cerrado
        }
    }

    public void setRetrasoMs(int retrasoMs) {
        this.retrasoMs = retrasoMs;
    }

    private void atender(Socket cliente) {
        try (cliente) {
            // mismo orden que Conexion: primero el de salida y flush
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

            while (true) {
                Peticion peticion = (Peticion) entrada.readObject();
                Respuesta respuesta = procesar(peticion);
                if (retrasoMs > 0) {
                    Thread.sleep(retrasoMs);
                }
                salida.writeObject(respuesta);
                salida.flush();
                salida.reset();
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            // el cliente se desconecto
        }
    }

    // Siempre devuelve una Respuesta, igual que el backend real: si algo falla, va el mensaje
    private synchronized Respuesta procesar(Peticion peticion) {
        try {
            return Respuesta.ok(ejecutar(peticion.getMetodo(), peticion.getParametros()));
        } catch (RuntimeException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Object ejecutar(String metodo, Object[] p) {
        switch (metodo) {
            // ---------- Login ----------
            case Protocolo.INICIAR_SESION:
                return iniciarSesion((LoginDTO) p[0]);
            case Protocolo.CAMBIAR_CLAVE:
                cambiarClave((String) p[0], (String) p[1], (String) p[2], (String) p[3]);
                return null;

            // ---------- Usuarios ----------
            case Protocolo.OBTENER_USUARIOS:
                return new ArrayList<>(usuarios);
            case Protocolo.BUSCAR_USUARIO_POR_ID:
                return buscarUsuario((String) p[0]);
            case Protocolo.NOMBRE_DE_USUARIO: {
                UsuarioDTO u = buscarUsuario((String) p[0]);
                return u == null ? null : u.getNombre();
            }
            case Protocolo.BUSCAR_USUARIO_POR_NOMBRE: {
                List<UsuarioDTO> lista = new ArrayList<>();
                for (UsuarioDTO u : usuarios) {
                    if (contiene(u.getNombre(), (String) p[0])) {
                        lista.add(u);
                    }
                }
                return lista;
            }
            case Protocolo.CAMBIOS_USUARIO:
                return cambiosUsuario((UsuarioDTO) p[0]);
            case Protocolo.ELIMINAR_USUARIO:
                return eliminarUsuario((String) p[0]);

            // ---------- Categorias ----------
            case Protocolo.OBTENER_CATEGORIAS:
                return new ArrayList<>(categorias);
            case Protocolo.GUARDAR_CATEGORIA:
                return guardarCategoria((CategoriaDTO) p[0]);
            case Protocolo.ELIMINAR_CATEGORIA:
                eliminarCategoria((String) p[0]);
                return null;
            case Protocolo.BUSCAR_CATEGORIA_POR_ID:
                return buscarCategoria((String) p[0]);

            // ---------- Recursos ----------
            case Protocolo.OBTENER_RECURSOS:
                return new ArrayList<>(recursos);
            case Protocolo.GUARDAR_RECURSO:
                return guardarRecurso((RecursoDTO) p[0]);
            case Protocolo.ELIMINAR_RECURSO:
                eliminarRecurso((String) p[0]);
                return null;
            case Protocolo.BUSCAR_RECURSO_POR_ID:
                return buscarRecurso((String) p[0]);
            case Protocolo.BUSCAR_RECURSO_POR_DESCRIPCION:
                return filtrarRecursos(null, (String) p[0]);
            case Protocolo.OBTENER_RECURSOS_POR_CATEGORIA:
                return filtrarRecursos((String) p[0], null);
            case Protocolo.FILTRAR_RECURSOS:
                return filtrarRecursos((String) p[0], (String) p[1]);

            // ---------- Reservas ----------
            case Protocolo.CREAR_RESERVA:
                return crearReserva((String) p[0], (String) p[1], (String) p[2],
                        (String) p[3], (String) p[4], (List<String>) p[5]);
            case Protocolo.CANCELAR_RESERVA:
                cancelarReserva((String) p[0]);
                return null;
            case Protocolo.GENERAR_RESERVA_AI:
                // respuesta fija: no llama a Gemini
                return new ReservaAiDTO("Reunión de prueba (IA)", LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), List.of("Laptop"));
            case Protocolo.RESERVAS_ACTIVAS_DE: {
                List<ReservaDTO> lista = new ArrayList<>();
                for (ReservaDTO r : reservas) {
                    if (r.getIdFuncionario().equals(p[0]) && "ACTIVA".equals(r.getEstado())) {
                        lista.add(r);
                    }
                }
                return lista;
            }
            case Protocolo.RECURSO_DISPONIBLE:
                return !enReservaActiva((String) p[0]);
            case Protocolo.RECURSO_DISPONIBLE_EN_HORARIO:
                return libreEnHorario((String) p[0], (String) p[1], (String) p[2], (String) p[3]);
            case Protocolo.RECURSOS_DISPONIBLES: {
                List<RecursoDTO> lista = new ArrayList<>();
                for (RecursoDTO r : filtrarRecursos((String) p[0], null)) {
                    if (libreEnHorario(r.getId(), (String) p[1], (String) p[2], (String) p[3])) {
                        lista.add(r);
                    }
                }
                return lista;
            }
            case Protocolo.OBTENER_TODAS_LAS_RESERVAS:
                return new ArrayList<>(reservas);

            default:
                throw new IllegalArgumentException("Método desconocido: " + metodo);
        }
    }

    // ================= Login =================

    private UsuarioDTO iniciarSesion(LoginDTO login) {
        if (login == null || login.getId() == null) {
            return null;
        }
        UsuarioDTO u = buscarUsuario(login.getId());
        if (u == null || !u.getContrasena().equals(login.getContrasena())) {
            return null;   // el LoginController espera null si las credenciales no sirven
        }
        return u;
    }

    private void cambiarClave(String id, String actual, String nueva, String confirmacion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe indicar su ID");
        }
        UsuarioDTO u = buscarUsuario(id);
        if (u == null || !u.getContrasena().equals(actual)) {
            throw new IllegalArgumentException("ID o clave actual incorrectos");
        }
        if (nueva == null || nueva.isBlank()) {
            throw new IllegalArgumentException("La clave nueva no puede estar vacía");
        }
        if (!nueva.equals(confirmacion)) {
            throw new IllegalArgumentException("Las claves nuevas no coinciden");
        }
        if (nueva.equals(actual)) {
            throw new IllegalArgumentException("La clave nueva debe ser distinta de la actual");
        }
        u.setContrasena(nueva);
    }

    // ================= Usuarios =================

    private UsuarioDTO buscarUsuario(String id) {
        for (UsuarioDTO u : usuarios) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    // 1 si se agrega, 2 si se modifica (igual que UsuarioLogica)
    private int cambiosUsuario(UsuarioDTO usuario) {
        UsuarioDTO existente = buscarUsuario(usuario.getId());
        if (existente != null) {
            usuarios.set(usuarios.indexOf(existente), usuario);
            return 2;
        }
        if (usuario.getId() == null || !usuario.getId().matches("\\d{9,12}")) {
            throw new IllegalArgumentException("La cédula debe tener solo números (entre 9 y 12 dígitos).");
        }
        usuarios.add(usuario);
        return 1;
    }

    private boolean eliminarUsuario(String id) {
        if (buscarUsuario(id) == null) {
            return false;
        }
        for (ReservaDTO r : reservas) {
            if (r.getIdFuncionario().equals(id) && "ACTIVA".equals(r.getEstado())) {
                throw new IllegalArgumentException("No se puede eliminar el funcionario: tiene reservas activas.");
            }
        }
        usuarios.remove(buscarUsuario(id));
        return true;
    }

    // ================= Categorias =================

    private CategoriaDTO buscarCategoria(String id) {
        for (CategoriaDTO c : categorias) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private int guardarCategoria(CategoriaDTO categoria) {
        if (categoria.getId() == null || categoria.getId().isBlank()) {
            categoria.setId(String.valueOf(siguienteId++));
            categorias.add(categoria);
            return 1;
        }
        CategoriaDTO existente = buscarCategoria(categoria.getId());
        categorias.set(categorias.indexOf(existente), categoria);
        return 2;
    }

    private void eliminarCategoria(String id) {
        if (!filtrarRecursos(id, null).isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar la categoría: todavía tiene recursos asociados. Reasígnelos primero.");
        }
        categorias.remove(buscarCategoria(id));
    }

    // ================= Recursos =================

    private RecursoDTO buscarRecurso(String id) {
        for (RecursoDTO r : recursos) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    private int guardarRecurso(RecursoDTO recurso) {
        if (recurso.getDescripcion() == null || recurso.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("Debe indicar la descripción del recurso.");
        }
        if (recurso.getId() == null || recurso.getId().isBlank()) {
            recurso.setId(String.valueOf(siguienteId++));
            recursos.add(recurso);
            return 1;
        }
        RecursoDTO existente = buscarRecurso(recurso.getId());
        recursos.set(recursos.indexOf(existente), recurso);
        return 2;
    }

    private void eliminarRecurso(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el recurso que desea eliminar.");
        }
        if (buscarRecurso(id) == null) {
            throw new IllegalArgumentException("No existe el recurso " + id + ".");
        }
        if (enReservaActiva(id)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el recurso porque está asignado a una reserva activa.");
        }
        recursos.remove(buscarRecurso(id));
    }

    // idCategoria o descripcion vacios significan "sin filtro", igual que RecursoDatos.filtrar
    private List<RecursoDTO> filtrarRecursos(String idCategoria, String descripcion) {
        List<RecursoDTO> lista = new ArrayList<>();
        for (RecursoDTO r : recursos) {
            boolean categoriaOk = idCategoria == null || idCategoria.isBlank() || r.getIdCategoria().equals(idCategoria);
            boolean descripcionOk = descripcion == null || descripcion.isBlank() || contiene(r.getDescripcion(), descripcion);
            if (categoriaOk && descripcionOk) {
                lista.add(r);
            }
        }
        return lista;
    }

    // ================= Reservas =================

    private ReservaDTO crearReserva(String idFuncionario, String actividad, String fecha,
                                    String horaInicio, String horaFin, List<String> idsCategorias) {
        if (actividad == null || actividad.isBlank()) {
            throw new IllegalArgumentException("Debe indicar la actividad.");
        }
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoría.");
        }
        List<String> asignados = new ArrayList<>();
        List<String> sinCupo = new ArrayList<>();
        for (String idCategoria : idsCategorias) {
            String libre = null;
            for (RecursoDTO r : filtrarRecursos(idCategoria, null)) {
                if (libreEnHorario(r.getId(), fecha, horaInicio, horaFin)) {
                    libre = r.getId();
                    break;
                }
            }
            if (libre == null) {
                CategoriaDTO c = buscarCategoria(idCategoria);
                sinCupo.add(c == null ? idCategoria : c.getDescripcion());
            } else {
                asignados.add(libre);
            }
        }
        if (!sinCupo.isEmpty()) {
            throw new IllegalArgumentException("No hay disponibilidad en: " + String.join(", ", sinCupo));
        }
        ReservaDTO reserva = new ReservaDTO(String.valueOf(siguienteId++), idFuncionario, asignados,
                actividad, fecha, horaInicio, horaFin, "ACTIVA");
        reservas.add(reserva);
        return reserva;
    }

    private void cancelarReserva(String idReserva) {
        for (ReservaDTO r : reservas) {
            if (r.getId().equals(idReserva)) {
                if (LocalDate.parse(r.getFecha()).isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("No se puede cancelar una reserva que ya pasó.");
                }
                r.setEstado("CANCELADA");
                return;
            }
        }
        throw new IllegalArgumentException("No existe la reserva " + idReserva);
    }

    private boolean enReservaActiva(String idRecurso) {
        for (ReservaDTO r : reservas) {
            if ("ACTIVA".equals(r.getEstado()) && r.getIdsRecursos().contains(idRecurso)) {
                return true;
            }
        }
        return false;
    }

    // Las horas vienen como "HH:mm", asi que compararlas como texto da el orden correcto
    private boolean libreEnHorario(String idRecurso, String fecha, String inicio, String fin) {
        for (ReservaDTO r : reservas) {
            boolean seCruzan = r.getHoraInicio().compareTo(fin) < 0 && inicio.compareTo(r.getHoraFin()) < 0;
            if ("ACTIVA".equals(r.getEstado()) && r.getFecha().equals(fecha)
                    && r.getIdsRecursos().contains(idRecurso) && seCruzan) {
                return false;
            }
        }
        return true;
    }

    private boolean contiene(String texto, String buscado) {
        return texto != null && buscado != null && texto.toLowerCase().contains(buscado.toLowerCase());
    }

    // ================= Datos iniciales =================

    // Mismos usuarios de prueba que sql/schema_srr.sql. Las fechas son relativas a hoy
    // para que Calendarizacion, Actividades y Estadisticas siempre tengan algo que mostrar.
    private void cargarDatos() {
        usuarios.add(new UsuarioDTO("admin", "Admin", "0000-0000", "admin", "ADMINISTRADOR"));
        usuarios.add(new UsuarioDTO("user", "User", "0000-0000", "user", "FUNCIONARIO"));
        usuarios.add(new UsuarioDTO("101010101", "Maria", "8888-1111", "maria123", "FUNCIONARIO"));

        categorias.add(new CategoriaDTO("1", "Sala de reuniones"));
        categorias.add(new CategoriaDTO("2", "Laptop"));
        categorias.add(new CategoriaDTO("3", "Proyector"));
        categorias.add(new CategoriaDTO("4", "Vehículo"));   // sin recursos: se puede eliminar

        recursos.add(new RecursoDTO("1", "Sala de reuniones - Piso 1", "1"));
        recursos.add(new RecursoDTO("2", "Sala de reuniones - Piso 2", "1"));
        recursos.add(new RecursoDTO("3", "Laptop Dell Latitude #1", "2"));
        recursos.add(new RecursoDTO("4", "Laptop Dell Latitude #2", "2"));
        recursos.add(new RecursoDTO("5", "Proyector Epson #1", "3"));   // uno solo: facil de agotar

        String hoy = LocalDate.now().toString();
        String manana = LocalDate.now().plusDays(1).toString();
        String pasado = LocalDate.now().minusDays(3).toString();
        reservas.add(new ReservaDTO("1", "user", new ArrayList<>(List.of("1", "5")),
                "Reunión de equipo", manana, "09:00", "10:00", "ACTIVA"));
        reservas.add(new ReservaDTO("2", "user", new ArrayList<>(List.of("3")),
                "Capacitación", hoy, "13:00", "15:00", "ACTIVA"));
        reservas.add(new ReservaDTO("3", "101010101", new ArrayList<>(List.of("2", "4")),
                "Presentación a clientes", manana, "14:00", "16:00", "ACTIVA"));
        reservas.add(new ReservaDTO("4", "user", new ArrayList<>(List.of("1")),
                "Reunión cancelada", pasado, "08:00", "09:00", "CANCELADA"));
        reservas.add(new ReservaDTO("5", "101010101", new ArrayList<>(List.of("3", "5")),
                "Taller pasado", pasado, "10:00", "12:00", "ACTIVA"));
    }
}