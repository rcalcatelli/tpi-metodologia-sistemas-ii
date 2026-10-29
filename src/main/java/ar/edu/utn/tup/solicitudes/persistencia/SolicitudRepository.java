package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.Solicitud;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository {

    int siguienteId();

    void guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(int id);

    /** Todas las solicitudes en orden de alta. */
    List<Solicitud> todas();

    void reemplazarTodas(List<Solicitud> solicitudes);
}
