# Etapa 6 — Pruebas y verificación del comportamiento

## Estrategia

1. **Red de seguridad antes de tocar el código.** Sobre la versión inicial se
   escribieron tests de caracterización que describen *lo que el sistema hace hoy*
   (no lo que debería hacer), usando solo la API pública de `GestorSolicitudes`
   y lo que el usuario ve (`toString`, reporte, archivo CSV).
2. **Golden master.** La salida completa del escenario `--demo` (37 líneas:
   mensajes, listado y reporte) quedó registrada en
   `src/test/resources/golden/demo-esperado.txt` y se compara en cada ejecución.
3. **Refactorings pequeños.** Cada refactoring fue un commit; después de cada uno
   se ejecutó la suite completa **sin modificar los tests**.
4. **Tests unitarios nuevos** para las clases extraídas, que antes no se podían
   probar aisladamente.

## Suites

| Suite | Casos | Qué verifica | Escrita sobre |
|---|---:|---|---|
| `CaracterizacionGestorTest` | 58 | Registro y validaciones, ciclo de estados, asignación por área, búsqueda y orden, reporte, CSV, email | `estado-inicial` |
| `DemoGoldenMasterTest` | 1 | Salida completa del escenario de demostración | `estado-inicial` |
| `DominioTest` | 24 | Tabla de transiciones de `Estado`, prioridad/área por tipo, reglas de `Solicitud` y `Estudiante` | versión refactorizada (+1 por el code review) |
| `SolicitudServiceTest` | 7 | Reloj inyectado, numeración por instancia, excepciones tipadas, filtros | versión refactorizada |
| `ArchivoCsvSolicitudesTest` | 4 | UTF-8, línea inválida, responsable inexistente, carga atómica | versión refactorizada |
| `ConsolaAppTest` | 4 | Entrada no numérica, flujo completo por menú, pregunta de tipo armada desde el enum, fin de entrada | versión refactorizada (+1 por CR-09) |
| **Total** | **98** | | |

## Casos de prueba representativos

| Id | Caso | Entrada | Resultado esperado | Antes | Después |
|---|---|---|---|---|---|
| CP01 | Registro válido | legajo 10001, CERTIFICADO, prioridad 0 | id 1, PENDIENTE, prioridad BAJA | ✅ | ✅ |
| CP02 | Legajo inválido | "12a45", "1234", "1234567" | -1, "Error: legajo invalido" | ✅ | ✅ |
| CP03 | Duplicado abierto | 2.ª CERTIFICADO del mismo legajo | -1 | ✅ | ✅ |
| CP04 | Rechazo sin motivo | RECHAZADA con observación vacía | false, estado sin cambio | ✅ | ✅ |
| CP05 | Resolver sin responsable | EN_PROCESO → RESUELTA | false | ✅ | ✅ |
| CP06 | Responsable de otra área | EQUIVALENCIA → responsable 1 (Alumnado) | false, sin asignar | ✅ | ✅ |
| CP07 | Orden de búsqueda | 4 solicitudes de distinta prioridad | ids 2,3,4,1 | ✅ | ✅ |
| CP08 | Tasa de resolución | 1 resuelta, 2 rechazadas | "33.3%" | ✅ | ✅ |
| CP09 | CSV ida y vuelta | guardar → cargar en otro gestor | mismo reporte, ids continúan | ✅ | ✅ |
| CP10 | Golden master | `--demo` | 37 líneas idénticas | ✅ | ✅ |
| CP11 | Id no numérico en el menú | opción 2, id "abc" | mensaje de error y vuelve al menú | ❌ termina con `NumberFormatException` | ✅ |
| CP12 | Dos gestores en paralelo | registrar en ambos | cada uno numera desde 1 | ❌ comparten contador estático | ✅ |
| CP13 | Acentos en CSV | "Íñigo Ñúñez" | se leen iguales | ❌ depende del charset de la plataforma | ✅ |
| CP14 | Archivo corrupto | 2.ª línea inválida | false y datos en memoria intactos | ❌ borra la memoria y carga parcial | ✅ |

CP11–CP14 son **defectos de la versión inicial** detectados en el diagnóstico
(S10, S13, S14). Su corrección es un cambio de comportamiento **intencional y
documentado**; ninguno afecta los casos cubiertos por la caracterización.

## Evidencias

| Evidencia | Archivo |
|---|---|
| Suite ejecutada en **cada commit** del historial (59 → … → 96 → 97 → 98, 0 fallos) | [`evidencias/tests-por-commit.txt`](evidencias/tests-por-commit.txt) |
| Resultado final por clase de test | [`evidencias/tests-resultado-final.txt`](evidencias/tests-resultado-final.txt) |
| Salida del demo antes / después y diff | [`demo-salida-antes.txt`](evidencias/demo-salida-antes.txt), [`demo-salida-despues.txt`](evidencias/demo-salida-despues.txt), [`demo-diff.txt`](evidencias/demo-diff.txt) |
| Entrada inválida antes (crash) / después | [`entrada-invalida-antes.txt`](evidencias/entrada-invalida-antes.txt), [`entrada-invalida-despues.txt`](evidencias/entrada-invalida-despues.txt) |
| Métricas antes / después | [`metricas-antes.txt`](evidencias/metricas-antes.txt), [`metricas-despues.txt`](evidencias/metricas-despues.txt) |

```text
Commit    Mensaje                                                       Tests Fallos Errores
88a10bb   Agrega tests de caracterización y golden master ...              59      0      0
3fe4b1e   Documenta diagnóstico ...                                        59      0      0
2f9b24a   Refactor: Replace Type Code with Enum y Move Method ...          59      0      0
b0277d9   Refactor: Extract Method, Rename y eliminación de duplicación    59      0      0
05ebf89   Refactor: Introduce Parameter Object, Encapsulate Field ...      59      0      0
8b133c3   Refactor: Extract Class e inversión de dependencias ...          59      0      0
74ee84c   Refactor: Extract Class en la interfaz de consola                59      0      0
6808995   Agrega tests unitarios de dominio, servicio, persistencia ...    96      0      0
7268c24   Documenta pruebas y verificación ...                             96      0      0
ef993da   Aplica correcciones del code review                              97      0      0
...       (documentación, herramientas y PRs #1 a #3)                      97      0      0
43ca3e1   Arma la pregunta de tipo del menú desde TipoSolicitud            98      0      0
```

## Cómo reproducir

```bash
mvn test                                   # suite completa
tools/verificar-historial.sh               # suite en cada commit
git checkout estado-inicial && mvn test    # no hay tests: se agregan en el commit siguiente
```

## Límites

- Los tests de caracterización fijan el comportamiento observable, incluidos
  detalles discutibles (por ejemplo, que un `;` en la descripción se guarde como `,`).
  Cambiarlos requiere una decisión explícita, no un efecto colateral.
- La cobertura de la versión final, medida con JaCoCo (`mvn verify`, reporte en
  `target/site/jacoco/index.html`), es **97,0 % de líneas y 92,2 % de ramas**
  ([`evidencias/cobertura.txt`](evidencias/cobertura.txt)). Lo no cubierto son
  ramas defensivas (por ejemplo, error de escritura en disco al guardar).
- La versión inicial no tenía tests, por lo que no hay cobertura "antes" con
  la cual comparar.
