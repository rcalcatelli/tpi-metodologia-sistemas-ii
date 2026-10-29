package ar.edu.utn.tup.solicitudes.reporte;

import ar.edu.utn.tup.solicitudes.Estado;
import ar.edu.utn.tup.solicitudes.Prioridad;
import ar.edu.utn.tup.solicitudes.Solicitud;
import ar.edu.utn.tup.solicitudes.TipoSolicitud;

import java.util.List;
import java.util.function.Predicate;

/** Arma el reporte de texto. Las secciones se generan a partir de los enums. */
public class ReporteSolicitudes {

    private static final String ENCABEZADO = "===== REPORTE DE SOLICITUDES =====";
    private static final String PIE = "==================================";

    public String generar(List<Solicitud> solicitudes) {
        StringBuilder reporte = new StringBuilder();
        linea(reporte, ENCABEZADO);
        linea(reporte, "Total: " + solicitudes.size());
        linea(reporte, "-- Por estado --");
        for (Estado estado : Estado.values()) {
            conteo(reporte, estado.name(), solicitudes, s -> s.getEstado() == estado);
        }
        linea(reporte, "-- Por tipo --");
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            conteo(reporte, tipo.name(), solicitudes, s -> s.getTipo() == tipo);
        }
        linea(reporte, "-- Por prioridad --");
        for (Prioridad prioridad : Prioridad.values()) {
            conteo(reporte, prioridad.name(), solicitudes, s -> s.getPrioridad() == prioridad);
        }
        conteo(reporte, "Sin responsable asignado", solicitudes, s -> s.estaAbierta() && !s.tieneResponsable());
        linea(reporte, "Tasa de resolucion: " + tasaDeResolucion(solicitudes));
        linea(reporte, PIE);
        return reporte.toString();
    }

    private void conteo(StringBuilder reporte, String etiqueta, List<Solicitud> solicitudes, Predicate<Solicitud> criterio) {
        linea(reporte, etiqueta + ": " + contar(solicitudes, criterio));
    }

    private void linea(StringBuilder reporte, String texto) {
        reporte.append(texto).append('\n');
    }

    private long contar(List<Solicitud> solicitudes, Predicate<Solicitud> criterio) {
        return solicitudes.stream().filter(criterio).count();
    }

    /** Porcentaje de resueltas sobre cerradas, con un decimal; "N/A" si no hay cerradas. */
    private String tasaDeResolucion(List<Solicitud> solicitudes) {
        long resueltas = contar(solicitudes, s -> s.getEstado() == Estado.RESUELTA);
        long cerradas = contar(solicitudes, s -> !s.estaAbierta());
        if (cerradas == 0) {
            return "N/A";
        }
        double porcentaje = (double) resueltas / cerradas * 100;
        return Math.round(porcentaje * 10) / 10.0 + "%";
    }
}
