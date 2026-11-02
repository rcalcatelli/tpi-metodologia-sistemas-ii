# Etapa 5 — Refactoring seguro

Cada refactoring es un commit independiente. Antes y después de cada uno se
ejecutaron los 59 tests de caracterización **sin modificarlos**
([`evidencias/tests-por-commit.txt`](evidencias/tests-por-commit.txt)).

| # | Refactoring (catálogo de Fowler) | Smells que ataca | Commit |
|---|---|---|---|
| R1 | Replace Type Code with Enum + **Move Method** a los enums | S4, S5 | `2f9b24a` |
| R2 | **Extract Method**, Rename Variable, Replace Magic Number with Constant, Remove Dead Code, Substitute Algorithm | S2, S6, S12, S15, S16 | `b0277d9` |
| R3 | **Introduce Parameter Object**, Encapsulate Field, **Move Method** a `Solicitud`/`Responsable`, Replace Error Code with Exception | S3, S7, S8, S9 | `05ebf89` |
| R4 | **Extract Class** (servicio, validador, reporte, CSV, repositorios) + inyección de dependencias | S1, S10, S11, S13 | `8b133c3` |
| R5 | Extract Class + Extract Method en la consola | S14 | `74ee84c` |

---

## R1 — Replace Type Code with Enum + Move Method

**Problema:** tipo y estado eran `String`, la prioridad un `int`; la regla "qué
prioridad y qué área corresponde a cada tipo" estaba en un `switch` y en un `if`.

**Antes** (`GestorSolicitudes.java`, `estado-inicial`; fragmento condensado: en el código real cada `case` ocupa tres líneas):

```java
if (prioridad == 0) {
    switch (tipo) {
        case "CERTIFICADO":   prioridad = 3; break;
        case "EQUIVALENCIA":  prioridad = 2; break;
        case "REVISION_NOTA": prioridad = 1; break;
        case "BAJA_MATERIA":  prioridad = 2; break;
    }
}
// ... 120 líneas después
String area = "";
if (s.tipo.equals("CERTIFICADO")) {
    area = "Alumnado";
} else if (s.tipo.equals("EQUIVALENCIA") || s.tipo.equals("REVISION_NOTA")) {
    area = "Departamento Académico";
} else if (s.tipo.equals("BAJA_MATERIA")) {
    area = "Bedelía";
}
```

**Después** (`TipoSolicitud.java`):

```java
public enum TipoSolicitud {
    CERTIFICADO(Prioridad.BAJA, Area.ALUMNADO),
    EQUIVALENCIA(Prioridad.MEDIA, Area.DEPARTAMENTO_ACADEMICO),
    REVISION_NOTA(Prioridad.ALTA, Area.DEPARTAMENTO_ACADEMICO),
    BAJA_MATERIA(Prioridad.MEDIA, Area.BEDELIA);
    // getPrioridadPorDefecto(), getAreaResponsable()
}
```

La máquina de estados (40 líneas de `if` anidados) pasó a `Estado.puedePasarA`:

```java
public boolean puedePasarA(Estado destino) {
    return switch (this) {
        case PENDIENTE -> destino == EN_PROCESO || destino == RECHAZADA;
        case EN_PROCESO -> destino == RESUELTA || destino == RECHAZADA;
        case RESUELTA, RECHAZADA -> false;
    };
}
```

**Por qué es seguro:** los nombres de las constantes coinciden con los textos
anteriores, así que `toString`, el reporte y el CSV producen exactamente lo mismo.
**Efecto colateral corregido:** `cambiarEstado(id, null, ...)` lanzaba
`NullPointerException`; ahora informa "transicion invalida".

---

## R2 — Extract Method (+ Rename, constantes, eliminación de duplicados)

**Problema:** `registrarSolicitud` tenía 81 líneas con seis bloques marcados por
comentarios `// validar ...`; el reporte repetía 11 bucles casi idénticos.

**Antes:**

```java
public int registrarSolicitud(String legajo, String nombre, String email, String tipo, String desc, int prioridad) {
    // validar legajo
    if (legajo == null || legajo.trim().equals("")) { ... return -1; }
    for (int i = 0; i < legajo.length(); i++) { ... }
    // validar nombre
    ...
    // validar email
    if (email == null || !email.contains("@") || ...) { ... }
    ... (60 líneas más)
}
```

**Después** (estado en el commit `b0277d9`; en la versión final esta lógica está en `SolicitudService.registrar`, porque R3 y R4 la reubicaron):

```java
public int registrarSolicitud(String legajo, String nombre, String email, String tipo, String descripcion, int prioridad) {
    String error = validarDatosSolicitud(legajo, nombre, email, tipo, descripcion, prioridad);
    if (error != null) {
        return informarError(error);
    }
    TipoSolicitud tipoSolicitud = TipoSolicitud.valueOf(tipo);
    if (tieneSolicitudAbierta(legajo, tipoSolicitud)) {
        return informarError("ya existe una solicitud abierta de ese tipo");
    }
    Solicitud solicitud = crearSolicitud(buscarOCrearEstudiante(legajo, nombre, email), tipoSolicitud, descripcion,
            resolverPrioridad(tipoSolicitud, prioridad));
    ...
}
```

