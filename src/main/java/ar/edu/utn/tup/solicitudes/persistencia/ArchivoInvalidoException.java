package ar.edu.utn.tup.solicitudes.persistencia;

import java.io.IOException;

/** El archivo existe pero su contenido no respeta el formato esperado. */
public class ArchivoInvalidoException extends IOException {

    public ArchivoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
