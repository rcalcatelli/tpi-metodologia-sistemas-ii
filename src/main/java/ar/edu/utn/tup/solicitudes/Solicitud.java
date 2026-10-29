package ar.edu.utn.tup.solicitudes;

import java.time.LocalDate;

// clase de datos de la solicitud
public class Solicitud {
    public int id;
    public Estudiante est;
    public TipoSolicitud tipo;
    public String desc;
    public Prioridad prioridad;
    public Estado estado;
    public Responsable resp;
    public LocalDate fecha;
    public String obs;

    public Solicitud() {
    }

    public String toString() {
        String r = "#" + id + " [" + estado + "] " + tipo + " - " + est.nombre + " (" + est.legajo + ")";
        r = r + " - Prioridad: " + prioridad;
        if (resp != null) {
            r = r + " - Resp: " + resp.nombre;
        } else {
            r = r + " - Resp: (sin asignar)";
        }
        return r;
    }
}
