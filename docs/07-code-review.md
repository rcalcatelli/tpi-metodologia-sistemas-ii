# Etapa 7 — Code review profesional

**Alcance:** diff `estado-inicial..etapa-pruebas` (código de producción y tests).
**Checklist aplicado:** [checklist-code-review.md](checklist-code-review.md).
**Método:** lectura del diff completo por paquete, ejecución de `mvn verify`,
revisión de métricas y cobertura, y prueba manual del menú con entradas inválidas.

## Resultado del checklist

| Ítem | Resultado | Observación |
|---|---|---|
| A1 SRP | Cumple | Servicio, validador, reporte, CSV, consola y entidades separados. |
| A2 OCP | Cumple | Un tipo nuevo es una constante en `TipoSolicitud`; el reporte se adapta solo. |
| A3 LSP | Cumple | Repositorios intercambiables; excepciones tratadas por la clase base. |
| A4 ISP | Cumple | Tres interfaces de repositorio de 2 a 5 métodos. |
| A5 DIP | Cumple | Repositorios, validador, `Clock` y `PrintStream` inyectados. |
| A6 Sin ciclos | **Cumple parcialmente** | CR-01. |
| B1 Nombres | Cumple | — |
| B2 Métodos cortos | Cumple | Máximo 22 líneas (secuencia lineal del demo). |
| B3 Sin mágicos | Cumple | — |
| B4 Sin duplicación | **Cumplía parcialmente → Cumple** | CR-03 (regla de email en dos lugares). |
| B5 Sin código muerto | Cumple | — |
| B6 Sin ocultamiento | **No cumplía → Cumple** | CR-05. |
| C1 Encapsulamiento | Cumple | — |
| C2 Reglas en la entidad | Cumple | `Solicitud.cambiarEstado/asignar`, `Responsable.puedeAtender`. |
| C3 Invariantes propias | **No cumplía → Cumple** | CR-03. |
| C4 Objetos parámetro | **Cumple parcialmente** | CR-02. |
| D1 Excepciones tipadas | Cumple | — |
| D2 Causas conservadas | Cumple | `ArchivoInvalidoException` encadena la causa. |
| D3 Recursos | Cumple | UTF-8 y `try-with-resources`. |
| D4 Atomicidad | Cumple | La carga valida todo el archivo antes de reemplazar. |
| D5 UI robusta | Cumple | `ConsolaAppTest.idNoNumericoNoTerminaElPrograma`. |
| E1 Suite verde | Cumple | 97/97 tras las correcciones. |
| E2 Caracterización intacta | Cumple | `git diff estado-inicial..HEAD -- src/test/java/.../CaracterizacionGestorTest.java` solo muestra el commit que la creó. |
| E3 Tests por clase | Cumple | Dominio, servicio, CSV y consola. |
| E4 Sin estado global | Cumple | El contador estático fue eliminado. |
| E5 Deterministas | **Cumple parcialmente** | CR-06. |
| F1–F4 Repositorio | Cumple | — |

## Observaciones y feedback

| Id | Severidad | Ubicación | Observación (feedback) | Decisión | Evidencia |
|---|---|---|---|---|---|
| CR-01 | Media | paquete raíz ↔ `servicio`/`persistencia` | La fachada `GestorSolicitudes` está en el paquete raíz, que también contiene el dominio; los subpaquetes dependen del dominio y la fachada depende de los subpaquetes: hay un ciclo a nivel de paquete. | **Pendiente con plan:** mover la fachada y `Main` a un paquete `app`. Se pospuso porque obliga a modificar los imports de los tests de caracterización, y su inmutabilidad es la evidencia principal del refactoring seguro. | — |
| CR-02 | Baja | `Solicitud.restaurar(...)` | Nueve parámetros: reaparece Long Parameter List. | **Aceptada:** es una fábrica de reconstrucción usada solo por el mapeo CSV; un builder agregaría más código que claridad. | — |
| CR-03 | Alta | `Estudiante.cambiarEmail`, `ValidadorSolicitud` | El estudiante aceptaba cualquier email: la invariante dependía de que el servicio validara antes. La regla además estaba escrita en dos lugares. | **Corregida:** clase `Email` con la regla única; `Estudiante` la exige. | `ef993da`, `DominioTest.estudianteNoAceptaEmailInvalido` |
| CR-04 | Media | `SolicitudService.reemplazarDatos` | `distinct()` deduplicaba estudiantes por identidad de objeto; funcionaba solo porque el lector CSV reutiliza instancias. | **Corregida:** deduplicación explícita por legajo. | `ef993da` |
| CR-05 | Baja | `Solicitud.cambiarEstado` | El parámetro `observacion` ocultaba el campo homónimo; se usaba `this.` para distinguirlos. | **Corregida:** renombrado a `observacionDeCierre` y documentado cuándo se guarda. | `ef993da` |
| CR-06 | Baja | `CaracterizacionGestorTest.formatoDelArchivo` | Compara contra `LocalDate.now()`: si el test corre exactamente a medianoche puede fallar. | **Aceptada:** el test caracteriza la versión inicial, que no permitía inyectar el reloj; los tests nuevos usan `Clock.fixed`. | `SolicitudServiceTest` |
| CR-07 | Baja | `ArchivoCsvSolicitudes.sinSeparador` | Un `;` en la descripción se guarda como `,` (pérdida silenciosa heredada). | **Pendiente:** adoptar escape de CSV estándar; cambia el formato del archivo y requiere migración. | `CaracterizacionGestorTest.formatoDelArchivo` |
| CR-08 | Baja | `ConsolaApp.atender` | Una opción de menú inexistente se ignora sin mensaje. | **Aceptada:** es el comportamiento original y el menú se vuelve a mostrar. | — |

## Feedback general al autor

- **Fortaleza:** el refactoring se hizo en pasos verificables; cada commit deja la
  suite en verde y el historial permite revisar cada técnica por separado.
- **Fortaleza:** las reglas de negocio quedaron en el dominio y son legibles sin
  conocer la consola ni el archivo.
- **A mejorar:** resolver CR-01 en la próxima iteración, antes de que crezca la
  cantidad de clases que dependen de la ubicación actual de la fachada.
- **A mejorar:** documentar en el README el formato del CSV para quien lo consuma
  desde otra herramienta.

## Veredicto

Con CR-03, CR-04 y CR-05 corregidas (commit `ef993da`, 97/97 tests en verde,
golden master sin diferencias) **se cumplen los criterios de aceptación**: no hay
observaciones de severidad Alta abiertas y cada observación tiene decisión registrada.
