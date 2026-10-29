package ar.edu.utn.tup.solicitudes.ui;

import ar.edu.utn.tup.solicitudes.GestorSolicitudes;
import ar.edu.utn.tup.solicitudes.Solicitud;

import java.io.PrintStream;

/** Escenario de demostración con datos sintéticos; su salida es el golden master. */
public class EscenarioDemo {

    private final GestorSolicitudes gestor;
    private final PrintStream salida;

    public EscenarioDemo(GestorSolicitudes gestor, PrintStream salida) {
        this.gestor = gestor;
        this.salida = salida;
    }

    public void ejecutar() {
        int certificado = gestor.registrarSolicitud("10001", "Ana Torres", "ana.torres@example.com", "CERTIFICADO", "Certificado de alumno regular", 0);
        int equivalencia = gestor.registrarSolicitud("10002", "Bruno Sosa", "bruno.sosa@example.com", "EQUIVALENCIA", "Equivalencia de Programacion I", 0);
        int revision = gestor.registrarSolicitud("10003", "Carla Medina", "carla.medina@example.com", "REVISION_NOTA", "Revision del segundo parcial", 0);
        int baja = gestor.registrarSolicitud("10001", "Ana Torres", "ana.torres@example.com", "BAJA_MATERIA", "Baja de Base de Datos I", 3);
        gestor.registrarSolicitud("123", "Dato Invalido", "sin-arroba", "CERTIFICADO", "x", 0);
        gestor.registrarSolicitud("10001", "Ana Torres", "ana.torres@example.com", "CERTIFICADO", "Duplicada", 0);
        gestor.asignarResponsable(certificado, 1);
        gestor.asignarResponsable(equivalencia, 1);
        gestor.asignarResponsable(equivalencia, 2);
        gestor.asignarResponsable(revision, 4);
        gestor.cambiarEstado(certificado, "RESUELTA", "Emitido");
        gestor.cambiarEstado(equivalencia, "RECHAZADA", "");
        gestor.cambiarEstado(equivalencia, "RECHAZADA", "Programa no equivalente");
        gestor.cambiarEstado(baja, "RESUELTA", "");
        gestor.cambiarEstado(certificado, "EN_PROCESO", "");
        salida.println("-- Solicitudes abiertas --");
        gestor.buscar(null, null, null, 0).stream()
                .filter(Solicitud::estaAbierta)
                .forEach(salida::println);
        salida.print(gestor.generarReporte());
    }
}
