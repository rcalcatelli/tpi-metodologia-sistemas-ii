package ar.edu.utn.tup.solicitudes;

import java.util.Arrays;
import java.util.Optional;

/**
 * Cada tipo de solicitud conoce su prioridad por defecto y el área que la atiende.
 * Agregar un tipo nuevo es agregar una constante aquí.
 */
public enum TipoSolicitud {
    CERTIFICADO(Prioridad.BAJA, Area.ALUMNADO),
    EQUIVALENCIA(Prioridad.MEDIA, Area.DEPARTAMENTO_ACADEMICO),
    REVISION_NOTA(Prioridad.ALTA, Area.DEPARTAMENTO_ACADEMICO),
    BAJA_MATERIA(Prioridad.MEDIA, Area.BEDELIA);

    private final Prioridad prioridadPorDefecto;
    private final Area areaResponsable;

    TipoSolicitud(Prioridad prioridadPorDefecto, Area areaResponsable) {
        this.prioridadPorDefecto = prioridadPorDefecto;
        this.areaResponsable = areaResponsable;
    }

    public Prioridad getPrioridadPorDefecto() {
        return prioridadPorDefecto;
    }

    public Area getAreaResponsable() {
        return areaResponsable;
    }

    public static Optional<TipoSolicitud> desdeNombre(String nombre) {
        return Arrays.stream(values()).filter(tipo -> tipo.name().equals(nombre)).findFirst();
    }
}
