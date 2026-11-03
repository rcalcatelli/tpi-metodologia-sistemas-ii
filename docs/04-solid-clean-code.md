# Etapa 4 — Aplicación de SOLID y Clean Code

## Arquitectura resultante

```text
ar.edu.utn.tup.solicitudes                 dominio + fachada
├── Solicitud, Estudiante, Responsable     entidades que protegen sus reglas
├── Estado, TipoSolicitud, Prioridad, Area enums con comportamiento
├── DatosSolicitud, FiltroSolicitudes      objetos parámetro
├── Email                                  regla única de formato
├── SolicitudException (+3 subclases)      errores del dominio
├── GestorSolicitudes                      fachada compatible con la API original
├── Main                                   punto de entrada
├── servicio/   SolicitudService, ValidadorSolicitud
├── persistencia/ SolicitudRepository, EstudianteRepository, ResponsableRepository,
│                 RepositorioSolicitudesEnMemoria, RepositorioEstudiantesEnMemoria,
│                 CatalogoResponsables, ArchivoCsvSolicitudes
├── reporte/    ReporteSolicitudes
└── ui/         ConsolaApp, EscenarioDemo
```

```text
 ConsolaApp / EscenarioDemo
            │ usa
            ▼
   GestorSolicitudes (fachada) ──► ReporteSolicitudes
            │                 └──► ArchivoCsvSolicitudes ──► ResponsableRepository
            ▼
     SolicitudService ──► ValidadorSolicitud
            │        ──► Clock
            ▼ (interfaces)
 SolicitudRepository · EstudianteRepository · ResponsableRepository
            ▲ implementan
 RepositorioSolicitudesEnMemoria · RepositorioEstudiantesEnMemoria · CatalogoResponsables

 Solicitud ──► Estado (transiciones) · TipoSolicitud (prioridad, área) · Responsable.puedeAtender
```

## Principios SOLID

| Principio | Problema en `estado-inicial` | Mejora aplicada | Dónde |
|---|---|---|---|
| **SRP** — una razón de cambio | `GestorSolicitudes` cambiaba por validación, reglas, reporte, CSV y consola (S1). | Cada responsabilidad en su clase: `ValidadorSolicitud` (formato), `Solicitud` (ciclo de vida), `SolicitudService` (casos de uso), `ReporteSolicitudes`, `ArchivoCsvSolicitudes`, `ConsolaApp`. | commits `8b133c3`, `74ee84c` |
| **OCP** — abierto a extensión, cerrado a modificación | Agregar un tipo de solicitud tocaba 5 lugares (S5). | El tipo conoce su prioridad y su área; el reporte y el menú de la consola recorren `TipoSolicitud.values()`. Un tipo nuevo = **una línea** en el enum. | `TipoSolicitud`, `ReporteSolicitudes`, `ConsolaApp` |
| **LSP** — sustitución | No había jerarquías. | Cualquier implementación de `SolicitudRepository` es intercambiable: los tests usan la de memoria igual que la aplicación. Las subclases de `SolicitudException` se tratan uniformemente en la fachada. | `persistencia/`, `GestorSolicitudes.ejecutar` |
| **ISP** — interfaces chicas | No había interfaces. | Tres repositorios separados en lugar de uno genérico: `ArchivoCsvSolicitudes` solo depende de `ResponsableRepository`. | `persistencia/` |
| **DIP** — depender de abstracciones | La lógica creaba `FileWriter`, usaba `LocalDate.now()` y `System.out` (S11). | `SolicitudService` recibe repositorios, validador y `Clock` por constructor; la fachada recibe el `PrintStream`. La composición se hace en un solo lugar (constructor de la fachada). | `SolicitudService`, `GestorSolicitudes` |

## Clean Code

| Práctica | Antes | Después |
|---|---|---|
| **Nombres que revelan intención** | `lst`, `ests`, `d`, `c`, `x`, `p`, `obs`, `resp` | `solicitudes`, `estudiantes`, `reporte`, `observacionDeCierre`, `responsable` |
| **Funciones cortas que hacen una cosa** | `registrarSolicitud` 81 líneas, CC 36 | `SolicitudService.registrar` 9 líneas; validación dividida en `validarLegajo`, `validarNombre`, … |
| **Sin números ni textos mágicos** | `prioridad == 1`, `"EN_PROCESO"` repetido | `Prioridad.ALTA`, `Estado.EN_PROCESO`, `LARGO_MAXIMO_DESCRIPCION` |
| **Manejo de errores con excepciones** | `return -1` + `System.out` + `catch (Exception)` | `DatosInvalidosException`, `ReglaNegocioException`, `RecursoNoEncontradoException`, `ArchivoInvalidoException` con mensaje y causa |
| **Cohesión / encapsulamiento** | Campos públicos modificables desde cualquier clase | Campos `private final`; el estado cambia solo vía `cambiarEstado` y `asignar` |
| **Bajo acoplamiento** | Todo depende de `GestorSolicitudes` concreto | El dominio no conoce persistencia ni consola |
| **Límites entre componentes** | Consola, lógica y archivo mezclados | Capas: `ui` → fachada → `servicio` → repositorios; el CSV traduce en el borde |
| **Comentarios** | `// validar email` sobre bloques de código; código comentado | Javadoc solo donde aporta el *por qué*; el código muerto se eliminó |
| **DRY** | Validación de email ×2, búsqueda por id ×3, 11 bucles de conteo | Una sola implementación de cada regla |

## Métricas antes / después

Fuente: [`tools/metricas.py`](../tools/metricas.py) sobre `estado-inicial` y la versión final.

| Indicador | Antes | Después |
|---|---:|---:|
| Clases | 5 | 29 |
| LOC totales (sin comentarios ni líneas vacías) | 560 | 1015 |
| Clase más grande | GestorSolicitudes, 397 LOC | GestorSolicitudes (fachada), 128 LOC |
| Método más largo | 81 líneas (`registrarSolicitud`, `main`) | 22 líneas (`EscenarioDemo.ejecutar`, secuencial) |
| Complejidad ciclomática máxima por método | 36 | 10 (`Solicitud.cambiarEstado`) |
| Complejidad total de la clase central | 139 | 28 |
| Tests automatizados | 0 | 98 |

Las líneas totales **aumentan**: se cambia código concentrado por más clases
pequeñas, interfaces y firmas explícitas. El objetivo no fue reducir líneas sino
reducir el costo de entender y modificar cada parte.

## Decisiones y alternativas descartadas

- **Mantener `GestorSolicitudes` como fachada** en vez de eliminarlo: permite que los
  tests de caracterización y la consola sigan funcionando sin cambios, que es la
  evidencia central de que el refactoring fue seguro.
- **Enums con comportamiento** en vez de patrón State con una clase por estado: con
  cuatro estados y reglas simples, una clase por estado agregaba más estructura que
  claridad.
- **`DatosSolicitud` con `String`/`int`**: es un objeto de entrada que representa lo
  que tipea el usuario; la conversión a tipos del dominio ocurre después de validar,
  para conservar el orden de los mensajes de error originales.
