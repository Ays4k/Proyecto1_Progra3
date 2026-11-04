# Sistema de Reserva de Recursos — Proyecto #2, Programación III (EIF206)

Universidad Nacional, Costa Rica — 2026 - II Ciclo

## Integrantes
- CESAR SEGURA GALEANO — 208830522
- SAUL SERRANO BRENES — 703320671
- ISAAC BRENES TORRENTES — 119640551

## Arquitectura

El sistema corre como **dos programas separados** que se comunican por la red: el **backend** (un servidor sin interfaz) y el **frontend** (la aplicación JavaFX). Puede haber varios frontends conectados al mismo backend.

**Qué pasa cuando el usuario hace una acción** (por ejemplo, guardar un recurso):
1. El **controlador** de la pantalla llama a un método del **proxy** correspondiente (`RecursoProxy`), que tiene los mismos métodos que el Servicio del backend.
2. El proxy arma una `Peticion` con el nombre del método y sus parámetros, y la entrega a `Conexion`.
3. `Conexion` la envía por el **socket** al backend, en el puerto 6767, y espera la respuesta.
4. En el backend, el **servidor** atiende a ese frontend en su propio hilo, ejecuta el **Servicio** real (que pasa por la Lógica y los Datos hasta MySQL) y devuelve una `Respuesta` con el resultado o con el mensaje de error.
5. El proxy recibe la `Respuesta`. Si trae un error, lo lanza como `ServidorException`, y el controlador muestra el mensaje en pantalla.

**Qué pasa cuando alguien crea una reserva:** además de responderle a quien la creó, el backend envía una `AlertaReservaDTO` por un segundo socket (puerto 7676) a **todos los demás** frontends conectados, que la muestran en su panel de alertas.

El proyecto tiene tres módulos de Maven:
- **`backend`**: capas Servicio, Lógica y Datos. Es el **único** que se conecta a MySQL. Atiende a cada frontend en su propio hilo y, cuando alguien crea una reserva, avisa a los demás frontends conectados.
- **`frontend`**: capa de presentación (JavaFX, Modelo-Vista-Controlador). No tiene ninguna referencia a la base de datos ni a las clases del backend.
- **`shared`**: lo que conocen los dos lados: los DTO, `Peticion`, `Respuesta`, `Protocolo` (puertos y nombres de métodos) y `AlertaReservaDTO`.

Términos usados:
- **Socket**: canal de comunicación entre dos programas por la red. Cada lado escribe y lee objetos por él.
- **Puerto**: número que identifica en qué canal escucha el servidor dentro de su computadora. El backend usa el **6767** para peticiones y el **7676** para notificaciones (definidos en `Protocolo`).
- **Proxy** (patrón Proxy): clase del frontend que tiene los mismos métodos que un Servicio del backend (por ejemplo `RecursoProxy` y `RecursoServicio`). En vez de hacer el trabajo, arma una `Peticion`, la manda por el socket y devuelve lo que responde el Servicio real.
- **Hilo** (thread): línea de ejecución dentro de un programa. El servidor usa uno por cliente, y el frontend usa uno aparte para escuchar las alertas sin congelar la pantalla.

## Requisitos
- **JDK 26** (el proyecto se compila con la versión 26 de Java).
- **Maven 3**.
- **MySQL 8.0.16 o superior**.
- Se recomienda **IntelliJ IDEA** para ejecutar el backend y el frontend.

## 1. Crear la base de datos

El script `sql/schema_srr.sql` crea la base `srr`, sus tablas, el usuario de MySQL que usa la aplicación (`admin` / `admin`) y los datos de prueba.

Con un usuario administrador de MySQL (por ejemplo `root`), ejecutarlo una sola vez:

- **Desde la consola:**
  ```bash
  mysql -u root -p < sql/schema_srr.sql
  ```
- **Desde MySQL Workbench:** *File → Open SQL Script* → `sql/schema_srr.sql` → ejecutar todo con el botón del rayo.

