package ar.edu.utn.tup.solicitudes;

import java.util.Arrays;
import java.util.Optional;

public enum Prioridad {
    ALTA(1),
    MEDIA(2),
    BAJA(3);

    private final int codigo;

    Prioridad(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static Optional<Prioridad> desdeCodigo(int codigo) {
        return Arrays.stream(values()).filter(prioridad -> prioridad.codigo == codigo).findFirst();
    }
}
