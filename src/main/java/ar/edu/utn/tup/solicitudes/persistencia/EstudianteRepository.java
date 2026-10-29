package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.Estudiante;

import java.util.Collection;
import java.util.Optional;

public interface EstudianteRepository {

    Optional<Estudiante> buscarPorLegajo(String legajo);

    void guardar(Estudiante estudiante);

    void reemplazarTodos(Collection<Estudiante> estudiantes);
}
