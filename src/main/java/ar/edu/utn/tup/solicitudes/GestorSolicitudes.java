package ar.edu.utn.tup.solicitudes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Gestor principal del sistema. Hace todo: alta de solicitudes, estados,
 * responsables, busquedas, reportes y archivos.
 */
public class GestorSolicitudes {

    private static final int LARGO_MINIMO_LEGAJO = 5;
    private static final int LARGO_MAXIMO_LEGAJO = 6;
    private static final int LARGO_MAXIMO_DESCRIPCION = 500;
    private static final int ERROR = -1;

    static int ultimoId = 0;

    private final List<Solicitud> solicitudes = new ArrayList<>();
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final List<Responsable> responsables = new ArrayList<>();

    public GestorSolicitudes() {
        ultimoId = 0;
        responsables.add(new Responsable(1, "Laura Gómez", Area.ALUMNADO));
        responsables.add(new Responsable(2, "Martín Díaz", Area.DEPARTAMENTO_ACADEMICO));
        responsables.add(new Responsable(3, "Sofía Pereyra", Area.BEDELIA));
        responsables.add(new Responsable(4, "Javier Ruiz", Area.DEPARTAMENTO_ACADEMICO));
    }

    /** Se mantiene por compatibilidad; delega en {@link #registrarSolicitud(DatosSolicitud)}. */
    public int registrarSolicitud(String legajo, String nombre, String email, String tipo, String descripcion, int prioridad) {
        return registrarSolicitud(new DatosSolicitud(legajo, nombre, email, tipo, descripcion, prioridad));
    }

    /** Registra una solicitud nueva. Devuelve el id o -1 si los datos no son válidos. */
    public int registrarSolicitud(DatosSolicitud datos) {
        try {
            Solicitud solicitud = registrar(datos);
            System.out.println("Solicitud registrada con id " + solicitud.getId());
            return solicitud.getId();
        } catch (SolicitudException e) {
            informarError(e);
            return ERROR;
        }
    }

    private Solicitud registrar(DatosSolicitud datos) {
        validar(datos);
        TipoSolicitud tipo = TipoSolicitud.valueOf(datos.tipo());
        if (tieneSolicitudAbierta(datos.legajo(), tipo)) {
            throw new ReglaNegocioException("ya existe una solicitud abierta de ese tipo");
        }
        Estudiante estudiante = buscarOCrearEstudiante(datos.legajo(), datos.nombre(), datos.email());
        ultimoId++;
        Solicitud solicitud = new Solicitud(ultimoId, estudiante, tipo, datos.descripcion(), resolverPrioridad(datos, tipo), LocalDate.now());
        solicitudes.add(solicitud);
        return solicitud;
    }

    private void validar(DatosSolicitud datos) {
        if (estaVacio(datos.legajo())) {
            throw new DatosInvalidosException("legajo vacio");
        }
        if (!esLegajoValido(datos.legajo())) {
            throw new DatosInvalidosException("legajo invalido");
        }
        if (estaVacio(datos.nombre())) {
            throw new DatosInvalidosException("nombre vacio");
        }
        validarEmail(datos.email());
        if (TipoSolicitud.desdeNombre(datos.tipo()).isEmpty()) {
            throw new DatosInvalidosException("tipo invalido");
        }
        if (estaVacio(datos.descripcion()) || datos.descripcion().length() > LARGO_MAXIMO_DESCRIPCION) {
            throw new DatosInvalidosException("descripcion invalida");
        }
        if (!datos.usaPrioridadAutomatica() && Prioridad.desdeCodigo(datos.prioridad()).isEmpty()) {
            throw new DatosInvalidosException("prioridad invalida");
        }
    }

