package ar.edu.utn.tup.solicitudes.ui;

import ar.edu.utn.tup.solicitudes.GestorSolicitudes;
import ar.edu.utn.tup.solicitudes.Responsable;
import ar.edu.utn.tup.solicitudes.Solicitud;

import java.io.PrintStream;
import java.util.List;
import java.util.OptionalInt;
import java.util.Scanner;

/** Menú interactivo de consola. Solo lee, delega en la fachada y muestra. */
public class ConsolaApp {

    private static final String ARCHIVO_DATOS = "solicitudes.csv";
    private static final int SALIR = 0;
    private static final int OPCION_INVALIDA = -1;

    private final GestorSolicitudes gestor;
    private final Scanner entrada;
    private final PrintStream salida;

    public ConsolaApp(GestorSolicitudes gestor, Scanner entrada, PrintStream salida) {
        this.gestor = gestor;
        this.entrada = entrada;
        this.salida = salida;
    }

    public void ejecutar() {
        int opcion = OPCION_INVALIDA;
        while (opcion != SALIR && entrada.hasNextLine()) {
            mostrarMenu();
            opcion = leerEntero().orElse(OPCION_INVALIDA);
            atender(opcion);
        }
    }

    private void mostrarMenu() {
        salida.println();
        salida.println("=== SISTEMA DE SOLICITUDES ACADEMICAS ===");
        salida.println("1. Registrar solicitud");
        salida.println("2. Cambiar estado");
        salida.println("3. Asignar responsable");
        salida.println("4. Buscar solicitudes");
        salida.println("5. Reporte");
        salida.println("6. Guardar en archivo");
        salida.println("7. Cargar desde archivo");
        salida.println("0. Salir");
        salida.print("Opcion: ");
    }

    private void atender(int opcion) {
        switch (opcion) {
            case 1 -> registrarSolicitud();
            case 2 -> cambiarEstado();
            case 3 -> asignarResponsable();
            case 4 -> buscarSolicitudes();
            case 5 -> salida.print(gestor.generarReporte());
            case 6 -> informarSiOk(gestor.guardar(ARCHIVO_DATOS), "Guardado OK");
            case 7 -> informarSiOk(gestor.cargar(ARCHIVO_DATOS), "Cargado OK");
            default -> { }
        }
    }

    private void registrarSolicitud() {
        String legajo = preguntar("Legajo: ");
        String nombre = preguntar("Nombre: ");
        String email = preguntar("Email: ");
        String tipo = preguntar("Tipo (CERTIFICADO/EQUIVALENCIA/REVISION_NOTA/BAJA_MATERIA): ").trim().toUpperCase();
        String descripcion = preguntar("Descripcion: ");
        salida.print("Prioridad (1 alta, 2 media, 3 baja, 0 automatica): ");
        int prioridad = leerEntero().orElse(0);
        gestor.registrarSolicitud(legajo, nombre, email, tipo, descripcion, prioridad);
    }

    private void cambiarEstado() {
        salida.print("Id: ");
        OptionalInt id = leerEnteroObligatorio();
        if (id.isEmpty()) {
            return;
        }
        String estado = preguntar("Nuevo estado (EN_PROCESO/RESUELTA/RECHAZADA): ").trim().toUpperCase();
        String observacion = preguntar("Observacion: ");
        gestor.cambiarEstado(id.getAsInt(), estado, observacion);
    }

    private void asignarResponsable() {
        for (Responsable responsable : gestor.getResponsables()) {
            salida.println(responsable.getId() + " - " + responsable.getNombre() + " (" + responsable.getArea().getNombre() + ")");
        }
        salida.print("Id solicitud: ");
        OptionalInt idSolicitud = leerEnteroObligatorio();
        if (idSolicitud.isEmpty()) {
            return;
        }
        salida.print("Id responsable: ");
        OptionalInt idResponsable = leerEnteroObligatorio();
        if (idResponsable.isEmpty()) {
            return;
        }
        gestor.asignarResponsable(idSolicitud.getAsInt(), idResponsable.getAsInt());
    }

    private void buscarSolicitudes() {
        String estado = preguntar("Estado (enter = todos): ").trim().toUpperCase();
        String tipo = preguntar("Tipo (enter = todos): ").trim().toUpperCase();
        String legajo = preguntar("Legajo (enter = todos): ").trim();
        List<Solicitud> resultado = gestor.buscar(vacioComoNulo(estado), vacioComoNulo(tipo), vacioComoNulo(legajo), 0);
        resultado.forEach(salida::println);
        salida.println(resultado.size() + " resultado(s)");
    }

    private String preguntar(String mensaje) {
        salida.print(mensaje);
        return entrada.hasNextLine() ? entrada.nextLine() : "";
    }

    private OptionalInt leerEntero() {
        if (!entrada.hasNextLine()) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(entrada.nextLine().trim()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    private OptionalInt leerEnteroObligatorio() {
        OptionalInt valor = leerEntero();
        if (valor.isEmpty()) {
            salida.println("Error: debe ingresar un numero");
        }
        return valor;
    }

    private void informarSiOk(boolean ok, String mensaje) {
        if (ok) {
            salida.println(mensaje);
        }
    }

    private static String vacioComoNulo(String texto) {
        return texto.isEmpty() ? null : texto;
    }
}
