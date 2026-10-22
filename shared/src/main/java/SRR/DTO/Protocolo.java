package SRR.DTO;

/**
 * Contrato entre frontend y backend: puertos y nombres de métodos.
 * Para cada método se documenta el ORDEN de los parámetros y lo que devuelve.
 * Backend (dispatcher) y frontend (proxies) deben respetarlo exactamente.
 */
public final class Protocolo {

    private Protocolo() {
    }

    // ---------- Puertos ----------
    public static final String HOST_POR_DEFECTO = "localhost";
    public static final int PUERTO_PETICIONES = 5000;
    public static final int PUERTO_NOTIFICACIONES = 5001;

    // ---------- Login ----------
    /** params: (LoginDTO login) -> UsuarioDTO */
    public static final String INICIAR_SESION = "iniciarSesion";
    /** params: (String id, String actual, String nueva, String confirmacion) -> void */
    public static final String CAMBIAR_CLAVE = "cambiarClave";

    // ---------- Usuarios ----------
    /** params: () -> List<UsuarioDTO> */
    public static final String OBTENER_USUARIOS = "obtenerUsuarios";
    /** params: (String id) -> UsuarioDTO */
    public static final String BUSCAR_USUARIO_POR_ID = "buscarUsuarioPorId";
    /** params: (String id) -> String */
    public static final String NOMBRE_DE_USUARIO = "nombreDeUsuario";
    /** params: (String texto) -> List<UsuarioDTO> */
    public static final String BUSCAR_USUARIO_POR_NOMBRE = "buscarUsuarioPorNombre";
    /** params: (UsuarioDTO usuario) -> Integer */
    public static final String CAMBIOS_USUARIO = "cambiosUsuario";
    /** params: (String id) -> Boolean */
    public static final String ELIMINAR_USUARIO = "eliminarUsuario";

    // ---------- Categorías ----------
    /** params: () -> List<CategoriaDTO> */
    public static final String OBTENER_CATEGORIAS = "obtenerCategorias";
    /** params: (CategoriaDTO categoria) -> Integer */
    public static final String GUARDAR_CATEGORIA = "guardarCategoria";
    /** params: (String id) -> void */
    public static final String ELIMINAR_CATEGORIA = "eliminarCategoria";
    /** params: (String id) -> CategoriaDTO */
    public static final String BUSCAR_CATEGORIA_POR_ID = "buscarCategoriaPorId";

    // ---------- Recursos ----------
    /** params: () -> List<RecursoDTO> */
    public static final String OBTENER_RECURSOS = "obtenerRecursos";
    /** params: (RecursoDTO recurso) -> Integer */
    public static final String GUARDAR_RECURSO = "guardarRecurso";
    /** params: (String id) -> void */
    public static final String ELIMINAR_RECURSO = "eliminarRecurso";
    /** params: (String id) -> RecursoDTO */
    public static final String BUSCAR_RECURSO_POR_ID = "buscarRecursoPorId";
    /** params: (String descripcion) -> List<RecursoDTO> */
    public static final String BUSCAR_RECURSO_POR_DESCRIPCION = "buscarRecursoPorDescripcion";
    /** params: (String idCategoria) -> List<RecursoDTO> */
    public static final String OBTENER_RECURSOS_POR_CATEGORIA = "obtenerRecursosPorCategoria";
    /** params: (String idCategoria, String descripcion) -> List<RecursoDTO> */
    public static final String FILTRAR_RECURSOS = "filtrarRecursos";

    // ---------- Reservas ----------
    /** params: (String idFuncionario, String actividad, String fecha, String horaInicio,
     *           String horaFin, List<String> idsCategorias) -> ReservaDTO */
    public static final String CREAR_RESERVA = "crearReserva";
    /** params: (String idReserva) -> void */
    public static final String CANCELAR_RESERVA = "cancelarReserva";
    /** params: (String prompt) -> ReservaAiDTO */
    public static final String GENERAR_RESERVA_AI = "generarReservaAi";
    /** params: (String idFuncionario) -> List<ReservaDTO> */
    public static final String RESERVAS_ACTIVAS_DE = "reservasActivasDe";
    /** params: (String idRecurso) -> Boolean */
    public static final String RECURSO_DISPONIBLE = "recursoDisponible";
    /** params: (String idRecurso, String fecha, String horaInicio, String horaFin) -> Boolean */
    public static final String RECURSO_DISPONIBLE_EN_HORARIO = "recursoDisponibleEnHorario";
    /** params: (String idCategoria, String fecha, String horaInicio, String horaFin) -> List<RecursoDTO> */
    public static final String RECURSOS_DISPONIBLES = "recursosDisponibles";
    /** params: () -> List<ReservaDTO> */
    public static final String OBTENER_TODAS_LAS_RESERVAS = "obtenerTodasLasReservas";
}
