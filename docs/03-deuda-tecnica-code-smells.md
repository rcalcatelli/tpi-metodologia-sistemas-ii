# Etapa 3 — Deuda técnica y code smells

Diagnóstico sobre el tag `estado-inicial`. Las líneas refieren a ese tag.
Prioridad: **Alta** = bloquea la evolución o esconde defectos; **Media** = encarece
el mantenimiento; **Baja** = cosmético o de bajo riesgo.

| # | Smell / debilidad | Ubicación | Evidencia | Impacto | Riesgo de mantenimiento | Prioridad |
|---|---|---|---|---|---|---|
| S1 | **God Class** (viola SRP) | `GestorSolicitudes.java:15-441` | Una clase valida, aplica reglas de estado, asigna por área, busca, ordena, reporta, persiste en CSV e imprime en consola. 397 LOC, CC total 139. | Toda modificación pasa por el mismo archivo. | Alto: regresiones cruzadas entre funciones no relacionadas. | Alta |
| S2 | **Long Method** | `registrarSolicitud` `:34-122` (81 líneas, 6 bloques `// validar`) · `generarReporte` `:286-362` | El método valida 6 campos, controla duplicados, busca/crea el estudiante, calcula la prioridad y construye la solicitud. | Difícil de leer y de probar por partes. | Alto: un cambio en una validación obliga a releer todo. | Alta |
| S3 | **Long Parameter List** | `registrarSolicitud(String, String, String, String, String, int)` `:34` · `buscar(String, String, String, int)` `:252` | 5 parámetros `String` seguidos: el compilador no detecta si se invierten nombre y email. Los filtros usan `null`/`0` como "sin filtro". | Llamadas confusas y propensas a errores. | Medio-alto. | Alta |
| S4 | **Primitive Obsession** (type codes) | `Solicitud.java:9-12` | `tipo` y `estado` son `String`; `prioridad` es `int` 1-3. | `"Resuelta"` o `prioridad = 7` compilan. | Alto: errores en ejecución en vez de compilación. | Alta |
| S5 | **Switch statements / condicionales por tipo** (viola OCP) | `:93-108` (prioridad por tipo), `:222-229` (área por tipo), `:156-191` (máquina de estados con `if` anidados), `Solicitud.toString:20-26` | La misma decisión ("según el tipo...") repetida en varios lugares. | Agregar un tipo o estado requiere editar 5 sitios. | Alto: olvidar uno no da error de compilación. | Alta |
| S6 | **Duplicated Code** | Email `:56` y `:126`; búsqueda por id `:141-146`, `:197-202`, `:243-247`; "¿está cerrada?" `:152`, `:207`, `:350`; 11 bucles de conteo `:292-349` | Mismo código copiado con variaciones mínimas. | Una corrección debe replicarse a mano. | Medio-alto. | Alta |
| S7 | **Manejo de errores con códigos de retorno** | `return -1` / `return false` + `System.out.println("Error: ...")` en todos los métodos; `catch (Exception e)` `:377`, `:425` | El llamador no sabe *por qué* falló; la causa de la excepción se descarta. `cambiarEstado(id, null, ...)` lanza `NullPointerException` `:156`. | Errores silenciosos y diagnósticos imposibles. | Alto. | Alta |
| S8 | **Data Class + campos públicos** (Inappropriate Intimacy) | `Solicitud`, `Estudiante`, `Responsable`; listas `lst`, `ests`, `resps` públicas `:20-22` | Cualquier clase puede hacer `s.estado = "RESUELTA"` salteando las reglas. | Las invariantes del dominio no están protegidas. | Alto. | Alta |
| S9 | **Feature Envy** | `cambiarEstado :140-194`, `asignarResponsable :196-240` | El gestor lee y escribe el estado y el responsable de `Solicitud` y decide por ella. | La regla de negocio vive lejos del dato. | Medio. | Media |
| S10 | **Estado global mutable** | `static int ultimoId` `:18`, reiniciado en el constructor `:25` | Dos instancias de `GestorSolicitudes` comparten el contador: crear una segunda reinicia los ids de la primera. | Defecto latente (ids repetidos). | Medio. | Media |
| S11 | **Dependencias concretas ocultas** (viola DIP) | `LocalDate.now()` `:117`; `new FileWriter/FileReader` `:367`, `:386`; `System.out` en la lógica | La clase crea sus propias dependencias. | No se puede reemplazar la persistencia ni controlar la fecha en tests. | Medio. | Media |
| S12 | **Nombres poco expresivos** | `lst`, `ests`, `resps`, `d`, `c`, `x`, `p`, `l`, `est`, `desc`, `obs`, `resp` | Abreviaturas y letras sueltas. | Aumenta el tiempo de comprensión. | Medio. | Media |
| S13 | **Recursos sin cerrar y charset implícito** | `guardar :365-380`, `cargar :384-428` | Sin `try-with-resources`: si falla una línea, el archivo queda abierto. `FileWriter` usa la codificación de la plataforma. | Archivos bloqueados; acentos corruptos entre equipos. | Medio. | Media |
| S14 | **UI frágil** | `Main.java` opciones 2 y 3 | `Integer.parseInt` sin control: ingresar "abc" como id termina el programa. `main` tiene 81 líneas y CC 20. | Defecto visible por el usuario. | Medio. | Media |
| S15 | **Algoritmo reinventado** | `buscar :272-283` | Ordenamiento burbuja manual en vez de `Comparator`. | Más código para mantener. | Bajo. | Baja |
| S16 | **Comentarios desodorante y código muerto** | `:431-436` (método `proc` comentado con `TODO: borrar`); comentarios `// validar ...` | Los comentarios explican código que debería explicarse solo. | Ruido. | Bajo. | Baja |

## Priorización

Se atacan primero los smells que **bloquean la evolución** o **esconden defectos**
(S1–S8), porque son la base para los siguientes: no se puede mover responsabilidad
(S9) ni invertir dependencias (S11) mientras todo sea `String` público dentro de una
sola clase. S15 y S16 se corrigen de paso.

| Orden | Smells | Técnica principal |
|---|---|---|
| 1 | S4, S5 | Replace Type Code with Enum; Move Method a los enums |
| 2 | S2, S6, S12 | Extract Method; Rename |
| 3 | S3 | Introduce Parameter Object |
| 4 | S8, S9 | Encapsulate Field; Move Method a `Solicitud` |
| 5 | S7 | Replace Error Code with Exception |
| 6 | S1, S10, S11, S13 | Extract Class (servicio, repositorio, reporte); inyección de dependencias |
| 7 | S14, S15, S16 | Extract Method en la UI; `Comparator`; Remove Dead Code |
