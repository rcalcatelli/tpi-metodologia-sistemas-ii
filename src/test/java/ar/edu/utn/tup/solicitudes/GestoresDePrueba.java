package ar.edu.utn.tup.solicitudes;

import ar.edu.utn.tup.solicitudes.persistencia.CatalogoResponsables;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Construcción común de gestores para los tests unitarios: reloj fijo y salida controlada. */
public final class GestoresDePrueba {

    public static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);

    private GestoresDePrueba() {
    }

    public static GestorSolicitudes conSalida(PrintStream salida) {
        return new GestorSolicitudes(new CatalogoResponsables(), RELOJ_FIJO, salida);
    }

    public static GestorSolicitudes silencioso() {
        return conSalida(new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8));
    }
}
