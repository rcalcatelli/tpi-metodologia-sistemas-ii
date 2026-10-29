package ar.edu.utn.tup.solicitudes;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        GestorSolicitudes g = new GestorSolicitudes();
        if (args.length > 0 && args[0].equals("--demo")) {
            demo(g);
            return;
        }
        Scanner sc = new Scanner(System.in);
        int op = -1;
        while (op != 0) {
            System.out.println();
            System.out.println("=== SISTEMA DE SOLICITUDES ACADEMICAS ===");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar solicitudes");
            System.out.println("5. Reporte");
            System.out.println("6. Guardar en archivo");
            System.out.println("7. Cargar desde archivo");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            try {
                op = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                op = -1;
            }
            if (op == 1) {
                System.out.print("Legajo: ");
                String leg = sc.nextLine();
                System.out.print("Nombre: ");
                String nom = sc.nextLine();
                System.out.print("Email: ");
                String em = sc.nextLine();
                System.out.print("Tipo (CERTIFICADO/EQUIVALENCIA/REVISION_NOTA/BAJA_MATERIA): ");
                String t = sc.nextLine().trim().toUpperCase();
                System.out.print("Descripcion: ");
                String d = sc.nextLine();
                System.out.print("Prioridad (1 alta, 2 media, 3 baja, 0 automatica): ");
                int p = 0;
                try {
                    p = Integer.parseInt(sc.nextLine().trim());
                } catch (Exception e) {
                }
                g.registrarSolicitud(leg, nom, em, t, d, p);
            } else if (op == 2) {
                System.out.print("Id: ");
                int id = Integer.parseInt(sc.nextLine().trim());
                System.out.print("Nuevo estado (EN_PROCESO/RESUELTA/RECHAZADA): ");
                String e = sc.nextLine().trim().toUpperCase();
                System.out.print("Observacion: ");
                String o = sc.nextLine();
                g.cambiarEstado(id, e, o);
            } else if (op == 3) {
                for (Responsable r : g.getResponsables()) {
                    System.out.println(r.id + " - " + r.nombre + " (" + r.area.getNombre() + ")");
                }
                System.out.print("Id solicitud: ");
                int id = Integer.parseInt(sc.nextLine().trim());
                System.out.print("Id responsable: ");
                int idr = Integer.parseInt(sc.nextLine().trim());
                g.asignarResponsable(id, idr);
            } else if (op == 4) {
                System.out.print("Estado (enter = todos): ");
                String e = sc.nextLine().trim().toUpperCase();
                System.out.print("Tipo (enter = todos): ");
                String t = sc.nextLine().trim().toUpperCase();
                System.out.print("Legajo (enter = todos): ");
                String l = sc.nextLine().trim();
                List<Solicitud> r = g.buscar(e.equals("") ? null : e, t.equals("") ? null : t, l.equals("") ? null : l, 0);
                for (Solicitud s : r) {
                    System.out.println(s);
                }
                System.out.println(r.size() + " resultado(s)");
            } else if (op == 5) {
                System.out.print(g.generarReporte());
            } else if (op == 6) {
                if (g.guardar("solicitudes.csv")) System.out.println("Guardado OK");
            } else if (op == 7) {
                if (g.cargar("solicitudes.csv")) System.out.println("Cargado OK");
            }
        }
    }

    // escenario de demostracion con datos sinteticos
    static void demo(GestorSolicitudes g) {
        int a = g.registrarSolicitud("10001", "Ana Torres", "ana.torres@example.com", "CERTIFICADO", "Certificado de alumno regular", 0);
        int b = g.registrarSolicitud("10002", "Bruno Sosa", "bruno.sosa@example.com", "EQUIVALENCIA", "Equivalencia de Programacion I", 0);
        int c = g.registrarSolicitud("10003", "Carla Medina", "carla.medina@example.com", "REVISION_NOTA", "Revision del segundo parcial", 0);
        int d = g.registrarSolicitud("10001", "Ana Torres", "ana.torres@example.com", "BAJA_MATERIA", "Baja de Base de Datos I", 3);
        g.registrarSolicitud("123", "Dato Invalido", "sin-arroba", "CERTIFICADO", "x", 0);
        g.registrarSolicitud("10001", "Ana Torres", "ana.torres@example.com", "CERTIFICADO", "Duplicada", 0);
        g.asignarResponsable(a, 1);
        g.asignarResponsable(b, 1);
        g.asignarResponsable(b, 2);
        g.asignarResponsable(c, 4);
        g.cambiarEstado(a, "RESUELTA", "Emitido");
        g.cambiarEstado(b, "RECHAZADA", "");
        g.cambiarEstado(b, "RECHAZADA", "Programa no equivalente");
        g.cambiarEstado(d, "RESUELTA", "");
        g.cambiarEstado(a, "EN_PROCESO", "");
        System.out.println("-- Solicitudes abiertas --");
        for (Solicitud s : g.buscar(null, null, null, 0)) {
            if (!s.estado.esFinal()) {
                System.out.println(s);
            }
        }
        System.out.print(g.generarReporte());
    }
}
