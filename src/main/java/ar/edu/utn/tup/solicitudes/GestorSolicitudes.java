package ar.edu.utn.tup.solicitudes;

import ar.edu.utn.tup.solicitudes.persistencia.ArchivoCsvSolicitudes;
import ar.edu.utn.tup.solicitudes.persistencia.CatalogoResponsables;
import ar.edu.utn.tup.solicitudes.persistencia.RepositorioEstudiantesEnMemoria;
import ar.edu.utn.tup.solicitudes.persistencia.RepositorioSolicitudesEnMemoria;
import ar.edu.utn.tup.solicitudes.persistencia.ResponsableRepository;
import ar.edu.utn.tup.solicitudes.reporte.ReporteSolicitudes;
import ar.edu.utn.tup.solicitudes.servicio.SolicitudService;
import ar.edu.utn.tup.solicitudes.servicio.ValidadorSolicitud;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Fachada para la interfaz de consola. Conserva la API pública de la versión
 * inicial (ids, -1 y true/false) y traduce las excepciones del dominio a
 * mensajes para el usuario. La lógica vive en {@link SolicitudService}.
 */
public class GestorSolicitudes {

    private static final int ERROR = -1;

    private final SolicitudService servicio;
    private final ArchivoCsvSolicitudes archivo;
    private final ReporteSolicitudes reporte;
    private final PrintStream salida;

    public GestorSolicitudes() {
        this(new CatalogoResponsables(), Clock.systemDefaultZone(), System.out);
    }

    public GestorSolicitudes(ResponsableRepository responsables, Clock reloj, PrintStream salida) {
        this(new SolicitudService(new RepositorioSolicitudesEnMemoria(), new RepositorioEstudiantesEnMemoria(),
                        responsables, new ValidadorSolicitud(), reloj),
                new ArchivoCsvSolicitudes(responsables), new ReporteSolicitudes(), salida);
    }

    public GestorSolicitudes(SolicitudService servicio, ArchivoCsvSolicitudes archivo, ReporteSolicitudes reporte, PrintStream salida) {
        this.servicio = servicio;
        this.archivo = archivo;
        this.reporte = reporte;
        this.salida = salida;
    }

    /** Se mantiene por compatibilidad; delega en {@link #registrarSolicitud(DatosSolicitud)}. */
    public int registrarSolicitud(String legajo, String nombre, String email, String tipo, String descripcion, int prioridad) {
        return registrarSolicitud(new DatosSolicitud(legajo, nombre, email, tipo, descripcion, prioridad));
    }

    /** Registra una solicitud nueva. Devuelve el id o -1 si los datos no son válidos. */
    public int registrarSolicitud(DatosSolicitud datos) {
        try {
            Solicitud solicitud = servicio.registrar(datos);
            salida.println("Solicitud registrada con id " + solicitud.getId());
            return solicitud.getId();
        } catch (SolicitudException e) {
            informarError(e);
            return ERROR;
        }
    }

    public boolean cambiarEstado(int id, String nuevoEstado, String observacion) {
        Estado destino = Estado.desdeNombre(nuevoEstado).orElse(null);
        return ejecutar(() -> servicio.cambiarEstado(id, destino, observacion),
                solicitud -> "Solicitud " + id + " ahora esta " + solicitud.getEstado());
    }

    public boolean asignarResponsable(int id, int idResponsable) {
        return ejecutar(() -> servicio.asignarResponsable(id, idResponsable),
                solicitud -> "Solicitud " + id + " asignada a " + solicitud.getResponsable().getNombre());
    }

    public boolean actualizarEmailEstudiante(String legajo, String email) {
        try {
            servicio.actualizarEmail(legajo, email);
            return true;
        } catch (SolicitudException e) {
            informarError(e);
            return false;
        }
    }

    /** Devuelve la solicitud o null si no existe (contrato de la versión inicial). */
    public Solicitud obtener(int id) {
        return servicio.buscarPorId(id).orElse(null);
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
        return buscar(new FiltroSolicitudes(estadoBuscado.orElse(null), tipoBuscado.orElse(null), legajo,
                prioridadBuscada.orElse(null)));
    }

    public List<Solicitud> buscar(FiltroSolicitudes filtro) {
        return servicio.buscar(filtro);
    }

    public String generarReporte() {
        return reporte.generar(servicio.todas());
    }

    public boolean guardar(String ruta) {
        try {
            archivo.guardar(servicio.todas(), Path.of(ruta));
            return true;
        } catch (IOException e) {
            salida.println("Error al guardar");
            return false;
        }
    }

    public boolean cargar(String ruta) {
        try {
            servicio.reemplazarDatos(archivo.cargar(Path.of(ruta)));
            return true;
        } catch (IOException e) {
            salida.println("Error al cargar");
            return false;
        }
    }

    public List<Responsable> getResponsables() {
        return servicio.responsables();
    }

    private boolean ejecutar(OperacionSobreSolicitud operacion, Function<Solicitud, String> mensajeDeExito) {
        try {
            Solicitud solicitud = operacion.ejecutar();
            salida.println(mensajeDeExito.apply(solicitud));
            return true;
        } catch (SolicitudException e) {
            informarError(e);
            return false;
        }
    }

    private void informarError(SolicitudException e) {
        salida.println("Error: " + e.getMessage());
    }

    @FunctionalInterface
    private interface OperacionSobreSolicitud {
        Solicitud ejecutar();
    }
}
