package ar.edu.utn.tup.solicitudes;

import java.util.Arrays;
import java.util.Optional;

/**
 * Ciclo de vida de una solicitud: PENDIENTE → EN_PROCESO → RESUELTA / RECHAZADA.
 * También se puede rechazar directamente una solicitud pendiente.
 */
public enum Estado {
    PENDIENTE,
    EN_PROCESO,
    RESUELTA,
    RECHAZADA;

    public boolean esFinal() {
        return this == RESUELTA || this == RECHAZADA;
    }

    public boolean puedePasarA(Estado destino) {
        return switch (this) {
            case PENDIENTE -> destino == EN_PROCESO || destino == RECHAZADA;
            case EN_PROCESO -> destino == RESUELTA || destino == RECHAZADA;
            case RESUELTA, RECHAZADA -> false;
        };
    }

    public static Optional<Estado> desdeNombre(String nombre) {
        return Arrays.stream(values()).filter(estado -> estado.name().equals(nombre)).findFirst();
    }
}
