package ar.edu.utn.tup.solicitudes.persistencia;

import ar.edu.utn.tup.solicitudes.Area;
import ar.edu.utn.tup.solicitudes.Responsable;

import java.util.List;
import java.util.Optional;

/** Responsables fijos de cada área (datos sintéticos). */
public class CatalogoResponsables implements ResponsableRepository {

    private final List<Responsable> responsables;

    public CatalogoResponsables() {
        this(List.of(
                new Responsable(1, "Laura Gómez", Area.ALUMNADO),
                new Responsable(2, "Martín Díaz", Area.DEPARTAMENTO_ACADEMICO),
                new Responsable(3, "Sofía Pereyra", Area.BEDELIA),
                new Responsable(4, "Javier Ruiz", Area.DEPARTAMENTO_ACADEMICO)));
    }

    public CatalogoResponsables(List<Responsable> responsables) {
        this.responsables = List.copyOf(responsables);
    }

    @Override
    public Optional<Responsable> buscarPorId(int id) {
        return responsables.stream().filter(responsable -> responsable.getId() == id).findFirst();
    }

    @Override
    public List<Responsable> todos() {
        return responsables;
    }
}
