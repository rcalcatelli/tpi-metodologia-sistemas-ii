package ar.edu.utn.tup.solicitudes;

/** Error de negocio o de datos del sistema de solicitudes. Su mensaje es apto para mostrar al usuario. */
public abstract class SolicitudException extends RuntimeException {

    public SolicitudException(String mensaje) {
        super(mensaje);
    }
}
