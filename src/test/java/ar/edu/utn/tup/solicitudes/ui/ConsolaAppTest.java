package ar.edu.utn.tup.solicitudes.ui;

import ar.edu.utn.tup.solicitudes.GestorSolicitudes;
import ar.edu.utn.tup.solicitudes.persistencia.CatalogoResponsables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ConsolaApp")
class ConsolaAppTest {

    private String ejecutar(String entrada) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        GestorSolicitudes gestor = new GestorSolicitudes(new CatalogoResponsables(), Clock.systemDefaultZone(), salida);
        new ConsolaApp(gestor, new Scanner(entrada), salida).ejecutar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void idNoNumericoNoTerminaElPrograma() {
        String salida = ejecutar("2\nabc\n3\nxyz\n5\n0\n");

        assertTrue(salida.contains("Error: debe ingresar un numero"), salida);
        assertTrue(salida.contains("===== REPORTE DE SOLICITUDES ====="), salida);
    }

    @Test
    void flujoCompletoDesdeElMenu() {
        String salida = ejecutar(String.join("\n",
                "1", "10001", "Ana Torres", "ana@example.com", "certificado", "Constancia", "0",
                "3", "1", "1",
                "2", "1", "RESUELTA", "Emitido",
                "4", "", "", "10001",
                "0", ""));

        assertTrue(salida.contains("Solicitud registrada con id 1"), salida);
        assertTrue(salida.contains("Solicitud 1 asignada a Laura Gómez"), salida);
        assertTrue(salida.contains("Solicitud 1 ahora esta RESUELTA"), salida);
        assertTrue(salida.contains("#1 [RESUELTA] CERTIFICADO - Ana Torres (10001)"), salida);
        assertTrue(salida.contains("1 resultado(s)"), salida);
    }

    @Test
    void finDeEntradaTerminaSinErrores() {
        String salida = ejecutar("");
        assertTrue(salida.isEmpty());
    }
}
