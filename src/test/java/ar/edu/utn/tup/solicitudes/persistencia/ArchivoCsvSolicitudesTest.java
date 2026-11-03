package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.GestorSolicitudes;
import ar.edu.utn.tup.solicitudes.GestoresDePrueba;
import ar.edu.utn.tup.solicitudes.Solicitud;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArchivoCsvSolicitudes")
class ArchivoCsvSolicitudesTest {

    private static final String PENDIENTE_VALIDA = "1;10002;Bruno;bruno@example.com;CERTIFICADO;x;3;PENDIENTE;;2026-10-01;";
    private static final String LINEA_INVALIDA = "esto no es una solicitud";
    private static final String CON_RESPONSABLE_INEXISTENTE = "1;10001;Ana;ana@example.com;CERTIFICADO;x;3;EN_PROCESO;99;2026-10-01;";

    @TempDir
    Path dir;

    private final ArchivoCsvSolicitudes archivo = new ArchivoCsvSolicitudes(new CatalogoResponsables());

    /** Escribe un CSV temporal con una solicitud por línea. */
    private Path csvCon(String... lineas) throws IOException {
        return Files.writeString(dir.resolve("datos.csv"), String.join("\n", lineas) + "\n");
    }

    @Test
    void conservaAcentosPorqueEscribeEnUtf8() throws IOException {
        GestorSolicitudes gestor = GestoresDePrueba.silencioso();
        gestor.registrarSolicitud("10001", "Íñigo Ñúñez", "inigo@example.com", "CERTIFICADO", "Constancia de inscripción", 0);
        Path ruta = dir.resolve("datos.csv");
        gestor.guardar(ruta.toString());

        Solicitud leida = archivo.cargar(ruta).get(0);

        assertEquals("Íñigo Ñúñez", leida.getEstudiante().getNombre());
        assertEquals("Constancia de inscripción", leida.getDescripcion());
    }

    @Test
    void lineaInvalidaLanzaExcepcionConElNumeroDeLinea() throws IOException {
        Path ruta = csvCon(PENDIENTE_VALIDA, LINEA_INVALIDA);

        ArchivoInvalidoException error = assertThrows(ArchivoInvalidoException.class, () -> archivo.cargar(ruta));

        assertTrue(error.getMessage().startsWith("Línea 2 inválida"), error.getMessage());
    }

    @Test
    void responsableInexistenteEsArchivoInvalido() throws IOException {
        Path ruta = csvCon(CON_RESPONSABLE_INEXISTENTE);

        assertThrows(ArchivoInvalidoException.class, () -> archivo.cargar(ruta));
    }

    @Test
    void siElArchivoEsInvalidoLosDatosEnMemoriaNoSePierden() throws IOException {
        GestorSolicitudes gestor = GestoresDePrueba.silencioso();
        gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "CERTIFICADO", "x", 0);
        Path ruta = csvCon(PENDIENTE_VALIDA, LINEA_INVALIDA);

        assertFalse(gestor.cargar(ruta.toString()));

        assertEquals(1, gestor.buscar(null, null, "10001", 0).size(), "la solicitud previa sigue en memoria");
        assertTrue(gestor.buscar(null, null, "10002", 0).isEmpty(), "no se cargó la línea válida del archivo roto");
    }
}
