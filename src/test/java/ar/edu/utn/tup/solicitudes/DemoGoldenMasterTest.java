package ar.edu.utn.tup.solicitudes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Golden master: la salida completa del escenario de demostración (mensajes,
 * listado y reporte) se compara contra la salida registrada de la versión inicial.
 */
@DisplayName("Golden master del escenario --demo")
class DemoGoldenMasterTest {

    @Test
    void laSalidaDelDemoNoCambia() throws IOException {
        String esperado = leerRecurso("/golden/demo-esperado.txt");

        String obtenido = ejecutarDemo();

        assertEquals(normalizar(esperado), normalizar(obtenido));
    }

    private String ejecutarDemo() {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            Main.main(new String[]{"--demo"});
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private String leerRecurso(String ruta) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(ruta)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String normalizar(String texto) {
        return texto.replace("\r\n", "\n");
    }
}
