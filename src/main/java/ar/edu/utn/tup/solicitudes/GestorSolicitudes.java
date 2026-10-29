package ar.edu.utn.tup.solicitudes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Gestor principal del sistema. Hace todo: alta de solicitudes, estados,
 * responsables, busquedas, reportes y archivos.
 */
public class GestorSolicitudes {

    private static final int LARGO_MINIMO_LEGAJO = 5;
    private static final int LARGO_MAXIMO_LEGAJO = 6;
    private static final int LARGO_MAXIMO_DESCRIPCION = 500;
    private static final int PRIORIDAD_AUTOMATICA = 0;
    private static final int ERROR = -1;

    static int ultimoId = 0;

    public List<Solicitud> solicitudes = new ArrayList<>();
    public List<Estudiante> estudiantes = new ArrayList<>();
    public List<Responsable> responsables = new ArrayList<>();

    public GestorSolicitudes() {
        ultimoId = 0;
        responsables.add(new Responsable(1, "Laura Gómez", Area.ALUMNADO));
        responsables.add(new Responsable(2, "Martín Díaz", Area.DEPARTAMENTO_ACADEMICO));
        responsables.add(new Responsable(3, "Sofía Pereyra", Area.BEDELIA));
        responsables.add(new Responsable(4, "Javier Ruiz", Area.DEPARTAMENTO_ACADEMICO));
    }