Y los 11 bucles del reporte se reemplazaron por un método parametrizado que recorre los enums (también estado en `b0277d9`; en la versión final está en `ReporteSolicitudes.generar`):

```java
for (Estado estado : Estado.values()) {
    agregarConteo(reporte, estado.name(), s -> s.estado == estado);
}
```

**Por qué es seguro:** cada método extraído contiene exactamente las mismas
condiciones en el mismo orden, por lo que los mensajes de error no cambian.

---

## R3 — Introduce Parameter Object, Encapsulate Field, Move Method, excepciones

**Problema:** seis parámetros (cinco `String` seguidos) en el alta y cuatro con
comodines `null`/`0` en la búsqueda; campos públicos; el gestor decidía por la
solicitud (Feature Envy); errores como `-1`/`false`.

**Introduce Parameter Object — ejemplo de uso** (ilustrativo: la consola, el demo y los tests siguen llamando a las firmas anteriores, que delegan en las nuevas):

```java
// antes
gestor.registrarSolicitud("10001", "Ana Torres", "ana@example.com", "CERTIFICADO", "Constancia", 0);
gestor.buscar("PENDIENTE", null, "10001", 0);

// después
gestor.registrarSolicitud(new DatosSolicitud("10001", "Ana Torres", "ana@example.com", "CERTIFICADO", "Constancia", 0));
gestor.buscar(FiltroSolicitudes.todas().conEstado(Estado.PENDIENTE).conLegajo("10001"));
```

Las firmas anteriores se conservan y **delegan** en las nuevas, de modo que
ningún llamador existente se rompe.

**Move Method — la regla vuelve al dato:**

```java
// antes, en GestorSolicitudes (fragmento condensado de estado-inicial)
if (!r.area.equals(area)) { System.out.println("Error: ..."); return false; }
s.resp = r;
if (s.estado.equals("PENDIENTE")) { s.estado = "EN_PROCESO"; }

// después, en Solicitud
public void asignar(Responsable nuevoResponsable) {
    verificarAbierta();
    if (!nuevoResponsable.puedeAtender(tipo)) {
        throw new ReglaNegocioException("el responsable no pertenece al area " + tipo.getAreaResponsable().getNombre());
    }
    responsable = nuevoResponsable;
    if (estado == Estado.PENDIENTE) {
        estado = Estado.EN_PROCESO;
    }
}
```

**Replace Error Code with Exception:** las reglas lanzan excepciones tipadas;
solo la fachada las traduce al contrato antiguo (`-1`/`false` + mensaje), un único
lugar en vez de 25.

---

## R4 — Extract Class + inversión de dependencias

**Problema:** la God Class y sus dependencias ocultas (archivo, reloj, consola,
contador estático).

| Responsabilidad | Antes | Después |
|---|---|---|
| Casos de uso | `GestorSolicitudes` | `SolicitudService` |
| Validación de formato | `GestorSolicitudes` | `ValidadorSolicitud` |
| Almacenamiento | listas públicas + `static int ultimoId` | `SolicitudRepository` / `EstudianteRepository` (+ implementación en memoria con contador propio) |
| Responsables | lista armada en el constructor | `ResponsableRepository` / `CatalogoResponsables` |
| CSV | `FileWriter`/`FileReader` sin cerrar, charset implícito | `ArchivoCsvSolicitudes`: UTF-8, `try-with-resources`, carga atómica |
| Reporte | 76 líneas en el gestor | `ReporteSolicitudes` |
| Fecha | `LocalDate.now()` | `LocalDate.now(reloj)` con `Clock` inyectado |

```java
// Composición en un solo lugar (GestorSolicitudes)
public GestorSolicitudes(ResponsableRepository responsables, Clock reloj, PrintStream salida) {
    this(new SolicitudService(new RepositorioSolicitudesEnMemoria(), new RepositorioEstudiantesEnMemoria(),
                    responsables, new ValidadorSolicitud(), reloj),
            new ArchivoCsvSolicitudes(responsables), new ReporteSolicitudes(), salida);
}
```

**Por qué es seguro:** la fachada mantiene las firmas, valores de retorno y
mensajes; el golden master compara las 37 líneas de salida del demo.
**Cambios de comportamiento intencionales** (defectos de la versión inicial, ver
[06-pruebas.md](06-pruebas.md) CP12–CP14): numeración por instancia, CSV en UTF-8,
carga que no deja datos parciales.

---

## R5 — Extract Class en la consola

`Main.main` (81 líneas, CC 20) se dividió en `Main` (8 líneas), `ConsolaApp` (un
método por opción del menú) y `EscenarioDemo`. Se agregó `leerEnteroObligatorio`,
que corrige el cierre abrupto ante un id no numérico (S14, CP11).

```java
// antes
System.out.print("Id: ");
int id = Integer.parseInt(sc.nextLine().trim());   // "abc" → NumberFormatException, fin del programa

// después
salida.print("Id: ");
OptionalInt id = leerEnteroObligatorio();          // "abc" → "Error: debe ingresar un numero"
if (id.isEmpty()) {
    return;
}
```

## Cómo ver cada paso

```bash
git log --oneline estado-inicial..estado-refactorizado
git show 2f9b24a --stat     # R1
git diff estado-inicial etapa-refactoring -- src/main
```
