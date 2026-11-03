package ar.edu.utn.tup.solicitudes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reglas del dominio")
class DominioTest {

    private static final Responsable ALUMNADO = new Responsable(1, "Laura Gómez", Area.ALUMNADO);
    private static final Responsable DEPARTAMENTO = new Responsable(2, "Martín Díaz", Area.DEPARTAMENTO_ACADEMICO);
    private static final LocalDate FECHA_ALTA = LocalDate.of(2026, 10, 1);

    private static Estudiante ana() {
        return new Estudiante("10001", "Ana Torres", "ana@example.com");
    }

    @Nested
    @DisplayName("Estado")
    class EstadoTest {

        @ParameterizedTest(name = "{0} -> {1}: {2}")
        @CsvSource({
                "PENDIENTE, EN_PROCESO, true", "PENDIENTE, RECHAZADA, true", "PENDIENTE, RESUELTA, false",
                "PENDIENTE, PENDIENTE, false", "EN_PROCESO, RESUELTA, true", "EN_PROCESO, RECHAZADA, true",
                "EN_PROCESO, PENDIENTE, false", "RESUELTA, EN_PROCESO, false", "RECHAZADA, PENDIENTE, false"})
        void transicionesPermitidas(Estado origen, Estado destino, boolean permitida) {
            assertEquals(permitida, origen.puedePasarA(destino));
        }

        @Test
        void soloResueltaYRechazadaSonFinales() {
            assertFalse(Estado.PENDIENTE.esFinal());
            assertFalse(Estado.EN_PROCESO.esFinal());
            assertTrue(Estado.RESUELTA.esFinal());
            assertTrue(Estado.RECHAZADA.esFinal());
        }

        @Test
        void conversionDesdeTexto() {
            assertEquals(Estado.EN_PROCESO, Estado.desdeNombre("EN_PROCESO").orElseThrow());
            assertTrue(Estado.desdeNombre("en_proceso").isEmpty());
            assertTrue(Estado.desdeNombre(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("TipoSolicitud y Responsable")
    class TipoTest {

        @ParameterizedTest(name = "{0}: prioridad {1}, área {2}")
        @CsvSource({
                "CERTIFICADO, BAJA, ALUMNADO", "EQUIVALENCIA, MEDIA, DEPARTAMENTO_ACADEMICO",
                "REVISION_NOTA, ALTA, DEPARTAMENTO_ACADEMICO", "BAJA_MATERIA, MEDIA, BEDELIA"})
        void cadaTipoConoceSuPrioridadYArea(TipoSolicitud tipo, Prioridad prioridad, Area area) {
            assertEquals(prioridad, tipo.getPrioridadPorDefecto());
            assertEquals(area, tipo.getAreaResponsable());
        }

        @Test
        void estudianteNoAceptaEmailInvalido() {
            Estudiante estudiante = ana();
            assertThrows(DatosInvalidosException.class, () -> estudiante.cambiarEmail("sin-arroba"));
            assertEquals("ana@example.com", estudiante.getEmail());
        }

        @Test
        void responsableAtiendeSoloLosTiposDeSuArea() {
            assertTrue(DEPARTAMENTO.puedeAtender(TipoSolicitud.EQUIVALENCIA));
            assertTrue(DEPARTAMENTO.puedeAtender(TipoSolicitud.REVISION_NOTA));
            assertFalse(DEPARTAMENTO.puedeAtender(TipoSolicitud.CERTIFICADO));
        }
    }

    @Nested
    @DisplayName("Solicitud")
    class SolicitudTest {

        private Solicitud solicitud;

        @BeforeEach
        void crear() {
            solicitud = new Solicitud(1, ana(), TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA, FECHA_ALTA);
        }

        @Test
        void naceAbiertaPendienteYSinResponsable() {
            assertEquals(Estado.PENDIENTE, solicitud.getEstado());
            assertTrue(solicitud.estaAbierta());
            assertFalse(solicitud.tieneResponsable());
            assertEquals("", solicitud.getObservacion());
        }

        @Test
        void asignarResponsableDelAreaLaPoneEnProceso() {
            solicitud.asignar(ALUMNADO);
            assertEquals(Estado.EN_PROCESO, solicitud.getEstado());
            assertSame(ALUMNADO, solicitud.getResponsable());
        }

        @Test
        void asignarResponsableDeOtraAreaLanzaReglaDeNegocio() {
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> solicitud.asignar(DEPARTAMENTO));
            assertEquals("el responsable no pertenece al area Alumnado", error.getMessage());
            assertFalse(solicitud.tieneResponsable());
        }

        @Test
        void resolverGuardaLaObservacion() {
            solicitud.asignar(ALUMNADO);
            solicitud.cambiarEstado(Estado.RESUELTA, "Emitido");
            assertEquals("Emitido", solicitud.getObservacion());
            assertFalse(solicitud.estaAbierta());
        }

        @Test
        void pasarAEnProcesoNoGuardaObservacion() {
            solicitud.cambiarEstado(Estado.EN_PROCESO, "ignorada");
            assertEquals("", solicitud.getObservacion());
        }

        @Test
        void erroresDeTransicionSonExcepcionesConMensaje() {
            assertRechazaCambio(Estado.RESUELTA, "", "transicion invalida");
            assertRechazaCambio(null, "", "transicion invalida");
            assertRechazaCambio(Estado.RECHAZADA, " ", "para rechazar hay que indicar el motivo");

            solicitud.cambiarEstado(Estado.EN_PROCESO, "");
            assertRechazaCambio(Estado.RESUELTA, "", "no se puede resolver sin responsable");
        }

        private void assertRechazaCambio(Estado destino, String observacion, String mensajeEsperado) {
            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> solicitud.cambiarEstado(destino, observacion));
            assertEquals(mensajeEsperado, error.getMessage());
        }

        @Test
        void cerradaNoAceptaCambios() {
            solicitud.cambiarEstado(Estado.RECHAZADA, "Motivo");
            assertThrows(ReglaNegocioException.class, () -> solicitud.cambiarEstado(Estado.EN_PROCESO, ""));
            assertThrows(ReglaNegocioException.class, () -> solicitud.asignar(ALUMNADO));
        }
    }
}
