package ar.edu.utn.tup.solicitudes;

import java.time.LocalDate;

// clase de datos de la solicitud
public class Solicitud {
    public int id;
    public Estudiante est;
    public String tipo;       // "CERTIFICADO", "EQUIVALENCIA", "REVISION_NOTA", "BAJA_MATERIA"
    public String desc;
    public int prioridad;     // 1 = alta, 2 = media, 3 = baja
    public String estado;     // "PENDIENTE", "EN_PROCESO", "RESUELTA", "RECHAZADA"
    public Responsable resp;
    public LocalDate fecha;
    public String obs;

    public Solicitud() {
    }

    public String toString() {
        String r = "#" + id + " [" + estado + "] " + tipo + " - " + est.nombre + " (" + est.legajo + ")";
        if (prioridad == 1) {
            r = r + " - Prioridad: ALTA";
        } else if (prioridad == 2) {
            r = r + " - Prioridad: MEDIA";
        } else {
            r = r + " - Prioridad: BAJA";
        }
        if (resp != null) {
            r = r + " - Resp: " + resp.nombre;
        } else {
            r = r + " - Resp: (sin asignar)";
        }
        return r;
    }
}
