package ar.edu.utn.tup.solicitudes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de caracterización: fijan el comportamiento externo observable de la
 * versión inicial usando solo la API pública de GestorSolicitudes.
 * Deben seguir pasando, sin cambios, después de cada refactoring.
 */
@DisplayName("Caracterización del comportamiento externo de GestorSolicitudes")
class CaracterizacionGestorTest {

    private GestorSolicitudes gestor;

    @BeforeEach
    void setUp() {
        gestor = new GestorSolicitudes();
    }

    private int registrarValida(String legajo, String tipo) {
        return gestor.registrarSolicitud(legajo, "Estudiante " + legajo, "e" + legajo + "@example.com", tipo, "Descripcion", 0);
    }

    private String estadoDe(int id) {
        String texto = gestor.obtener(id).toString();
        return texto.substring(texto.indexOf('[') + 1, texto.indexOf(']'));
    }

    @Nested
    @DisplayName("Registrar solicitud")
    class Registrar {

        @Test
        void asignaIdsCorrelativosDesdeUno() {
            assertEquals(1, registrarValida("10001", "CERTIFICADO"));
            assertEquals(2, registrarValida("10002", "CERTIFICADO"));
        }

        @Test
        void nuevaSolicitudQuedaPendienteYSinResponsable() {
            int id = registrarValida("10001", "CERTIFICADO");
            assertEquals("#1 [PENDIENTE] CERTIFICADO - Estudiante 10001 (10001) - Prioridad: BAJA - Resp: (sin asignar)",
                    gestor.obtener(id).toString());
        }

        @ParameterizedTest(name = "{0} -> prioridad {1}")
        @CsvSource({"CERTIFICADO, BAJA", "EQUIVALENCIA, MEDIA", "REVISION_NOTA, ALTA", "BAJA_MATERIA, MEDIA"})
        void prioridadAutomaticaSegunTipo(String tipo, String prioridadEsperada) {
            int id = registrarValida("10001", tipo);
            assertTrue(gestor.obtener(id).toString().contains("Prioridad: " + prioridadEsperada));
        }

        @Test
        void prioridadExplicitaSeRespeta() {
            int id = gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "CERTIFICADO", "Urgente", 1);
            assertTrue(gestor.obtener(id).toString().contains("Prioridad: ALTA"));
        }

        @ParameterizedTest(name = "legajo inválido: \"{0}\"")
        @ValueSource(strings = {"", "   ", "12a45", "1234", "1234567"})
        void rechazaLegajoInvalido(String legajo) {
            assertEquals(-1, gestor.registrarSolicitud(legajo, "Ana", "ana@example.com", "CERTIFICADO", "x", 0));
        }

        @Test
        void rechazaLegajoNulo() {
            assertEquals(-1, gestor.registrarSolicitud(null, "Ana", "ana@example.com", "CERTIFICADO", "x", 0));
        }

        @Test
        void aceptaLegajosDeCincoYSeisDigitos() {
            assertNotEquals(-1, gestor.registrarSolicitud("12345", "Ana", "ana@example.com", "CERTIFICADO", "x", 0));
            assertNotEquals(-1, gestor.registrarSolicitud("123456", "Ana", "ana@example.com", "CERTIFICADO", "x", 0));
        }

        @Test
        void rechazaNombreVacio() {
            assertEquals(-1, gestor.registrarSolicitud("10001", " ", "ana@example.com", "CERTIFICADO", "x", 0));
            assertEquals(-1, gestor.registrarSolicitud("10001", null, "ana@example.com", "CERTIFICADO", "x", 0));
        }

        @ParameterizedTest(name = "email inválido: \"{0}\"")
        @ValueSource(strings = {"anaexample.com", "ana@examplecom", "@example.com"})
        void rechazaEmailInvalido(String email) {
            assertEquals(-1, gestor.registrarSolicitud("10001", "Ana", email, "CERTIFICADO", "x", 0));
        }

