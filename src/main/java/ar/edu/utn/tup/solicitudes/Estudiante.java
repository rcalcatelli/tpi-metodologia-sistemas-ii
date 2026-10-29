package ar.edu.utn.tup.solicitudes;

public class Estudiante {

    private final String legajo;
    private final String nombre;
    private String email;

    public Estudiante(String legajo, String nombre, String email) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.email = email;
    }

    public void cambiarEmail(String nuevoEmail) {
        if (!Email.esValido(nuevoEmail)) {
            throw new DatosInvalidosException("email invalido");
        }
        this.email = nuevoEmail;
    }

    public String getLegajo() {
        return legajo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }
}
