package ar.edu.utn.tup.solicitudes;

/** La solicitud, el estudiante o el responsable buscado no existe. */
public class RecursoNoEncontradoException extends SolicitudException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
