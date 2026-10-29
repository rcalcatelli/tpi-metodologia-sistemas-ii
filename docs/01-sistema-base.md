# Etapa 1 — Selección y descripción del sistema base

## Dominio

Gestión de **solicitudes académicas** de estudiantes de una tecnicatura: pedidos de
certificados, equivalencias, revisiones de nota y bajas de materia. Cada solicitud
se clasifica por tipo, prioridad y estado, y la atiende un responsable del área
que corresponde.

## Alcance

Módulo de consola, sin interfaz gráfica ni base de datos. Los datos viven en memoria
y pueden guardarse/cargarse desde un archivo CSV. Todos los datos de ejemplo son
**sintéticos** (nombres inventados y correos `@example.com`).

## Actores

| Actor | Interés |
|---|---|
| Estudiante | Presenta solicitudes (las registra el personal administrativo en su nombre). |
| Personal administrativo (Alumnado / Bedelía) | Registra, asigna, consulta y cierra solicitudes. |
| Responsable de área | Atiende la solicitud: la resuelve o la rechaza con motivo. |
| Coordinación | Consulta el reporte de estado general. |

## Entidades

| Entidad | Datos principales |
|---|---|
| `Solicitud` | id, estudiante, tipo, descripción, prioridad, estado, responsable, fecha, observación |
| `Estudiante` | legajo, nombre, email |
| `Responsable` | id, nombre, área |
| Estado | PENDIENTE → EN_PROCESO → RESUELTA / RECHAZADA |

## Funcionalidades (operaciones verificables)

1. **Registrar solicitud** con validación de datos y prioridad automática según tipo.
2. **Cambiar estado** respetando el ciclo de vida (rechazo con motivo obligatorio,
   resolución solo con responsable asignado, estados finales inmodificables).
3. **Asignar responsable** del área correcta (Alumnado, Departamento Académico, Bedelía).
4. **Buscar/filtrar** por estado, tipo, legajo y prioridad, ordenado por prioridad.
5. **Reporte** con totales por estado, tipo y prioridad, pendientes sin responsable y tasa de resolución.
6. **Persistencia** en CSV (guardar y cargar).
7. **Actualizar email** de un estudiante.

## Reglas de negocio

| Regla | Detalle |
|---|---|
| Legajo | Numérico, 5 o 6 dígitos. |
| Email | Debe contener `@` (no al inicio) y `.`. |
| Descripción | Obligatoria, máximo 500 caracteres. |
| Prioridad | 1 alta, 2 media, 3 baja; 0 = automática (Revisión de nota → alta; Equivalencia y Baja → media; Certificado → baja). |
| Duplicados | Un estudiante no puede tener dos solicitudes abiertas del mismo tipo. |
| Asignación | Certificado → Alumnado; Equivalencia y Revisión de nota → Departamento Académico; Baja de materia → Bedelía. Asignar pasa una solicitud PENDIENTE a EN_PROCESO. |

## Tecnología

Java 17, Maven, JUnit 5. Sin dependencias de producción.

## Repositorio y versión inicial

- Tag `estado-inicial`: versión base con deuda técnica (5 clases, 560 LOC).
- Evidencia de funcionamiento: salida del escenario `--demo` en
  [`evidencias/demo-salida-antes.txt`](evidencias/demo-salida-antes.txt).

```bash
git checkout estado-inicial
mvn package
java -jar target/sistema-solicitudes-academicas-1.0.0.jar --demo
```
