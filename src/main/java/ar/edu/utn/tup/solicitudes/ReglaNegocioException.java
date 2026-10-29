package ar.edu.utn.tup.solicitudes;

/** La operación viola una regla del ciclo de vida de la solicitud. */
public class ReglaNegocioException extends SolicitudException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