    private void validarEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".") || email.startsWith("@")) {
            throw new DatosInvalidosException("email invalido");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean esLegajoValido(String legajo) {
        return legajo.chars().allMatch(Character::isDigit)
                && legajo.length() >= LARGO_MINIMO_LEGAJO
                && legajo.length() <= LARGO_MAXIMO_LEGAJO;
    }

    private boolean tieneSolicitudAbierta(String legajo, TipoSolicitud tipo) {
        return solicitudes.stream().anyMatch(s -> s.perteneceA(legajo) && s.getTipo() == tipo && s.estaAbierta());
    }

    private Estudiante buscarOCrearEstudiante(String legajo, String nombre, String email) {
        return buscarEstudiante(legajo).orElseGet(() -> {
            Estudiante nuevo = new Estudiante(legajo, nombre, email);
            estudiantes.add(nuevo);
            return nuevo;
        });
    }

    private Optional<Estudiante> buscarEstudiante(String legajo) {
        return estudiantes.stream().filter(e -> e.getLegajo().equals(legajo)).findFirst();
    }

    private Prioridad resolverPrioridad(DatosSolicitud datos, TipoSolicitud tipo) {
        if (datos.usaPrioridadAutomatica()) {
            return tipo.getPrioridadPorDefecto();
        }
        return Prioridad.desdeCodigo(datos.prioridad()).orElseThrow();
    }

    public boolean actualizarEmailEstudiante(String legajo, String email) {
        return ejecutar(() -> {
            validarEmail(email);
            buscarEstudiante(legajo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("estudiante no encontrado"))
                    .cambiarEmail(email);
        }, null);
    }

    public boolean cambiarEstado(int id, String nuevoEstado, String observacion) {
        return ejecutar(() -> {
            Solicitud solicitud = buscarSolicitud(id);
            solicitud.cambiarEstado(Estado.desdeNombre(nuevoEstado).orElse(null), observacion);
        }, () -> "Solicitud " + id + " ahora esta " + obtener(id).getEstado());
    }

    public boolean asignarResponsable(int id, int idResponsable) {
        return ejecutar(() -> {
            Solicitud solicitud = buscarSolicitud(id);
            solicitud.verificarAbierta();
            solicitud.asignar(buscarResponsable(idResponsable));
        }, () -> "Solicitud " + id + " asignada a " + obtener(id).getResponsable().getNombre());
    }

    private Solicitud buscarSolicitud(int id) {
        Solicitud solicitud = obtener(id);
        if (solicitud == null) {
            throw new RecursoNoEncontradoException("no existe la solicitud " + id);
        }
        return solicitud;
    }

    private Responsable buscarResponsable(int id) {
        return responsables.stream().filter(r -> r.getId() == id).findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe el responsable " + id));
    }

    public Solicitud obtener(int id) {
        return solicitudes.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    /** Se mantiene por compatibilidad; null (o 0 en prioridad) significa "no filtrar". */
    public List<Solicitud> buscar(String estado, String tipo, String legajo, int prioridad) {
        Optional<Estado> estadoBuscado = Estado.desdeNombre(estado);
        Optional<TipoSolicitud> tipoBuscado = TipoSolicitud.desdeNombre(tipo);
        Optional<Prioridad> prioridadBuscada = Prioridad.desdeCodigo(prioridad);
        boolean criterioInexistente = (estado != null && estadoBuscado.isEmpty())
                || (tipo != null && tipoBuscado.isEmpty())
                || (prioridad != 0 && prioridadBuscada.isEmpty());
        if (criterioInexistente) {
            return List.of();
        }
        return buscar(new FiltroSolicitudes(estadoBuscado.orElse(null), tipoBuscado.orElse(null), legajo, prioridadBuscada.orElse(null)));
    }

    public List<Solicitud> buscar(FiltroSolicitudes filtro) {
        return solicitudes.stream()
                .filter(filtro::acepta)
                .sorted(Comparator.comparing(Solicitud::getPrioridad).thenComparingInt(Solicitud::getId))
                .toList();
    }

    public String generarReporte() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("===== REPORTE DE SOLICITUDES =====\n");
        reporte.append("Total: ").append(solicitudes.size()).append("\n");
        reporte.append("-- Por estado --\n");
        for (Estado estado : Estado.values()) {
            agregarConteo(reporte, estado.name(), s -> s.getEstado() == estado);
        }
        reporte.append("-- Por tipo --\n");
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            agregarConteo(reporte, tipo.name(), s -> s.getTipo() == tipo);
        }
        reporte.append("-- Por prioridad --\n");
        for (Prioridad prioridad : Prioridad.values()) {
            agregarConteo(reporte, prioridad.name(), s -> s.getPrioridad() == prioridad);
        }
        agregarConteo(reporte, "Sin responsable asignado", s -> !s.tieneResponsable() && s.estaAbierta());
        reporte.append("Tasa de resolucion: ").append(tasaDeResolucion()).append("\n");
        reporte.append("==================================\n");
        return reporte.toString();
    }

    private void agregarConteo(StringBuilder reporte, String etiqueta, Predicate<Solicitud> criterio) {
        reporte.append(etiqueta).append(": ").append(contar(criterio)).append("\n");
    }

    private long contar(Predicate<Solicitud> criterio) {
        return solicitudes.stream().filter(criterio).count();
    }

    private String tasaDeResolucion() {
        long resueltas = contar(s -> s.getEstado() == Estado.RESUELTA);
        long cerradas = resueltas + contar(s -> s.getEstado() == Estado.RECHAZADA);
        if (cerradas == 0) {
            return "N/A";
        }
        double porcentaje = (double) resueltas / cerradas * 100;
        return Math.round(porcentaje * 10) / 10.0 + "%";
    }

    public boolean guardar(String ruta) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(ruta));
            for (Solicitud s : solicitudes) {
                String idR = s.tieneResponsable() ? String.valueOf(s.getResponsable().getId()) : "";
                Estudiante e = s.getEstudiante();
                pw.println(s.getId() + ";" + e.getLegajo() + ";" + e.getNombre() + ";" + e.getEmail() + ";" + s.getTipo() + ";"
                        + s.getDescripcion().replace(";", ",") + ";" + s.getPrioridad().getCodigo() + ";" + s.getEstado() + ";"
                        + idR + ";" + s.getFechaAlta() + ";" + s.getObservacion().replace(";", ","));
            }
            pw.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error al guardar");
            return false;
        }
    }

    public boolean cargar(String ruta) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            solicitudes.clear();
            estudiantes.clear();
            String l;
            int max = 0;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(";", -1);
                Responsable responsable = p[8].isEmpty() ? null : buscarResponsable(Integer.parseInt(p[8]));
                Solicitud s = Solicitud.restaurar(Integer.parseInt(p[0]), buscarOCrearEstudiante(p[1], p[2], p[3]),
                        TipoSolicitud.valueOf(p[4]), p[5], Prioridad.desdeCodigo(Integer.parseInt(p[6])).orElseThrow(),
                        LocalDate.parse(p[9]), Estado.valueOf(p[7]), responsable, p[10]);
                solicitudes.add(s);
                max = Math.max(max, s.getId());
            }
            br.close();
            ultimoId = max;
            return true;
        } catch (Exception e) {
            System.out.println("Error al cargar");
            return false;
        }
    }

    public List<Responsable> getResponsables() {
        return List.copyOf(responsables);
    }

    private boolean ejecutar(Runnable operacion, java.util.function.Supplier<String> mensajeExito) {
        try {
            operacion.run();
        } catch (SolicitudException e) {
            informarError(e);
            return false;
        }
        if (mensajeExito != null) {
            System.out.println(mensajeExito.get());
        }
        return true;
    }

    private void informarError(SolicitudException e) {
        System.out.println("Error: " + e.getMessage());
    }
}
