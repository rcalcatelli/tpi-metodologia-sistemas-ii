package ar.edu.utn.tup.solicitudes.servicio;

import ar.edu.utn.tup.solicitudes.DatosInvalidosException;
import ar.edu.utn.tup.solicitudes.DatosSolicitud;
import ar.edu.utn.tup.solicitudes.Estado;
import ar.edu.utn.tup.solicitudes.FiltroSolicitudes;
import ar.edu.utn.tup.solicitudes.Prioridad;
import ar.edu.utn.tup.solicitudes.RecursoNoEncontradoException;
import ar.edu.utn.tup.solicitudes.ReglaNegocioException;
import ar.edu.utn.tup.solicitudes.Solicitud;
import ar.edu.utn.tup.solicitudes.TipoSolicitud;
import ar.edu.utn.tup.solicitudes.persistencia.CatalogoResponsables;
import ar.edu.utn.tup.solicitudes.persistencia.RepositorioEstudiantesEnMemoria;
import ar.edu.utn.tup.solicitudes.persistencia.RepositorioSolicitudesEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SolicitudService")
class SolicitudServiceTest {

    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);

    private SolicitudService servicio;

    @BeforeEach
    void setUp() {
        servicio = nuevoServicio();
    }

    private SolicitudService nuevoServicio() {
        return new SolicitudService(new RepositorioSolicitudesEnMemoria(), new RepositorioEstudiantesEnMemoria(),
                new CatalogoResponsables(), new ValidadorSolicitud(), RELOJ_FIJO);
    }

    private static DatosSolicitud datos(String legajo, String tipo) {
        return new DatosSolicitud(legajo, "Estudiante " + legajo, "e" + legajo + "@example.com", tipo, "Descripción", 0);
    }

    @Test
    void registrarUsaLaFechaDelRelojInyectado() {
        Solicitud solicitud = servicio.registrar(datos("10001", "CERTIFICADO"));
        assertEquals(LocalDate.of(2026, 10, 15), solicitud.getFechaAlta());
    }

    @Test
    void cadaServicioTieneSuPropiaNumeracion() {
        SolicitudService otro = nuevoServicio();
        assertEquals(1, servicio.registrar(datos("10001", "CERTIFICADO")).getId());
        assertEquals(1, otro.registrar(datos("10001", "CERTIFICADO")).getId());
        assertEquals(2, servicio.registrar(datos("10002", "CERTIFICADO")).getId());
    }

    @Test
    void elEstudianteSeReutilizaPorLegajo() {
        Solicitud primera = servicio.registrar(datos("10001", "CERTIFICADO"));
        Solicitud segunda = servicio.registrar(new DatosSolicitud("10001", "Otro nombre", "otro@example.com", "EQUIVALENCIA", "x", 0));
        assertSame(primera.getEstudiante(), segunda.getEstudiante());
        assertEquals("Estudiante 10001", segunda.getEstudiante().getNombre());
    }

    @Test
    void erroresSeInformanConExcepcionesTipadas() {
        assertThrows(DatosInvalidosException.class, () -> servicio.registrar(datos("abc", "CERTIFICADO")));
        servicio.registrar(datos("10001", "CERTIFICADO"));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(datos("10001", "CERTIFICADO")));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.cambiarEstado(99, Estado.EN_PROCESO, ""));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.asignarResponsable(1, 99));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.actualizarEmail("99999", "ok@example.com"));
    }

    @Test
    void unIdRechazadoNoConsumeNumeracion() {
        assertThrows(DatosInvalidosException.class, () -> servicio.registrar(datos("1", "CERTIFICADO")));
        assertEquals(1, servicio.registrar(datos("10001", "CERTIFICADO")).getId());
    }

    @Test
    void buscarConFiltroTipado() {
        servicio.registrar(datos("10001", "CERTIFICADO"));
        servicio.registrar(datos("10002", "REVISION_NOTA"));
        servicio.registrar(datos("10001", "EQUIVALENCIA"));

        FiltroSolicitudes filtro = FiltroSolicitudes.todas().conLegajo("10001").conPrioridad(Prioridad.MEDIA);

        assertEquals(1, servicio.buscar(filtro).size());
        assertEquals(TipoSolicitud.EQUIVALENCIA, servicio.buscar(filtro).get(0).getTipo());
        assertEquals(TipoSolicitud.REVISION_NOTA, servicio.buscar(FiltroSolicitudes.todas()).get(0).getTipo());
    }

    @Test
    void reemplazarDatosReiniciaNumeracionYEstudiantes() {
        servicio.registrar(datos("10001", "CERTIFICADO"));
        SolicitudService origen = nuevoServicio();
        for (int i = 0; i < 5; i++) {
            origen.registrar(datos("1000" + i, "CERTIFICADO"));
        }

        servicio.reemplazarDatos(origen.todas());

        assertEquals(5, servicio.todas().size());
        assertEquals(6, servicio.registrar(datos("20000", "CERTIFICADO")).getId());
        servicio.actualizarEmail("10004", "nuevo@example.com");
    }
}
