package ar.edu.utn.tup.solicitudes;

public class Responsable {
    public int id;
    public String nombre;
    public Area area;

    public Responsable(int id, String nombre, Area area) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
    }
}