    /** Registra una solicitud nueva. Devuelve el id o -1 si los datos no son válidos. */
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
        solicitudes.add(solicitud);
        System.out.println("Solicitud registrada con id " + solicitud.id);
        return solicitud.id;
    }

    private String validarDatosSolicitud(String legajo, String nombre, String email, String tipo, String descripcion, int prioridad) {
        if (estaVacio(legajo)) {
            return "legajo vacio";
        }
        if (!esLegajoValido(legajo)) {
            return "legajo invalido";
        }
        if (estaVacio(nombre)) {
            return "nombre vacio";
        }
        if (!esEmailValido(email)) {
            return "email invalido";
        }
        if (TipoSolicitud.desdeNombre(tipo).isEmpty()) {
            return "tipo invalido";
        }
        if (estaVacio(descripcion) || descripcion.length() > LARGO_MAXIMO_DESCRIPCION) {
            return "descripcion invalida";
        }
        if (prioridad != PRIORIDAD_AUTOMATICA && Prioridad.desdeCodigo(prioridad).isEmpty()) {
            return "prioridad invalida";
        }
        return null;
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean esLegajoValido(String legajo) {
        return legajo.chars().allMatch(Character::isDigit)
                && legajo.length() >= LARGO_MINIMO_LEGAJO
                && legajo.length() <= LARGO_MAXIMO_LEGAJO;
    }

    private boolean esEmailValido(String email) {
        return email != null && email.contains("@") && email.contains(".") && !email.startsWith("@");
    }

    private boolean tieneSolicitudAbierta(String legajo, TipoSolicitud tipo) {
        return solicitudes.stream().anyMatch(s -> s.est.legajo.equals(legajo) && s.tipo == tipo && !s.estado.esFinal());
    }

    private Estudiante buscarOCrearEstudiante(String legajo, String nombre, String email) {
        Estudiante estudiante = buscarEstudiante(legajo);
        if (estudiante == null) {
            estudiante = new Estudiante(legajo, nombre, email);
            estudiantes.add(estudiante);
        }
        return estudiante;
    }

    private Estudiante buscarEstudiante(String legajo) {
        return estudiantes.stream().filter(e -> e.legajo.equals(legajo)).findFirst().orElse(null);
    }

    private Prioridad resolverPrioridad(TipoSolicitud tipo, int prioridad) {
        if (prioridad == PRIORIDAD_AUTOMATICA) {
            return tipo.getPrioridadPorDefecto();
        }
        return Prioridad.desdeCodigo(prioridad).orElseThrow();
    }

    private Solicitud crearSolicitud(Estudiante estudiante, TipoSolicitud tipo, String descripcion, Prioridad prioridad) {
        Solicitud solicitud = new Solicitud();
        ultimoId++;
        solicitud.id = ultimoId;
        solicitud.est = estudiante;
        solicitud.tipo = tipo;
        solicitud.desc = descripcion;
        solicitud.prioridad = prioridad;
        solicitud.estado = Estado.PENDIENTE;
        solicitud.fecha = LocalDate.now();
        solicitud.obs = "";
        return solicitud;
    }

    public boolean actualizarEmailEstudiante(String legajo, String email) {
        if (!esEmailValido(email)) {
            return informarFallo("email invalido");
        }
        Estudiante estudiante = buscarEstudiante(legajo);
        if (estudiante == null) {
            return informarFallo("estudiante no encontrado");
        }
        estudiante.email = email;
        return true;
    }

    public boolean cambiarEstado(int id, String nuevoEstado, String observacion) {
        Solicitud solicitud = obtener(id);
        if (solicitud == null) {
            return informarFallo("no existe la solicitud " + id);
        }
        if (solicitud.estado.esFinal()) {
            return informarFallo("la solicitud ya esta cerrada");
        }
        Estado destino = Estado.desdeNombre(nuevoEstado).orElse(null);
        if (destino == null || !solicitud.estado.puedePasarA(destino)) {
            return informarFallo("transicion invalida");
        }
        if (destino == Estado.RECHAZADA && estaVacio(observacion)) {
            return informarFallo("para rechazar hay que indicar el motivo");
        }
        if (destino == Estado.RESUELTA && solicitud.resp == null) {
            return informarFallo("no se puede resolver sin responsable");
        }
        solicitud.estado = destino;
        if (destino.esFinal() && observacion != null) {
            solicitud.obs = observacion;
        }
        System.out.println("Solicitud " + id + " ahora esta " + solicitud.estado);
        return true;
    }

    public boolean asignarResponsable(int id, int idResponsable) {
        Solicitud solicitud = obtener(id);
        if (solicitud == null) {
            return informarFallo("no existe la solicitud " + id);
        }
        if (solicitud.estado.esFinal()) {
            return informarFallo("la solicitud ya esta cerrada");
        }
        Responsable responsable = buscarResponsable(idResponsable);
        if (responsable == null) {
            return informarFallo("no existe el responsable " + idResponsable);
        }
        Area area = solicitud.tipo.getAreaResponsable();
        if (responsable.area != area) {
            return informarFallo("el responsable no pertenece al area " + area.getNombre());
        }
        solicitud.resp = responsable;
        if (solicitud.estado == Estado.PENDIENTE) {
            solicitud.estado = Estado.EN_PROCESO;
        }
        System.out.println("Solicitud " + id + " asignada a " + responsable.nombre);
        return true;
    }

    private Responsable buscarResponsable(int id) {
        return responsables.stream().filter(r -> r.id == id).findFirst().orElse(null);
    }

    public Solicitud obtener(int id) {
        return solicitudes.stream().filter(s -> s.id == id).findFirst().orElse(null);
    }

    /** Busca con filtros; null (o 0 en prioridad) significa "no filtrar". */
    public List<Solicitud> buscar(String estado, String tipo, String legajo, int prioridad) {
        return solicitudes.stream()
                .filter(s -> estado == null || s.estado.name().equals(estado))
                .filter(s -> tipo == null || s.tipo.name().equals(tipo))
                .filter(s -> legajo == null || s.est.legajo.equals(legajo))
                .filter(s -> prioridad == 0 || s.prioridad.getCodigo() == prioridad)
                .sorted(Comparator.comparing((Solicitud s) -> s.prioridad).thenComparingInt(s -> s.id))
                .toList();
    }

    public String generarReporte() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("===== REPORTE DE SOLICITUDES =====\n");
        reporte.append("Total: ").append(solicitudes.size()).append("\n");
        reporte.append("-- Por estado --\n");
        for (Estado estado : Estado.values()) {
            agregarConteo(reporte, estado.name(), s -> s.estado == estado);
        }
        reporte.append("-- Por tipo --\n");
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            agregarConteo(reporte, tipo.name(), s -> s.tipo == tipo);
        }
        reporte.append("-- Por prioridad --\n");
        for (Prioridad prioridad : Prioridad.values()) {
            agregarConteo(reporte, prioridad.name(), s -> s.prioridad == prioridad);
        }
        agregarConteo(reporte, "Sin responsable asignado", s -> s.resp == null && !s.estado.esFinal());
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
        long resueltas = contar(s -> s.estado == Estado.RESUELTA);
        long cerradas = resueltas + contar(s -> s.estado == Estado.RECHAZADA);
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
                String idR = "";
                if (s.resp != null) idR = "" + s.resp.id;
                pw.println(s.id + ";" + s.est.legajo + ";" + s.est.nombre + ";" + s.est.email + ";" + s.tipo + ";"
                        + s.desc.replace(";", ",") + ";" + s.prioridad.getCodigo() + ";" + s.estado + ";" + idR + ";" + s.fecha + ";"
                        + s.obs.replace(";", ","));
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
                Solicitud s = new Solicitud();
                s.id = Integer.parseInt(p[0]);
                s.est = buscarOCrearEstudiante(p[1], p[2], p[3]);
                s.tipo = TipoSolicitud.valueOf(p[4]);
                s.desc = p[5];
                s.prioridad = Prioridad.desdeCodigo(Integer.parseInt(p[6])).orElseThrow();
                s.estado = Estado.valueOf(p[7]);
                if (!p[8].equals("")) {
                    s.resp = buscarResponsable(Integer.parseInt(p[8]));
                }
                s.fecha = LocalDate.parse(p[9]);
                s.obs = p[10];
                solicitudes.add(s);
                if (s.id > max) max = s.id;
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
        return responsables;
    }

    private int informarError(String mensaje) {
        System.out.println("Error: " + mensaje);
        return ERROR;
    }

    private boolean informarFallo(String mensaje) {
        System.out.println("Error: " + mensaje);
        return false;
    }
}
