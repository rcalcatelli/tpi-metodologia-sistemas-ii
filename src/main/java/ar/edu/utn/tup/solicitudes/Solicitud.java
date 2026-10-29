package ar.edu.utn.tup.solicitudes;

import java.time.LocalDate;

/**
 * Solicitud académica. Protege sus invariantes: el estado solo cambia mediante
 * transiciones válidas y el responsable debe pertenecer al área del tipo.
 */
public class Solicitud {

    private final int id;
    private final Estudiante estudiante;
    private final TipoSolicitud tipo;
    private final String descripcion;
    private final Prioridad prioridad;
    private final LocalDate fechaAlta;
    private Estado estado;
    private Responsable responsable;
    private String observacion;

    public Solicitud(int id, Estudiante estudiante, TipoSolicitud tipo, String descripcion, Prioridad prioridad, LocalDate fechaAlta) {
        this(id, estudiante, tipo, descripcion, prioridad, fechaAlta, Estado.PENDIENTE, null, "");
    }

    private Solicitud(int id, Estudiante estudiante, TipoSolicitud tipo, String descripcion, Prioridad prioridad,
                      LocalDate fechaAlta, Estado estado, Responsable responsable, String observacion) {
        this.id = id;
        this.estudiante = estudiante;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.fechaAlta = fechaAlta;
        this.estado = estado;
        this.responsable = responsable;
        this.observacion = observacion;
    }

    /** Reconstruye una solicitud ya existente (por ejemplo, leída de un archivo). */
    public static Solicitud restaurar(int id, Estudiante estudiante, TipoSolicitud tipo, String descripcion, Prioridad prioridad,
                                      LocalDate fechaAlta, Estado estado, Responsable responsable, String observacion) {
        return new Solicitud(id, estudiante, tipo, descripcion, prioridad, fechaAlta, estado, responsable, observacion);
    }

    public void cambiarEstado(Estado destino, String observacion) {
        verificarAbierta();
        if (destino == null || !estado.puedePasarA(destino)) {
            throw new ReglaNegocioException("transicion invalida");
        }
        if (destino == Estado.RECHAZADA && (observacion == null || observacion.isBlank())) {
            throw new ReglaNegocioException("para rechazar hay que indicar el motivo");
        }
        if (destino == Estado.RESUELTA && responsable == null) {
            throw new ReglaNegocioException("no se puede resolver sin responsable");
        }
        estado = destino;
        if (destino.esFinal() && observacion != null) {
            this.observacion = observacion;
        }
    }

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

    public void verificarAbierta() {
        if (!estaAbierta()) {
            throw new ReglaNegocioException("la solicitud ya esta cerrada");
        }
    }

    public boolean estaAbierta() {
        return !estado.esFinal();
    }

    public boolean tieneResponsable() {
        return responsable != null;
    }

    public boolean perteneceA(String legajo) {
        return estudiante.getLegajo().equals(legajo);
    }

    public int getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public TipoSolicitud getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public Estado getEstado() {
        return estado;
    }

    public Responsable getResponsable() {
        return responsable;
    }

    public String getObservacion() {
        return observacion;
    }

    @Override
    public String toString() {
        String nombreResponsable = tieneResponsable() ? responsable.getNombre() : "(sin asignar)";
        return "#" + id + " [" + estado + "] " + tipo + " - " + estudiante.getNombre() + " (" + estudiante.getLegajo() + ")"
                + " - Prioridad: " + prioridad + " - Resp: " + nombreResponsable;
    }
}
