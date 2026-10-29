package ar.edu.utn.tup.solicitudes;

/** Regla única de formato de email, compartida por la validación de entrada y por Estudiante. */
public final class Email {

    private Email() {
    }

    public static boolean esValido(String email) {
        return email != null && email.contains("@") && email.contains(".") && !email.startsWith("@");
    }
}
