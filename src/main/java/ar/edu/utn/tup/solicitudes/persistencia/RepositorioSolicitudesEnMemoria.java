package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.Solicitud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RepositorioSolicitudesEnMemoria implements SolicitudRepository {

    private final Map<Integer, Solicitud> solicitudesPorId = new LinkedHashMap<>();
    private int ultimoId = 0;

    @Override
    public int siguienteId() {
        return ++ultimoId;
    }

    @Override
    public void guardar(Solicitud solicitud) {
        solicitudesPorId.put(solicitud.getId(), solicitud);
    }

    @Override
    public Optional<Solicitud> buscarPorId(int id) {
        return Optional.ofNullable(solicitudesPorId.get(id));
    }

    @Override
    public List<Solicitud> todas() {
        return new ArrayList<>(solicitudesPorId.values());
    }

    @Override
    public void reemplazarTodas(List<Solicitud> solicitudes) {
        solicitudesPorId.clear();
        solicitudes.forEach(this::guardar);
        ultimoId = solicitudes.stream().mapToInt(Solicitud::getId).max().orElse(0);
    }
}
