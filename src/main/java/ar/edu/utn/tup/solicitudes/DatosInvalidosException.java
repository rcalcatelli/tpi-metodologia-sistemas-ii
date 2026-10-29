package ar.edu.utn.tup.solicitudes;

/** Los datos ingresados no cumplen las reglas de formato (legajo, email, tipo, etc.). */
public class DatosInvalidosException extends SolicitudException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