        @Test
        void rechazaTipoInvalido() {
            assertEquals(-1, gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "INSCRIPCION", "x", 0));
            assertEquals(-1, gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "certificado", "x", 0));
            assertEquals(-1, gestor.registrarSolicitud("10001", "Ana", "ana@example.com", null, "x", 0));
        }

        @Test
        void rechazaDescripcionVaciaOMayorA500() {
            assertEquals(-1, gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "CERTIFICADO", "  ", 0));
            assertEquals(-1, gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "CERTIFICADO", "x".repeat(501), 0));
            assertNotEquals(-1, gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "CERTIFICADO", "x".repeat(500), 0));
        }

        @ParameterizedTest(name = "prioridad inválida: {0}")
        @ValueSource(ints = {-1, 4})
        void rechazaPrioridadFueraDeRango(int prioridad) {
            assertEquals(-1, gestor.registrarSolicitud("10001", "Ana", "ana@example.com", "CERTIFICADO", "x", prioridad));
        }

        @Test
        void rechazaSegundaSolicitudAbiertaDelMismoTipo() {
            registrarValida("10001", "CERTIFICADO");
            assertEquals(-1, registrarValida("10001", "CERTIFICADO"));
            assertNotEquals(-1, registrarValida("10001", "EQUIVALENCIA"));
            assertNotEquals(-1, registrarValida("10002", "CERTIFICADO"));
        }

        @Test
        void permiteNuevaSolicitudDelMismoTipoSiLaAnteriorEstaCerrada() {
            int id = registrarValida("10001", "CERTIFICADO");
            gestor.cambiarEstado(id, "RECHAZADA", "Falta documentación");
            assertNotEquals(-1, registrarValida("10001", "CERTIFICADO"));
        }

        @Test
        void unIdRechazadoNoConsumeNumeracion() {
            gestor.registrarSolicitud("1", "Ana", "ana@example.com", "CERTIFICADO", "x", 0);
            assertEquals(1, registrarValida("10001", "CERTIFICADO"));
        }
    }

    @Nested
    @DisplayName("Cambiar estado")
    class CambiarEstado {

        @Test
        void pendienteAEnProceso() {
            int id = registrarValida("10001", "CERTIFICADO");
            assertTrue(gestor.cambiarEstado(id, "EN_PROCESO", ""));
            assertEquals("EN_PROCESO", estadoDe(id));
        }

        @Test
        void pendienteNoPuedeResolverseDirectamente() {
            int id = registrarValida("10001", "CERTIFICADO");
            assertFalse(gestor.cambiarEstado(id, "RESUELTA", "ok"));
            assertEquals("PENDIENTE", estadoDe(id));
        }

        @Test
        void rechazarExigeMotivo() {
            int id = registrarValida("10001", "CERTIFICADO");
            assertFalse(gestor.cambiarEstado(id, "RECHAZADA", " "));
            assertFalse(gestor.cambiarEstado(id, "RECHAZADA", null));
            assertTrue(gestor.cambiarEstado(id, "RECHAZADA", "Falta documentación"));
            assertEquals("RECHAZADA", estadoDe(id));
        }

        @Test
        void enProcesoNoPuedeResolverseSinResponsable() {
            int id = registrarValida("10001", "CERTIFICADO");
            gestor.cambiarEstado(id, "EN_PROCESO", "");
            assertFalse(gestor.cambiarEstado(id, "RESUELTA", "ok"));
            assertEquals("EN_PROCESO", estadoDe(id));
        }

        @Test
        void enProcesoConResponsablePuedeResolverse() {
            int id = registrarValida("10001", "CERTIFICADO");
            gestor.asignarResponsable(id, 1);
            assertTrue(gestor.cambiarEstado(id, "RESUELTA", "Emitido"));
            assertEquals("RESUELTA", estadoDe(id));
        }

        @Test
        void enProcesoPuedeRechazarseConMotivo() {
            int id = registrarValida("10001", "CERTIFICADO");
            gestor.cambiarEstado(id, "EN_PROCESO", "");
            assertTrue(gestor.cambiarEstado(id, "RECHAZADA", "Duplicada"));
        }

        @Test
        void transicionesInvalidas() {
            int id = registrarValida("10001", "CERTIFICADO");
            assertFalse(gestor.cambiarEstado(id, "PENDIENTE", ""));
            assertFalse(gestor.cambiarEstado(id, "CUALQUIERA", ""));
            gestor.cambiarEstado(id, "EN_PROCESO", "");
            assertFalse(gestor.cambiarEstado(id, "EN_PROCESO", ""));
            assertFalse(gestor.cambiarEstado(id, "PENDIENTE", ""));
        }

        @Test
        void unaSolicitudCerradaNoCambiaMas() {
            int id = registrarValida("10001", "CERTIFICADO");
            gestor.cambiarEstado(id, "RECHAZADA", "Motivo");
            assertFalse(gestor.cambiarEstado(id, "EN_PROCESO", ""));
            assertFalse(gestor.cambiarEstado(id, "RESUELTA", ""));
            assertEquals("RECHAZADA", estadoDe(id));
        }

        @Test
        void solicitudInexistente() {
            assertFalse(gestor.cambiarEstado(99, "EN_PROCESO", ""));
        }
    }

    @Nested
    @DisplayName("Asignar responsable")
    class AsignarResponsable {

        @ParameterizedTest(name = "{0} lo atiende el responsable {1}")
        @CsvSource({"CERTIFICADO, 1", "EQUIVALENCIA, 2", "EQUIVALENCIA, 4", "REVISION_NOTA, 2", "BAJA_MATERIA, 3"})
        void asignaResponsableDelAreaCorrecta(String tipo, int idResponsable) {
            int id = registrarValida("10001", tipo);
            assertTrue(gestor.asignarResponsable(id, idResponsable));
        }

        @ParameterizedTest(name = "{0} no lo atiende el responsable {1}")
        @CsvSource({"CERTIFICADO, 2", "EQUIVALENCIA, 1", "REVISION_NOTA, 3", "BAJA_MATERIA, 4"})
        void rechazaResponsableDeOtraArea(String tipo, int idResponsable) {
            int id = registrarValida("10001", tipo);
            assertFalse(gestor.asignarResponsable(id, idResponsable));
            assertTrue(gestor.obtener(id).toString().endsWith("Resp: (sin asignar)"));
        }

        @Test
        void asignarPasaLaSolicitudPendienteAEnProceso() {
            int id = registrarValida("10001", "CERTIFICADO");
            gestor.asignarResponsable(id, 1);
            assertEquals("#1 [EN_PROCESO] CERTIFICADO - Estudiante 10001 (10001) - Prioridad: BAJA - Resp: Laura Gómez",
                    gestor.obtener(id).toString());
        }

        @Test
        void reasignarMantieneEnProceso() {
            int id = registrarValida("10001", "EQUIVALENCIA");
            gestor.asignarResponsable(id, 2);
            assertTrue(gestor.asignarResponsable(id, 4));
            assertTrue(gestor.obtener(id).toString().endsWith("Resp: Javier Ruiz"));
            assertEquals("EN_PROCESO", estadoDe(id));
        }

        @Test
        void responsableOSolicitudInexistentes() {
            int id = registrarValida("10001", "CERTIFICADO");
            assertFalse(gestor.asignarResponsable(id, 99));
            assertFalse(gestor.asignarResponsable(99, 1));
        }

        @Test
        void noSePuedeAsignarUnaSolicitudCerrada() {
            int id = registrarValida("10001", "CERTIFICADO");
            gestor.cambiarEstado(id, "RECHAZADA", "Motivo");
            assertFalse(gestor.asignarResponsable(id, 1));
        }
    }

    @Nested
    @DisplayName("Buscar solicitudes")
    class Buscar {

        @BeforeEach
        void cargarDatos() {
            registrarValida("10001", "CERTIFICADO");     // 1 BAJA
            registrarValida("10002", "REVISION_NOTA");   // 2 ALTA
            registrarValida("10001", "EQUIVALENCIA");    // 3 MEDIA
            registrarValida("10003", "BAJA_MATERIA");    // 4 MEDIA
            gestor.asignarResponsable(3, 2);              // 3 EN_PROCESO
        }

        private String ids(List<Solicitud> solicitudes) {
            StringBuilder sb = new StringBuilder();
            for (Solicitud s : solicitudes) {
                String texto = s.toString();
                sb.append(texto, 1, texto.indexOf(' ')).append(',');
            }
            return sb.toString();
        }

        @Test
        void sinFiltrosDevuelveTodasOrdenadasPorPrioridadYId() {
            assertEquals("2,3,4,1,", ids(gestor.buscar(null, null, null, 0)));
        }

        @Test
        void filtraPorEstado() {
            assertEquals("3,", ids(gestor.buscar("EN_PROCESO", null, null, 0)));
            assertEquals("2,4,1,", ids(gestor.buscar("PENDIENTE", null, null, 0)));
        }

        @Test
        void filtraPorTipoLegajoYPrioridad() {
            assertEquals("4,", ids(gestor.buscar(null, "BAJA_MATERIA", null, 0)));
            assertEquals("3,1,", ids(gestor.buscar(null, null, "10001", 0)));
            assertEquals("3,4,", ids(gestor.buscar(null, null, null, 2)));
        }

        @Test
        void combinaFiltros() {
            assertEquals("1,", ids(gestor.buscar("PENDIENTE", null, "10001", 3)));
            assertEquals("", ids(gestor.buscar("RESUELTA", null, null, 0)));
        }
    }

    @Nested
    @DisplayName("Reporte")
    class Reporte {

        @Test
        void reporteVacio() {
            assertEquals("""
                    ===== REPORTE DE SOLICITUDES =====
                    Total: 0
                    -- Por estado --
                    PENDIENTE: 0
                    EN_PROCESO: 0
                    RESUELTA: 0
                    RECHAZADA: 0
                    -- Por tipo --
                    CERTIFICADO: 0
                    EQUIVALENCIA: 0
                    REVISION_NOTA: 0
                    BAJA_MATERIA: 0
                    -- Por prioridad --
                    ALTA: 0
                    MEDIA: 0
                    BAJA: 0
                    Sin responsable asignado: 0
                    Tasa de resolucion: N/A
                    ==================================
                    """, gestor.generarReporte());
        }

        @Test
        void tasaDeResolucionRedondeadaAUnDecimal() {
            int a = registrarValida("10001", "CERTIFICADO");
            int b = registrarValida("10002", "CERTIFICADO");
            int c = registrarValida("10003", "CERTIFICADO");
            gestor.asignarResponsable(a, 1);
            gestor.cambiarEstado(a, "RESUELTA", "");
            gestor.cambiarEstado(b, "RECHAZADA", "x");
            gestor.cambiarEstado(c, "RECHAZADA", "x");
            String reporte = gestor.generarReporte();
            assertTrue(reporte.contains("Tasa de resolucion: 33.3%\n"), reporte);
            assertTrue(reporte.contains("Sin responsable asignado: 0\n"), reporte);
        }
    }

    @Nested
    @DisplayName("Persistencia en CSV")
    class Persistencia {

        @TempDir
        Path dir;

        @Test
        void formatoDelArchivo() throws IOException {
            int id = gestor.registrarSolicitud("10001", "Ana Torres", "ana@example.com", "EQUIVALENCIA", "Materia A; materia B", 0);
            gestor.asignarResponsable(id, 2);
            gestor.cambiarEstado(id, "RECHAZADA", "No; corresponde");
            registrarValida("10002", "CERTIFICADO");
            Path archivo = dir.resolve("datos.csv");

            assertTrue(gestor.guardar(archivo.toString()));

            String hoy = LocalDate.now().toString();
            assertEquals(List.of(
                    "1;10001;Ana Torres;ana@example.com;EQUIVALENCIA;Materia A, materia B;2;RECHAZADA;2;" + hoy + ";No, corresponde",
                    "2;10002;Estudiante 10002;e10002@example.com;CERTIFICADO;Descripcion;3;PENDIENTE;;" + hoy + ";"),
                    Files.readAllLines(archivo, StandardCharsets.UTF_8));
        }

        @Test
        void guardarYCargarConservaLosDatos() {
            registrarValida("10001", "CERTIFICADO");
            int b = registrarValida("10002", "EQUIVALENCIA");
            gestor.asignarResponsable(b, 4);
            Path archivo = dir.resolve("datos.csv");
            gestor.guardar(archivo.toString());

            GestorSolicitudes otro = new GestorSolicitudes();
            assertTrue(otro.cargar(archivo.toString()));

            assertEquals(gestor.generarReporte(), otro.generarReporte());
            assertEquals(gestor.obtener(b).toString(), otro.obtener(b).toString());
            assertEquals(3, otro.registrarSolicitud("10003", "Carla", "carla@example.com", "CERTIFICADO", "x", 0));
        }

        @Test
        void cargarArchivoInexistenteDevuelveFalse() {
            assertFalse(gestor.cargar(dir.resolve("no-existe.csv").toString()));
        }
    }

    @Nested
    @DisplayName("Actualizar email del estudiante")
    class ActualizarEmail {

        @TempDir
        Path dir;

        @Test
        void actualizaEmailDeEstudianteExistente() throws IOException {
            registrarValida("10001", "CERTIFICADO");
            assertTrue(gestor.actualizarEmailEstudiante("10001", "nuevo@example.com"));
            Path archivo = dir.resolve("datos.csv");
            gestor.guardar(archivo.toString());
            assertTrue(Files.readString(archivo, StandardCharsets.UTF_8).contains(";nuevo@example.com;"));
        }

        @Test
        void rechazaEmailInvalidoOEstudianteInexistente() {
            registrarValida("10001", "CERTIFICADO");
            assertFalse(gestor.actualizarEmailEstudiante("10001", "invalido"));
            assertFalse(gestor.actualizarEmailEstudiante("99999", "ok@example.com"));
        }
    }
}
