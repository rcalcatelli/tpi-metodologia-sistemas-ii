package ar.edu.utn.tup.solicitudes;

public class Responsable {

    private final int id;
    private final String nombre;
    private final Area area;

    public Responsable(int id, String nombre, Area area) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
    }

    public boolean puedeAtender(TipoSolicitud tipo) {
        return area == tipo.getAreaResponsable();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Area getArea() {
        return area;
    }
}
