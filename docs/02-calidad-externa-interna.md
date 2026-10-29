# Etapa 2 — Calidad externa vs. calidad interna

## Calidad externa (lo que ve el usuario)

Desde la consola, la versión inicial **funciona**: las 7 operaciones hacen lo esperado
y el escenario de demostración produce resultados correctos (ver
[`evidencias/demo-salida-antes.txt`](evidencias/demo-salida-antes.txt)).

| Atributo externo | Estado en `estado-inicial` |
|---|---|
| Funcionalidad | Correcta para los casos normales: valida datos, respeta el ciclo de estados, asigna por área, filtra y reporta. |
| Usabilidad | Menú simple; los errores se informan con mensajes cortos. |
| Fiabilidad | **Falla** si se ingresa un id no numérico en las opciones 2 y 3 del menú (`NumberFormatException` termina el programa). |
| Integridad de datos | Un `;` en la descripción se reemplaza por `,` al guardar (pérdida silenciosa). El CSV usa la codificación de la plataforma: en Windows los acentos pueden corromperse al cambiar de equipo. |

Conclusión externa: un usuario que no conozca el código diría que el sistema "anda".

## Calidad interna (lo que ve el equipo técnico)

| Atributo interno | Evidencia | Consecuencia |
|---|---|---|
| Cohesión | `GestorSolicitudes` (397 LOC, CC total 139) valida, aplica reglas, ordena, reporta, persiste e imprime. | Cualquier cambio toca la misma clase; alto riesgo de conflictos y regresiones. |
| Acoplamiento | `Main` y los tests dependen de la clase concreta; la persistencia está atada a `FileWriter`; el tiempo a `LocalDate.now()`. | No se puede cambiar CSV por base de datos ni probar fechas sin modificar la clase. |
| Legibilidad | Nombres como `lst`, `ests`, `d`, `c`, `x`, `p`; comentarios `// validar ...` que suplen métodos faltantes. | Más tiempo para entender antes de modificar. |
| Extensibilidad | Tipos, estados y prioridades son `String`/`int`; las reglas están repartidas en `switch` e `if` encadenados. | Agregar un tipo de solicitud exige editar 5 lugares distintos (OCP). |
| Robustez | Errores devueltos como `-1`/`false` + `System.out`; `catch (Exception)` que oculta la causa. | Los errores no se pueden distinguir ni tratar programáticamente. |
| Testeabilidad | Contador estático compartido, salida por consola en la lógica, sin interfaces. | Tests más frágiles y lentos de escribir. |

Métricas medidas con [`tools/metricas.py`](../tools/metricas.py)
([`evidencias/metricas-antes.txt`](evidencias/metricas-antes.txt)):

| Clase | LOC | Métodos | Método más largo | CC máx. | CC total |
|---|---:|---:|---|---:|---:|
| GestorSolicitudes | 397 | 11 | registrarSolicitud (81 l.) | 36 | 139 |
| Main | 110 | 2 | main (81 l.) | 20 | 24 |

## Relación entre ambas

La calidad externa es aceptable **hoy**, pero la interna determina el **costo del
próximo cambio**. Ejemplo concreto: la cátedra pide agregar el tipo
"CONSTANCIA_EXAMEN". En la versión inicial hay que modificar la validación de tipo,
el `switch` de prioridad, el `if` de área, el reporte (un bucle nuevo) y la ayuda del
menú, sin que el compilador avise si se olvida alguno. Esa es la deuda técnica:
el sistema funciona, pero cada evolución cuesta más y es más riesgosa.
