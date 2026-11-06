# Declaración de uso de inteligencia artificial

Conforme al punto 7 de la consigna.

| Campo | Detalle |
|---|---|
| Integrantes | Basualdo Arcati, Pablo Gabriel Ramón · Calcatelli, Renzo |
| Herramienta | Claude Code (Anthropic), modelo Claude Opus 5.5, en la aplicación de escritorio. |
| Período de uso | 29/10/2026 al 06/11/2026 |
| Finalidad | Asistencia para implementar el sistema base, escribir los tests de caracterización, aplicar los refactorings paso a paso, medir métricas y cobertura, y redactar la documentación técnica, el informe y el guion del video, según lo definido por el equipo. |
| Grado de intervención | **Medio.** Todas las decisiones del trabajo las tomó el equipo: dominio, tecnología, alcance, problemas de diseño a introducir, smells a priorizar, refactorings a aplicar y su orden, criterios de prueba, estructura de la documentación y reparto de la defensa. La herramienta actuó como asistente de implementación y redacción: produjo borradores de código, tests y textos a partir de esas decisiones, que el equipo revisó, corrigió y aprobó antes de incorporarlos. |
| Revisión humana | Los integrantes ejecutan, revisan y ajustan cada resultado propuesto por la IA, y estudian cada decisión, smell, refactoring y test para poder explicarlos y defenderlos. La IA no reemplaza la autoría ni la defensa. |
| Evidencia | Transcripción completa de las sesiones, exportada desde la aplicación y adjunta en el anexo ZIP (`ia/`). La tabla siguiente es un resumen ordenado; el detalle de cada prompt y respuesta está en esa transcripción. |

## Registro de prompts (resumen ordenado)

| # | Prompt / indicación del equipo | Resultado |
|---|---|---|
| 1 | Se compartió la consigna oficial del TPI (PDF y texto del aula virtual). | Lectura de requisitos: etapas, entregables, tags obligatorios, rúbrica. |
| 2 | Decisión del equipo: caso "Solicitudes Académicas", "Java + JUnit 5", alcance y plan de trabajo por etapas. | Plan de trabajo acordado. |
| 3 | El equipo definió la versión inicial y los problemas de diseño que debía contener; la IA asistió en la implementación. | Tag `estado-inicial`. |
| 4 | El equipo definió qué comportamientos fijar con tests de caracterización y golden master; la IA asistió en su escritura. | 59 casos en verde. |
| 5 | El equipo eligió las métricas y priorizó los code smells del diagnóstico; la IA asistió en la medición y redacción. | Tag `etapa-diagnostico`. |
| 6 | El equipo decidió los refactorings R1–R5 y su orden (un commit cada uno, suite en verde); la IA asistió en su aplicación. | Tag `etapa-refactoring`. |
| 7 | El equipo definió los tests unitarios nuevos y las evidencias antes/después; la IA asistió en la escritura y la medición de cobertura. | Tag `etapa-pruebas`. |
| 8 | El equipo realizó el code review con el checklist y decidió las correcciones; la IA asistió en aplicarlas. | Commit `ef993da`. |
| 9 | El equipo definió la estructura de la documentación y del informe; la IA asistió en la redacción y el armado del PDF. | Tag `etapa-docs`. |
