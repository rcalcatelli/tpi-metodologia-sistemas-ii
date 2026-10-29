package ar.edu.utn.tup.solicitudes;

/**
 * Criterios de búsqueda. Un criterio nulo significa "no filtrar por ese campo".
 * Reemplaza los cuatro parámetros de buscar y el uso de null/0 como comodín.
 */
public record FiltroSolicitudes(Estado estado, TipoSolicitud tipo, String legajo, Prioridad prioridad) {

    public static FiltroSolicitudes todas() {
        return new FiltroSolicitudes(null, null, null, null);
    }

    public FiltroSolicitudes conEstado(Estado nuevoEstado) {
        return new FiltroSolicitudes(nuevoEstado, tipo, legajo, prioridad);
    }

    public FiltroSolicitudes conTipo(TipoSolicitud nuevoTipo) {
        return new FiltroSolicitudes(estado, nuevoTipo, legajo, prioridad);
    }

    public FiltroSolicitudes conLegajo(String nuevoLegajo) {
        return new FiltroSolicitudes(estado, tipo, nuevoLegajo, prioridad);
    }

    public FiltroSolicitudes conPrioridad(Prioridad nuevaPrioridad) {
        return new FiltroSolicitudes(estado, tipo, legajo, nuevaPrioridad);
    }

    public boolean acepta(Solicitud solicitud) {
        return (estado == null || solicitud.getEstado() == estado)
                && (tipo == null || solicitud.getTipo() == tipo)
                && (legajo == null || solicitud.perteneceA(legajo))
                && (prioridad == null || solicitud.getPrioridad() == prioridad);
    }
}
