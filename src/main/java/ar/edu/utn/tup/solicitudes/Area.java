package ar.edu.utn.tup.solicitudes;

public enum Area {
    ALUMNADO("Alumnado"),
    DEPARTAMENTO_ACADEMICO("Departamento Académico"),
    BEDELIA("Bedelía");

    private final String nombre;

    Area(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
