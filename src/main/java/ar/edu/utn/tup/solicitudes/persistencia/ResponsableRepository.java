package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.Responsable;

import java.util.List;
import java.util.Optional;

public interface ResponsableRepository {

    Optional<Responsable> buscarPorId(int id);

    List<Responsable> todos();
}
