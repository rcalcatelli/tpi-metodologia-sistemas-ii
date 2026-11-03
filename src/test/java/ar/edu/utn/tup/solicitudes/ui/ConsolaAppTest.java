package ar.edu.utn.tup.solicitudes.ui;

import ar.edu.utn.tup.solicitudes.GestoresDePrueba;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ConsolaApp")
class ConsolaAppTest {

    private static final String SALIR = "0";

    /** Ejecuta la consola con una línea de entrada por argumento y devuelve todo lo impreso. */
    private String ejecutar(String... lineas) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsolaApp(GestoresDePrueba.conSalida(salida), new Scanner(String.join("\n", lineas)), salida).ejecutar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void assertContiene(String salida, String esperado) {
        assertTrue(salida.contains(esperado), () -> "falta \"" + esperado + "\" en:\n" + salida);
    }

    @Test
    void idNoNumericoNoTerminaElPrograma() {
        String salida = ejecutar(
                "2", "abc",     // cambiar estado con id inválido
                "3", "xyz",     // asignar responsable con id inválido
                "5",            // el programa sigue: muestra el reporte
                SALIR, "");

        assertContiene(salida, "Error: debe ingresar un numero");
        assertContiene(salida, "===== REPORTE DE SOLICITUDES =====");
    }

    @Test
    void flujoCompletoDesdeElMenu() {
        String salida = ejecutar(
                "1", "10001", "Ana Torres", "ana@example.com", "certificado", "Constancia", "0",  // registrar
                "3", "1", "1",                                                                // asignar
                "2", "1", "RESUELTA", "Emitido",                                              // resolver
                "4", "", "", "10001",                                                         // buscar por legajo
                SALIR, "");

        assertContiene(salida, "Solicitud registrada con id 1");
        assertContiene(salida, "Solicitud 1 asignada a Laura Gómez");
        assertContiene(salida, "Solicitud 1 ahora esta RESUELTA");
        assertContiene(salida, "#1 [RESUELTA] CERTIFICADO - Ana Torres (10001)");
        assertContiene(salida, "1 resultado(s)");
    }

    @Test
    void finDeEntradaTerminaSinErrores() {
        assertTrue(ejecutar().isEmpty());
    }
}
