package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.Estado;
import ar.edu.utn.tup.solicitudes.Estudiante;
import ar.edu.utn.tup.solicitudes.Prioridad;
import ar.edu.utn.tup.solicitudes.Responsable;
import ar.edu.utn.tup.solicitudes.Solicitud;
import ar.edu.utn.tup.solicitudes.TipoSolicitud;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee y escribe solicitudes en CSV (UTF-8, separador ';'), una por línea:
 * id;legajo;nombre;email;tipo;descripcion;prioridad;estado;idResponsable;fecha;observacion
 */
public class ArchivoCsvSolicitudes {

    private static final String SEPARADOR = ";";
    private static final String REEMPLAZO_SEPARADOR = ",";
    private static final int CANTIDAD_COLUMNAS = 11;

    private final ResponsableRepository responsables;

    public ArchivoCsvSolicitudes(ResponsableRepository responsables) {
        this.responsables = responsables;
    }

    public void guardar(List<Solicitud> solicitudes, Path ruta) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            for (Solicitud solicitud : solicitudes) {
                writer.write(aLinea(solicitud));
                writer.newLine();
            }
        }
    }

    /** Lee el archivo completo; si alguna línea es inválida no devuelve datos parciales. */
    public List<Solicitud> cargar(Path ruta) throws IOException {
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        Map<String, Estudiante> estudiantesPorLegajo = new HashMap<>();
        List<Solicitud> solicitudes = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            try {
                solicitudes.add(desdeLinea(lineas.get(i), estudiantesPorLegajo));
            } catch (RuntimeException e) {
                throw new ArchivoInvalidoException("Línea " + (i + 1) + " inválida: " + lineas.get(i), e);
            }
        }
        return solicitudes;
    }

    private String aLinea(Solicitud solicitud) {
        Estudiante estudiante = solicitud.getEstudiante();
        String idResponsable = solicitud.tieneResponsable() ? String.valueOf(solicitud.getResponsable().getId()) : "";
        return String.join(SEPARADOR,
                String.valueOf(solicitud.getId()),
                estudiante.getLegajo(),
                estudiante.getNombre(),
                estudiante.getEmail(),
                solicitud.getTipo().name(),
                sinSeparador(solicitud.getDescripcion()),
                String.valueOf(solicitud.getPrioridad().getCodigo()),
                solicitud.getEstado().name(),
                idResponsable,
                solicitud.getFechaAlta().toString(),
                sinSeparador(solicitud.getObservacion()));
    }

    private Solicitud desdeLinea(String linea, Map<String, Estudiante> estudiantesPorLegajo) {
        String[] campos = linea.split(SEPARADOR, -1);
        if (campos.length != CANTIDAD_COLUMNAS) {
            throw new IllegalArgumentException("se esperaban " + CANTIDAD_COLUMNAS + " columnas");
        }
        Estudiante estudiante = estudiantesPorLegajo.computeIfAbsent(campos[1],
                legajo -> new Estudiante(legajo, campos[2], campos[3]));
        return Solicitud.restaurar(
                Integer.parseInt(campos[0]),
                estudiante,
                TipoSolicitud.valueOf(campos[4]),
                campos[5],
                Prioridad.desdeCodigo(Integer.parseInt(campos[6])).orElseThrow(),
                LocalDate.parse(campos[9]),
                Estado.valueOf(campos[7]),
                responsableDe(campos[8]),
                campos[10]);
    }

    private Responsable responsableDe(String idResponsable) {
        if (idResponsable.isEmpty()) {
            return null;
        }
        int id = Integer.parseInt(idResponsable);
        return responsables.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("responsable inexistente " + id));
    }

    private String sinSeparador(String texto) {
        return texto.replace(SEPARADOR, REEMPLAZO_SEPARADOR);
    }
}
