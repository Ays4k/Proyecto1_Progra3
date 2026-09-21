# Sistema de Reserva de Recursos — Proyecto #1, Programación III (EIF206)

Universidad Nacional, Costa Rica — 2026 - II Ciclo

## Integrantes
- CESAR SEGURA GALEANO — 208830522
- SAUL SERRANO BRENES — 703320671
- ISAAC BRENES TORRENTES — 119640551

## Ejecutar

Requiere JDK 17+ y Maven.

```bash
mvn javafx:run
```

## Función de Inteligencia Artificial (opcional)

La creación de reservas por lenguaje natural usa la API de Gemini. Para habilitarla, definí
la variable de entorno `GEMINI_API_KEY` con una API key válida antes de correr la app.

**No es obligatoria.** Si no está configurada, el resto del sistema funciona con total
normalidad — la aplicación lo detecta al iniciar y desactiva solo esa función en particular,
mostrando un mensaje si se intenta usar. No hace falta configurar nada para probar el resto
de las funcionalidades.

## Datos y persistencia

Los datos (usuarios, categorías, recursos, reservas) se guardan en archivos JSON dentro de la
carpeta `datos/`, en la raíz del proyecto (fuera de `src`). Si algún archivo no existe al
arrancar, se crea automáticamente vacío.

Usuarios de prueba ya cargados en `datos/usuarios.json`:
- `admin` / `admin` — rol ADMINISTRADOR
- `user` / `user` — rol FUNCIONARIO

## Cosas a tomar en cuenta

- **Las contraseñas se guardan en texto plano** en `usuarios.json`. Es una simplificación, sin hashing (aún no lo vemos a fondo), no un error.
- **El rol determina qué pestañas se ven**, no hay una pantalla de permisos aparte: un
  usuario `FUNCIONARIO` no ve las pestañas de Funcionarios/Categorías/Recursos, y un
  `ADMINISTRADOR` no ve la de Reservas. Esto se controla en `SistemaReservasController`.
- **No se puede eliminar una categoría con recursos asociados**, ni un recurso que esté
  asignado a una reserva activa — ambas validaciones están en la capa `Logica`, no en el
  Controlador, y lanzan una excepción propia (`CategoriaEnUsoException`, `RecursoException`)
  que el Controlador atrapa para mostrar el mensaje.
- **Cancelar una reserva ya vencida no está permitido** — si se prueba con datos de ejemplo
  de fechas pasadas, es esperado que lo rechace.

## Pruebas

```bash
mvn test      # pruebas unitarias (Surefire)
mvn verify    # incluye además las pruebas de integración (Failsafe)
```

La prueba de integración de la IA (`GeminiServiceIT`) se salta automáticamente si no hay
`GEMINI_API_KEY` configurada — no hace que la ejecución falle.
