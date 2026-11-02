# Declaración de uso de inteligencia artificial

Conforme al punto 7 de la consigna.

| Campo | Detalle |
|---|---|
| Integrantes | Basualdo Arcati, Pablo Andrés · Calcatelli, Renzo |
| Herramienta | Claude Code (Anthropic), modelo Claude Opus 5.5, en la aplicación de escritorio. |
| Fecha | 29/10/2026 |
| Finalidad | Asistencia para construir el sistema base, escribir los tests de caracterización, ejecutar los refactorings paso a paso, medir métricas y cobertura, y redactar la documentación técnica, el informe y el guion del video. |
| Grado de intervención | **Alto.** La herramienta generó la mayor parte del código, de los tests y de la documentación a partir de las indicaciones del equipo (dominio, tecnología, alcance, integrantes) y de la consigna oficial. |
| Revisión humana | Los integrantes revisan, ejecutan y estudian cada decisión, smell, refactoring y test para poder explicarlos y defenderlos. La IA no reemplaza la autoría ni la defensa. |
| Evidencia | Transcripción de las sesiones, exportada desde la aplicación y adjunta en el anexo ZIP (`ia/`). |

## Registro de prompts (resumen ordenado)

| # | Prompt / indicación del equipo | Resultado |
|---|---|---|
| 1 | Se compartió la consigna oficial del TPI (PDF y texto del aula virtual). | Lectura de requisitos: etapas, entregables, tags obligatorios, rúbrica. |
| 2 | Elección: caso "Solicitudes Académicas", "Java + JUnit 5". | Definición del alcance y plan de trabajo. |
| 3 | (Ejecución guiada) Construcción de la versión inicial con problemas de diseño detectables. | Tag `estado-inicial`. |
| 4 | (Ejecución guiada) Tests de caracterización y golden master sobre la versión inicial. | 59 casos en verde. |
| 5 | (Ejecución guiada) Diagnóstico de calidad y code smells con métricas. | Tag `etapa-diagnostico`. |
| 6 | (Ejecución guiada) Refactorings R1–R5, un commit cada uno, suite en verde. | Tag `etapa-refactoring`. |
| 7 | (Ejecución guiada) Tests unitarios nuevos, evidencias antes/después, cobertura. | Tag `etapa-pruebas`. |
| 8 | (Ejecución guiada) Code review con checklist y correcciones. | Commit `ef993da`. |
| 9 | (Ejecución guiada) Documentación e informe PDF. | Tag `etapa-docs`. |
| 10 | "Realizar el trabajo en una nueva carpeta, presentado por Basualdo Arcati Pablo Andrés y Calcatelli Renzo, con un guion para el video que indique qué dice cada integrante y qué se muestra." | Adaptación de la autoría a entrega grupal, reparto de etapas para la defensa, presentación de diapositivas y guion del video. |

> Completar esta tabla con cualquier interacción adicional (por ejemplo, consultas
> para estudiar los conceptos antes de la defensa) y adjuntar la transcripción.
