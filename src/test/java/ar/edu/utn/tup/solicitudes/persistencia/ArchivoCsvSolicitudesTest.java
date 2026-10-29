package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.GestorSolicitudes;
import ar.edu.utn.tup.solicitudes.Solicitud;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArchivoCsvSolicitudes")
class ArchivoCsvSolicitudesTest {

    @TempDir
    Path dir;

    private final ArchivoCsvSolicitudes archivo = new ArchivoCsvSolicitudes(new CatalogoResponsables());

    private GestorSolicitudes nuevoGestor() {
        return new GestorSolicitudes(new CatalogoResponsables(), Clock.systemDefaultZone(),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }

    @Test
    void conservaAcentosPorqueEscribeEnUtf8() throws IOException {
        GestorSolicitudes gestor = nuevoGestor();
        gestor.registrarSolicitud("10001", "Íñigo Ñúñez", "inigo@example.com", "CERTIFICADO", "Constancia de inscripción", 0);
        Path ruta = dir.resolve("datos.csv");
        gestor.guardar(ruta.toString());

        List<Solicitud> leidas = archivo.cargar(ruta);

        assertEquals("Íñigo Ñúñez", leidas.get(0).getEstudiante().getNombre());
        assertEquals("Constancia de inscripción", leidas.get(0).getDescripcion());
    }

    @Test
    void lineaInvalidaLanzaExcepcionConElNumeroDeLinea() throws IOException {
        Path ruta = dir.resolve("roto.csv");
        Files.writeString(ruta, "1;10001;Ana;ana@example.com;CERTIFICADO;x;3;PENDIENTE;;2026-10-01;\nesto no es una solicitud\n");

        ArchivoInvalidoException error = assertThrows(ArchivoInvalidoException.class, () -> archivo.cargar(ruta));

        assertTrue(error.getMessage().startsWith("Línea 2 inválida"), error.getMessage());
    }

    @Test
    void responsableInexistenteEsArchivoInvalido() throws IOException {
        Path ruta = dir.resolve("roto.csv");
        Files.writeString(ruta, "1;10001;Ana;ana@example.com;CERTIFICADO;x;3;EN_PROCESO;99;2026-10-01;\n");

        assertThrows(ArchivoInvalidoException.class, () -> archivo.cargar(ruta));
    }

    @Test
    void siElArchivoEsInvalidoLosDatosEnMemoriaNoSePierden() throws IOException {
        GestorSolicitudes gestor = nuevoGestor();
        gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "CERTIFICADO", "x", 0);
        Path ruta = dir.resolve("roto.csv");
        Files.writeString(ruta, "1;10002;Bruno;bruno@example.com;CERTIFICADO;x;3;PENDIENTE;;2026-10-01;\nlinea rota\n");

        assertFalse(gestor.cargar(ruta.toString()));

        assertEquals(1, gestor.buscar(null, null, "10001", 0).size());
        assertTrue(gestor.buscar(null, null, "10002", 0).isEmpty());
    }
}