Para **recrear la base desde cero** (borra todos los datos), quitar el `--` de la línea `-- DROP DATABASE IF EXISTS srr;` al principio del script antes de ejecutarlo.

### Base de datos de pruebas (solo para las pruebas de integración del backend)

Las pruebas de integración del backend usan una base aparte, `srr_test`, para no tocar los datos reales. Se crea una sola vez con:
```bash
mysql -u root -p < sql/schema_srr_test.sql
```
Es el mismo script de arriba con el nombre de la base cambiado. Las pruebas leen su conexión de `backend/src/test/resources/conexion.properties`, que apunta a `srr_test`.

## 2. Configuración

### Backend
- Escucha en los puertos **6767** (peticiones) y **7676** (notificaciones).
- Se conecta a MySQL con los datos de `backend/src/main/resources/conexion.properties`:

| Clave | Para qué sirve | Valor por defecto |
|---|---|---|
| `db.url` | Dirección JDBC de la base | `jdbc:mysql://localhost:3306/srr?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `db.user` | Usuario de MySQL | `admin` |
| `db.pass` | Clave del usuario de MySQL | `admin` |

  El usuario `admin` lo crea `sql/schema_srr.sql`. Cada valor se puede cambiar sin editar el archivo con una propiedad del sistema al ejecutar el backend: `-Dsrr.db.url=...`, `-Dsrr.db.user=...` o `-Dsrr.db.pass=...` (en IntelliJ: *Edit Configurations → Servidor → VM options*). Si el archivo no existe, se usan los valores por defecto.
- Al crear una reserva, el backend avisa a todos los frontends conectados al puerto 7676 **menos al que la creó**. Cada frontend se identifica al conectarse enviando su ID de usuario como primer mensaje; si no lo envía en 5 segundos, el backend cierra esa conexión.

### Frontend
La dirección del backend y los tiempos de espera están en `frontend/src/main/resources/config.properties`:

| Clave | Para qué sirve | Valor por defecto |
|---|---|---|
| `servidor.host` | Dirección de la computadora donde corre el backend | `localhost` |
| `servidor.tiempoEspera` | Milisegundos que se espera una respuesta normal | `10000` |
| `servidor.tiempoEsperaIa` | Milisegundos que se espera la respuesta de la IA | `60000` |

Si existe un archivo `config.properties` en la carpeta desde donde se ejecuta el frontend, se usa ese en lugar del incluido en el proyecto. Así se puede cambiar la dirección **sin recompilar**. Si el archivo no se puede leer, la aplicación lo avisa al abrir y usa los valores por defecto.

### Backend y frontend en computadoras distintas
1. En la computadora del backend, averiguar su IP en la red (por ejemplo con `ipconfig` en Windows).
2. En la computadora del frontend, poner esa IP en `servidor.host`.
3. Revisar que el firewall de la computadora del backend permita las conexiones entrantes a los puertos **6767** y **7676**.

### Reservas con IA (opcional)
La creación de reservas por lenguaje natural usa la API de Gemini (modelo `gemini-3.5-flash-lite`). Para habilitarla, definir la variable de entorno `GEMINI_API_KEY` con una API key válida **en el proceso del backend**. En IntelliJ: *Edit Configurations → Servidor → Environment variables*.

**No es obligatoria.** Sin la variable, el resto del sistema funciona normalmente y solo esa función responde con un mensaje de que no está disponible. La API key **no** se guarda en ningún archivo del proyecto.

## 3. Ejecutar

El orden importa: primero MySQL, después el backend y por último uno o varios frontends.

1. Tener **MySQL** encendido, con la base creada (paso 1).
2. Ejecutar el **backend**: la clase `SRR.Servidor.Servidor`, del módulo `backend`. En la consola aparece que el servidor está escuchando en el puerto 6767 y en el de notificaciones.
3. Ejecutar el **frontend**: la clase `SRR.Launch`, del módulo `frontend`. Se puede abrir **más de una** a la vez (en IntelliJ: *Edit Configurations → Launch → Modify options → Allow multiple instances*).

Alternativa con Maven para el frontend, desde la raíz del proyecto:
```bash
mvn install -DskipTests
mvn -pl frontend javafx:run
```

Si el frontend se abre con el backend apagado, el login muestra que no hay conexión y vuelve a intentar cada 3 segundos. Cuando el backend se enciende, el aviso desaparece solo.

## Usuarios de prueba

Estos son los usuarios que carga `sql/schema_srr.sql`. El usuario se escribe en el campo **ID** del login.

| Usuario | Clave | Nombre | Rol |
|---|---|---|---|
| `admin` | `admin` | Admin | ADMINISTRADOR |
| `user` | `user` | User | FUNCIONARIO |
| `10101010` | `maria123` | Maria | ADMINISTRADOR |
| `20202020` | `carlos123` | Carlos | ADMINISTRADOR |
| `25252525` | `gabriela123` | Gabriela | ADMINISTRADOR |
| `30303030` | `ana123` | Ana | FUNCIONARIO |
| `40404040` | `luis123` | Luis | FUNCIONARIO |
| `50505050` | `sofia123` | Sofia | FUNCIONARIO |
| `60606060` | `diego123` | Diego | FUNCIONARIO |
| `70707070` | `valeria123` | Valeria | FUNCIONARIO |
| `80808080` | `pablo123` | Pablo | FUNCIONARIO |
| `90909090` | `andres123` | Andres | FUNCIONARIO |
| `11111122` | `camila123` | Camila | FUNCIONARIO |
| `22222233` | `jose123` | Jose | FUNCIONARIO |
| `33333344` | `natalia123` | Natalia | FUNCIONARIO |
| `44444455` | `esteban123` | Esteban | FUNCIONARIO |

Si se agregan o cambian funcionarios desde la pestaña Funcionarios, esos cambios quedan en MySQL y esta tabla ya no los refleja. Para volver a estos datos se recrea la base (paso 1).

## Alertas

A la derecha de la ventana principal, en todas las pestañas, está el área de alertas. Cuando **otro** usuario conectado crea una reserva, aparece una alerta. El que la creó no la recibe.
- **Ver**: abre una ventana con el encargado, la fecha, el horario y los recursos.
- **Descartar** (X): pide confirmación y quita la alerta solo de la lista local, sin avisar al backend.
- Si el backend se reinicia, el frontend se reconecta solo al servidor de notificaciones.

## Pruebas

Pruebas con **JUnit Jupiter**:
- **De unidad** (clases `*Test`, se ejecutan con el plugin **Surefire**): prueban una parte del sistema aislada, sin depender del backend real ni de la base de datos.
- **De integración** (clases `*IT`, se ejecutan con el plugin **Failsafe**): prueban que las piezas reales funcionen juntas (frontend, servidor y base de datos).

| Comando | Qué ejecuta |
|---|---|
| `mvn test` | Todas las pruebas de unidad |
| `mvn verify` | Las de unidad y además las de integración |

Pruebas incluidas:

| Clase | Tipo | Qué prueba |
|---|---|---|
| `ProxyTest` | Unidad | Proxies y `Conexion` contra un servidor simulado (`ServidorFalso`): respuesta correcta, error del backend, tiempo agotado, servidor apagado, reintento de consultas y aviso de IA ocupada |
| `ListaAlertasTest` | Unidad | Agregar, ver y descartar alertas, sin interfaz |
| `ClienteNotificacionesTest` | Unidad | Recepción de alertas y reconexión contra un servidor de alertas simulado |
| `EnrutadorBaseTest` | Unidad | El enrutador base del backend: registrar operaciones, atenderlas y propagar errores |
| `EnrutadorPrincipalTest` | Unidad | Que cada operación de `Protocolo` tenga enrutador y que una desconocida devuelva error |
| `AtencionClienteTest` | Unidad | Atención de un cliente por socket: respuesta, errores convertidos en `Respuesta`, varias peticiones en la misma conexión y desconexión |
| `ServidorNotificacionesTest` | Unidad | Registro de clientes de alertas, que la alerta llegue a todos menos al creador, saludos inválidos y clientes caídos |
| `SerializacionTest` | Unidad | `Peticion`, `Respuesta` y los DTO viajando por serialización |
| `ReservaLogicaValidacionTest`, `UsuarioLogicaValidacionTest`, `RecursoLogicaValidacionTest`, `LoginLogicaTest`, `NotificacionLogicaTest` | Unidad | Validaciones de la capa Lógica que no necesitan base de datos |
| `UsuarioDatosIT`, `CategoriaDatosIT`, `RecursoDatosIT`, `ReservaDatosIT` | Integración | Capa Datos contra `srr_test`: consultas, altas, cambios, bajas y restricciones de la base |
| `LoginLogicaIT`, `UsuarioLogicaIT`, `CategoriaYRecursoLogicaIT`, `ReservaLogicaIT` | Integración | Reglas de la capa Lógica con la base real: disponibilidad, cancelaciones, reservas sin cupo y alertas |
| `ServidorIT` | Integración | El servidor del backend por socket (peticiones y alertas) contra `srr_test`, con varios clientes |
| `ProxyIT` | Integración | Login y creación de reservas contra el servidor real |

**Para las pruebas de integración del frontend (`ProxyIT`)**, MySQL (con la base `srr` y sus datos de prueba) y el `Servidor` tienen que estar encendidos antes de ejecutar `mvn verify`. Si el `Servidor` no responde, esas pruebas **se saltan** en lugar de fallar. Usan el usuario `user` / `user`, así que esa clave no debe estar cambiada. La reserva que crean se cancela al terminar (queda registrada como cancelada).

**Para las pruebas de integración del backend**, solo hace falta MySQL con la base `srr_test` creada (ver el paso 1); **no** necesitan el `Servidor` encendido, porque cada prueba arma el suyo. Cada prueba crea sus propios usuarios, categorías y recursos y los borra al terminar. Antes de empezar comprueban que la conexión apunte a `srr_test` y, si apunta a otra base, fallan sin tocar nada. Si no se puede conectar a `srr_test`, se **saltan** en lugar de fallar.

Para ejecutar solo las del backend, desde la raíz del proyecto: `mvn test -pl backend -am` (unidad) o `mvn verify -pl backend -am` (unidad e integración).

`ServidorFalso` (en las fuentes de prueba del frontend) también se puede ejecutar a mano con su `main`, para probar el frontend sin backend ni MySQL.

## Cosas a tomar en cuenta

- **Las contraseñas se guardan en texto plano** en la base. Es una simplificación, sin hashing, no un error.
- **El rol determina qué pestañas se ven.** Un `FUNCIONARIO` no ve Funcionarios, Categorías ni Recursos, y un `ADMINISTRADOR` no ve Reservas.
- **Las validaciones están en la capa Lógica del backend.** Por ejemplo, no se puede eliminar una categoría con recursos asociados, ni un recurso asignado a una reserva activa, ni cancelar una reserva que ya pasó. El frontend muestra el mensaje que devuelve el backend.
- **Errores de conexión:** si el backend se apaga, cada pantalla lo informa al intentar una acción. Si se reinicia con la aplicación abierta, las consultas (cargar, buscar, filtrar) se recuperan solas. Las acciones que modifican datos (guardar, eliminar, reservar) **no se reintentan** automáticamente, para no hacerlas dos veces: la primera muestra el aviso de conexión perdida y la siguiente ya funciona.
- **Una petición a la vez por frontend:** todas las peticiones de un frontend van por la misma conexión. Mientras la IA está respondiendo, cualquier otra acción que pida datos muestra un aviso en lugar de quedarse esperando.
