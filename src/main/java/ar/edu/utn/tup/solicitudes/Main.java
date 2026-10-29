package ar.edu.utn.tup.solicitudes;

import ar.edu.utn.tup.solicitudes.ui.ConsolaApp;
import ar.edu.utn.tup.solicitudes.ui.EscenarioDemo;

import java.util.Scanner;

public class Main {

    private static final String ARGUMENTO_DEMO = "--demo";

    public static void main(String[] args) {
        GestorSolicitudes gestor = new GestorSolicitudes();
        if (args.length > 0 && args[0].equals(ARGUMENTO_DEMO)) {
            new EscenarioDemo(gestor, System.out).ejecutar();
        } else {
            new ConsolaApp(gestor, new Scanner(System.in), System.out).ejecutar();
        }
    }
}
