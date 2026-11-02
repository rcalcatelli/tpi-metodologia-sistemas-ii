# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador — **Metodología de Sistemas II** — Tecnicatura
Universitaria en Programación a Distancia (UTN).

Aplicación de consola en Java para registrar solicitudes de estudiantes
(certificados, equivalencias, revisiones de nota y bajas de materia), asignarlas a
un responsable del área correspondiente, cambiar su estado, buscarlas y obtener un
reporte. El foco del trabajo no es la funcionalidad sino la **evolución de la
calidad interna**: diagnóstico de deuda técnica, refactoring seguro guiado por
tests y code review.

## Funcionalidades

| Operación | Descripción |
|---|---|
| Registrar solicitud | Valida legajo, nombre, email, tipo, descripción y prioridad; prioridad automática según tipo; impide duplicados abiertos. |
| Cambiar estado | PENDIENTE → EN_PROCESO → RESUELTA / RECHAZADA; rechazo con motivo; resolución con responsable. |
| Asignar responsable | Solo del área que atiende el tipo (Alumnado, Departamento Académico, Bedelía). |
| Buscar / filtrar | Por estado, tipo, legajo y prioridad; ordenado por prioridad. |
| Reporte | Totales por estado, tipo y prioridad; sin responsable; tasa de resolución. |
| Guardar / cargar | Archivo CSV en UTF-8. |
| Actualizar email | Del estudiante, con validación. |

## Tecnologías

Java 17 · Maven · JUnit 5 · JaCoCo · Python 3 (script de métricas)

## Instalación y uso

```bash
mvn verify                                                         # compila, 97 tests y reporte de cobertura
java -jar target/sistema-solicitudes-academicas-1.0.0.jar          # menú interactivo
java -jar target/sistema-solicitudes-academicas-1.0.0.jar --demo   # escenario de demostración
```

Reporte de cobertura: `target/site/jacoco/index.html`.

## Versión inicial y versión refactorizada

| Tag | Contenido |
|---|---|
| `estado-inicial` | Sistema base con deuda técnica (5 clases, 560 LOC, sin tests). |
| `etapa-diagnostico` | Tests de caracterización + diagnóstico de calidad y 16 code smells. |
| `etapa-refactoring` | Cinco refactorings aplicados, un commit cada uno. |
| `etapa-pruebas` | Tests unitarios nuevos y evidencias antes/después. |
| `etapa-docs` | Code review, documentación completa e informe. |
| `estado-refactorizado` | Estado final del código. |

```bash
git checkout estado-inicial         # ver la versión original
git checkout estado-refactorizado   # ver la versión final
git diff estado-inicial estado-refactorizado -- src/main
tools/verificar-historial.sh        # ejecuta la suite en cada commit del historial
```

## Resultados

| Indicador | Antes | Después |
|---|---:|---:|
| Clase más grande | 397 LOC | 128 LOC |
| Método más largo | 81 líneas | 22 líneas |
| Complejidad ciclomática máxima por método | 36 | 10 |
| Tests automatizados | 0 | 97 (0 fallos) |
| Cobertura de líneas / ramas | — | 96,9 % / 92,1 % |
| Salida del escenario de demostración | 37 líneas | idéntica |

## Estructura

```text
src/main/java/ar/edu/utn/tup/solicitudes/   dominio + fachada GestorSolicitudes + Main
    servicio/      casos de uso y validación
    persistencia/  repositorios (interfaces y memoria) y archivo CSV
    reporte/       reporte de texto
    ui/            consola y escenario de demostración
src/test/java/...                            caracterización, golden master y unitarios
docs/                                        informe por etapas, checklist y evidencias
tools/                                       métricas y verificación del historial
```

## Documentación

1. [Sistema base](docs/01-sistema-base.md)
2. [Calidad externa vs. interna](docs/02-calidad-externa-interna.md)
3. [Deuda técnica y code smells](docs/03-deuda-tecnica-code-smells.md)
4. [SOLID y Clean Code](docs/04-solid-clean-code.md)
5. [Refactorings](docs/05-refactorings.md)
6. [Pruebas y verificación](docs/06-pruebas.md)
7. [Code review](docs/07-code-review.md) · [Checklist](docs/checklist-code-review.md)
8. [Conclusiones](docs/08-conclusiones.md)
9. [Declaración de uso de IA](docs/ia/declaracion-uso-ia.md)
10. [Evidencias](docs/evidencias/)

## Formato del archivo CSV

Una solicitud por línea, separador `;`, UTF-8:

```text
id;legajo;nombre;email;tipo;descripcion;prioridad;estado;idResponsable;fecha;observacion
1;10001;Ana Torres;ana@example.com;CERTIFICADO;Constancia;3;RESUELTA;1;2026-10-29;Emitido
```

## Autoría

| Integrante | Participación |
|---|---|
| Basualdo Arcati, Pablo Andrés | Expone y defiende: sistema base, calidad externa vs. interna, deuda técnica y code smells, pruebas y verificación. |
| Calcatelli, Renzo | Expone y defiende: SOLID y Clean Code, refactoring seguro, code review y conclusiones. |

El detalle de cómo se produjo el código y la documentación, incluido el uso de IA, está en la [declaración de uso de IA](docs/ia/declaracion-uso-ia.md).

Todos los datos del sistema son sintéticos.
