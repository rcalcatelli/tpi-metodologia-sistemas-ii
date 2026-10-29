package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.Estudiante;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RepositorioEstudiantesEnMemoria implements EstudianteRepository {

    private final Map<String, Estudiante> estudiantesPorLegajo = new LinkedHashMap<>();

    @Override
    public Optional<Estudiante> buscarPorLegajo(String legajo) {
        return Optional.ofNullable(estudiantesPorLegajo.get(legajo));
    }

    @Override
    public void guardar(Estudiante estudiante) {
        estudiantesPorLegajo.put(estudiante.getLegajo(), estudiante);
    }

    @Override
    public void reemplazarTodos(Collection<Estudiante> estudiantes) {
        estudiantesPorLegajo.clear();
        estudiantes.forEach(this::guardar);
    }
}
