package ar.edu.utn.tup.solicitudes;

/**
 * Datos de entrada para registrar una solicitud, tal como los ingresa el usuario.
 * Reemplaza la lista de seis parámetros de registrarSolicitud.
 *
 * @param prioridad 1 alta, 2 media, 3 baja o {@link #PRIORIDAD_AUTOMATICA}
 */
public record DatosSolicitud(String legajo, String nombre, String email, String tipo, String descripcion, int prioridad) {

    public static final int PRIORIDAD_AUTOMATICA = 0;

    public boolean usaPrioridadAutomatica() {
        return prioridad == PRIORIDAD_AUTOMATICA;
    }
}
