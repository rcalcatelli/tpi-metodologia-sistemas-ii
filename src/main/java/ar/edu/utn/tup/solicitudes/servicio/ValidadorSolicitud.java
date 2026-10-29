package ar.edu.utn.tup.solicitudes.servicio;

import ar.edu.utn.tup.solicitudes.DatosInvalidosException;
import ar.edu.utn.tup.solicitudes.DatosSolicitud;
import ar.edu.utn.tup.solicitudes.Email;
import ar.edu.utn.tup.solicitudes.Prioridad;
import ar.edu.utn.tup.solicitudes.TipoSolicitud;

/** Reglas de formato de los datos de entrada. Lanza DatosInvalidosException con el primer error encontrado. */
public class ValidadorSolicitud {

    private static final int LARGO_MINIMO_LEGAJO = 5;
    private static final int LARGO_MAXIMO_LEGAJO = 6;
    private static final int LARGO_MAXIMO_DESCRIPCION = 500;

    public void validar(DatosSolicitud datos) {
        validarLegajo(datos.legajo());
        validarNombre(datos.nombre());
        validarEmail(datos.email());
        validarTipo(datos.tipo());
        validarDescripcion(datos.descripcion());
        validarPrioridad(datos);
    }

    public void validarEmail(String email) {
        exigir(Email.esValido(email), "email invalido");
    }

    private void validarLegajo(String legajo) {
        exigir(!estaVacio(legajo), "legajo vacio");
        boolean formatoValido = legajo.chars().allMatch(Character::isDigit)
                && legajo.length() >= LARGO_MINIMO_LEGAJO
                && legajo.length() <= LARGO_MAXIMO_LEGAJO;
        exigir(formatoValido, "legajo invalido");
    }

    private void validarNombre(String nombre) {
        exigir(!estaVacio(nombre), "nombre vacio");
    }

    private void validarTipo(String tipo) {
        exigir(TipoSolicitud.desdeNombre(tipo).isPresent(), "tipo invalido");
    }

    private void validarDescripcion(String descripcion) {
        exigir(!estaVacio(descripcion) && descripcion.length() <= LARGO_MAXIMO_DESCRIPCION, "descripcion invalida");
    }

    private void validarPrioridad(DatosSolicitud datos) {
        exigir(datos.usaPrioridadAutomatica() || Prioridad.desdeCodigo(datos.prioridad()).isPresent(), "prioridad invalida");
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private void exigir(boolean condicion, String mensajeDeError) {
        if (!condicion) {
            throw new DatosInvalidosException(mensajeDeError);
        }
    }
}
