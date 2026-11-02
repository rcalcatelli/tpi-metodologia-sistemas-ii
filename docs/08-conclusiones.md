# Etapa 8 — Conclusiones y aprendizaje

## Conclusión del trabajo

La versión inicial *funcionaba*: un usuario no habría notado diferencias con la
versión final en el uso normal. La deuda estaba en la estructura: una clase de 397
líneas con complejidad 139, reglas repetidas, tipos representados con `String` y
dependencias ocultas. El refactoring no agregó funcionalidad; redujo el costo del
próximo cambio. Agregar un tipo de solicitud pasó de tocar cinco lugares sin ayuda
del compilador a agregar una constante en un enum.

## Decisiones tomadas

| Decisión | Motivo |
|---|---|
| Escribir tests de caracterización antes de refactorizar | Sin una red de seguridad no hay forma de afirmar que el comportamiento externo se conservó. |
| Un commit por refactoring y suite completa en cada uno | Permite revisar y, si hace falta, revertir cada paso por separado. |
| Conservar `GestorSolicitudes` como fachada | Mantener intactos los tests originales es la evidencia más fuerte de seguridad. |
| Corregir defectos (crash del menú, contador estático, charset) solo después de caracterizar | Separar "refactorizar" de "cambiar comportamiento" y documentar cada cambio intencional. |
| Enums con comportamiento en lugar de patrón State | Proporcional al tamaño del problema. |

## Dificultades

- **Preservar el orden de los mensajes de error.** Convertir los textos a enums
  antes de validar cambiaba qué error se informaba primero cuando había más de un
  dato inválido. Se resolvió con `DatosSolicitud` como objeto de entrada crudo y
  conversión después de la validación.
- **Distinguir defectos de comportamiento esperado.** Los tests de caracterización
  fijan también lo discutible (el `;` convertido en `,`). Hubo que decidir caso por
  caso qué se conservaba y qué se corregía.
- **El tamaño total del código creció** (560 → 1015 LOC). Fue necesario justificar
  con métricas de complejidad por método que la mejora es real aunque haya más líneas.

## Límites del trabajo

- Sistema de consola con persistencia en archivo: no se evaluaron concurrencia,
  rendimiento ni seguridad.
- Las métricas provienen de un script propio aproximado, no de una herramienta
  certificada (SonarQube, PMD).
- Quedan observaciones del code review pendientes (CR-01 ciclo de paquetes,
  CR-07 escape del CSV).

## Mejoras futuras

1. Mover la fachada a un paquete `app` y eliminar el ciclo de paquetes (CR-01).
2. Formato CSV estándar con escape de separadores (CR-07) o persistencia en SQLite
   implementando `SolicitudRepository`, sin tocar el servicio.
3. Análisis estático en el build (PMD/Checkstyle) y umbral de cobertura en JaCoCo.
4. Historial de cambios de estado por solicitud (auditoría).
5. Integración continua (GitHub Actions) que ejecute `mvn verify` en cada push.

## Relación con los contenidos de la asignatura

| Unidad / tema | Dónde se aplicó |
|---|---|
| Calidad externa vs. interna | Etapa 2: el sistema "anda" pero su estructura encarece el cambio. |
| Deuda técnica y code smells | Etapa 3: 16 smells con ubicación, impacto, riesgo y prioridad. |
| SOLID | Etapa 4: SRP, OCP, LSP, ISP y DIP con ejemplos concretos del código. |
| Clean Code | Nombres, funciones cortas, excepciones, sin duplicación ni código muerto. |
| Refactoring | Etapa 5: catálogo de Fowler aplicado en pasos pequeños y verificados. |
| Verificación y validación | Etapa 6: caracterización, golden master, unitarios y cobertura. |
| Code review | Etapa 7: checklist, observaciones con severidad y decisión, criterios de aceptación. |
| Repositorios y control de versiones | Commits atómicos, tags por etapa, historial verificable. |

## Conclusiones individuales

> **Cada integrante completa su párrafo con sus propias palabras** (la consigna pide
> conclusiones grupales e individuales breves). Guía: qué concepto se entendió mejor
> al aplicarlo, qué decisión costó más justificar y qué se haría distinto en un
> próximo proyecto.

### Basualdo Arcati, Pablo Gabriel Ramón

[COMPLETAR — 4 a 6 líneas]

### Calcatelli, Renzo

[COMPLETAR — 4 a 6 líneas]
