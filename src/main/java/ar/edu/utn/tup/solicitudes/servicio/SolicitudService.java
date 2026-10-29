package ar.edu.utn.tup.solicitudes.servicio;

import ar.edu.utn.tup.solicitudes.DatosSolicitud;
import ar.edu.utn.tup.solicitudes.Estado;
import ar.edu.utn.tup.solicitudes.Estudiante;
import ar.edu.utn.tup.solicitudes.FiltroSolicitudes;
import ar.edu.utn.tup.solicitudes.Prioridad;
import ar.edu.utn.tup.solicitudes.RecursoNoEncontradoException;
import ar.edu.utn.tup.solicitudes.ReglaNegocioException;
import ar.edu.utn.tup.solicitudes.Responsable;
import ar.edu.utn.tup.solicitudes.Solicitud;
import ar.edu.utn.tup.solicitudes.TipoSolicitud;
import ar.edu.utn.tup.solicitudes.persistencia.EstudianteRepository;
import ar.edu.utn.tup.solicitudes.persistencia.ResponsableRepository;
import ar.edu.utn.tup.solicitudes.persistencia.SolicitudRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Casos de uso del sistema. Coordina validación, repositorios y entidades;
 * las reglas de cada solicitud viven en la propia entidad.
 */
public class SolicitudService {

    private static final Comparator<Solicitud> POR_PRIORIDAD_E_ID =
            Comparator.comparing(Solicitud::getPrioridad).thenComparingInt(Solicitud::getId);

    private final SolicitudRepository solicitudes;
    private final EstudianteRepository estudiantes;
    private final ResponsableRepository responsables;
    private final ValidadorSolicitud validador;
    private final Clock reloj;

    public SolicitudService(SolicitudRepository solicitudes, EstudianteRepository estudiantes,
                            ResponsableRepository responsables, ValidadorSolicitud validador, Clock reloj) {
        this.solicitudes = solicitudes;
        this.estudiantes = estudiantes;
        this.responsables = responsables;
        this.validador = validador;
        this.reloj = reloj;
    }

    public Solicitud registrar(DatosSolicitud datos) {
        validador.validar(datos);
        TipoSolicitud tipo = TipoSolicitud.valueOf(datos.tipo());
        verificarSinSolicitudAbierta(datos.legajo(), tipo);
        Solicitud solicitud = new Solicitud(solicitudes.siguienteId(), buscarOCrearEstudiante(datos), tipo,
                datos.descripcion(), resolverPrioridad(datos, tipo), LocalDate.now(reloj));
        solicitudes.guardar(solicitud);
        return solicitud;
    }

    public Solicitud cambiarEstado(int idSolicitud, Estado destino, String observacion) {
        Solicitud solicitud = obtener(idSolicitud);
        solicitud.cambiarEstado(destino, observacion);
        return solicitud;
    }

    public Solicitud asignarResponsable(int idSolicitud, int idResponsable) {
        Solicitud solicitud = obtener(idSolicitud);
        solicitud.verificarAbierta();
        solicitud.asignar(obtenerResponsable(idResponsable));
        return solicitud;
    }

    public void actualizarEmail(String legajo, String nuevoEmail) {
        validador.validarEmail(nuevoEmail);
        estudiantes.buscarPorLegajo(legajo)
                .orElseThrow(() -> new RecursoNoEncontradoException("estudiante no encontrado"))
                .cambiarEmail(nuevoEmail);
    }

    public Optional<Solicitud> buscarPorId(int id) {
        return solicitudes.buscarPorId(id);
    }

    /** Solicitudes que cumplen el filtro, ordenadas por prioridad y luego por id. */
    public List<Solicitud> buscar(FiltroSolicitudes filtro) {
        return solicitudes.todas().stream().filter(filtro::acepta).sorted(POR_PRIORIDAD_E_ID).toList();
    }

    /** Todas las solicitudes en orden de alta. */
    public List<Solicitud> todas() {
        return solicitudes.todas();
    }

    public List<Responsable> responsables() {
        return responsables.todos();
    }

    /** Reemplaza los datos en memoria (por ejemplo, al cargar un archivo). */
    public void reemplazarDatos(List<Solicitud> nuevasSolicitudes) {
        solicitudes.reemplazarTodas(nuevasSolicitudes);
        Map<String, Estudiante> estudiantesPorLegajo = new LinkedHashMap<>();
        nuevasSolicitudes.forEach(s -> estudiantesPorLegajo.putIfAbsent(s.getEstudiante().getLegajo(), s.getEstudiante()));
        estudiantes.reemplazarTodos(estudiantesPorLegajo.values());
    }

    private Solicitud obtener(int id) {
        return solicitudes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe la solicitud " + id));
    }

    private Responsable obtenerResponsable(int id) {
        return responsables.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe el responsable " + id));
    }

    private void verificarSinSolicitudAbierta(String legajo, TipoSolicitud tipo) {
        boolean existeAbierta = solicitudes.todas().stream()
                .anyMatch(s -> s.perteneceA(legajo) && s.getTipo() == tipo && s.estaAbierta());
        if (existeAbierta) {
            throw new ReglaNegocioException("ya existe una solicitud abierta de ese tipo");
        }
    }

    private Estudiante buscarOCrearEstudiante(DatosSolicitud datos) {
        return estudiantes.buscarPorLegajo(datos.legajo()).orElseGet(() -> {
            Estudiante nuevo = new Estudiante(datos.legajo(), datos.nombre(), datos.email());
            estudiantes.guardar(nuevo);
            return nuevo;
        });
    }

    private Prioridad resolverPrioridad(DatosSolicitud datos, TipoSolicitud tipo) {
        if (datos.usaPrioridadAutomatica()) {
            return tipo.getPrioridadPorDefecto();
        }
        return Prioridad.desdeCodigo(datos.prioridad()).orElseThrow();
    }
}
