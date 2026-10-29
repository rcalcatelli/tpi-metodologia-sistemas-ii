package ar.edu.utn.tup.solicitudes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor principal del sistema. Hace todo: alta de solicitudes, estados,
 * responsables, busquedas, reportes y archivos.
 */
public class GestorSolicitudes {

    // contador de ids
    static int ultimoId = 0;

    public List<Solicitud> lst = new ArrayList<>();
    public List<Estudiante> ests = new ArrayList<>();
    public List<Responsable> resps = new ArrayList<>();

    public GestorSolicitudes() {
        ultimoId = 0;
        // responsables fijos
        resps.add(new Responsable(1, "Laura Gómez", Area.ALUMNADO));
        resps.add(new Responsable(2, "Martín Díaz", Area.DEPARTAMENTO_ACADEMICO));
        resps.add(new Responsable(3, "Sofía Pereyra", Area.BEDELIA));
        resps.add(new Responsable(4, "Javier Ruiz", Area.DEPARTAMENTO_ACADEMICO));
    }

    // registra una solicitud nueva. devuelve el id o -1 si hubo error
    public int registrarSolicitud(String legajo, String nombre, String email, String tipo, String desc, int prioridad) {
        // validar legajo
        if (legajo == null || legajo.trim().equals("")) {
            System.out.println("Error: legajo vacio");
            return -1;
        }
        for (int i = 0; i < legajo.length(); i++) {
            if (!Character.isDigit(legajo.charAt(i))) {
                System.out.println("Error: legajo invalido");
                return -1;
            }
        }
        if (legajo.length() < 5 || legajo.length() > 6) {
            System.out.println("Error: legajo invalido");
            return -1;
        }
        // validar nombre
        if (nombre == null || nombre.trim().equals("")) {
            System.out.println("Error: nombre vacio");
            return -1;
        }
        // validar email
        if (email == null || !email.contains("@") || !email.contains(".") || email.indexOf("@") == 0) {
            System.out.println("Error: email invalido");
            return -1;
        }
        // validar tipo
        TipoSolicitud tipoSolicitud = TipoSolicitud.desdeNombre(tipo).orElse(null);
        if (tipoSolicitud == null) {
            System.out.println("Error: tipo invalido");
            return -1;
        }
        // validar descripcion
        if (desc == null || desc.trim().equals("") || desc.length() > 500) {
            System.out.println("Error: descripcion invalida");
            return -1;
        }
        if (prioridad < 0 || prioridad > 3) {
            System.out.println("Error: prioridad invalida");
            return -1;
        }
        // no puede haber otra pendiente del mismo tipo para el mismo alumno
        for (Solicitud s : lst) {
            if (s.est.legajo.equals(legajo) && s.tipo == tipoSolicitud && !s.estado.esFinal()) {
                System.out.println("Error: ya existe una solicitud abierta de ese tipo");
                return -1;
            }
        }
        // buscar o crear estudiante
        Estudiante e = null;
        for (Estudiante x : ests) {
            if (x.legajo.equals(legajo)) {
                e = x;
            }
        }
        if (e == null) {
            e = new Estudiante(legajo, nombre, email);
            ests.add(e);
        }
        Solicitud s = new Solicitud();
        ultimoId++;
        s.id = ultimoId;
        s.est = e;
        s.tipo = tipoSolicitud;
        s.desc = desc;
        s.prioridad = prioridad == 0 ? tipoSolicitud.getPrioridadPorDefecto() : Prioridad.desdeCodigo(prioridad).orElseThrow();
        s.estado = Estado.PENDIENTE;
        s.fecha = LocalDate.now();
        s.obs = "";
        lst.add(s);
        System.out.println("Solicitud registrada con id " + s.id);
        return s.id;
    }

    // cambia el email de un estudiante
    public boolean actualizarEmailEstudiante(String legajo, String email) {
        if (email == null || !email.contains("@") || !email.contains(".") || email.indexOf("@") == 0) {
            System.out.println("Error: email invalido");
            return false;
        }
        for (Estudiante x : ests) {
            if (x.legajo.equals(legajo)) {
                x.email = email;
                return true;
            }
        }
        System.out.println("Error: estudiante no encontrado");
        return false;
    }

    public boolean cambiarEstado(int id, String nuevo, String obs) {
        Solicitud s = null;
        for (Solicitud x : lst) {
            if (x.id == id) {
                s = x;
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }
        // si ya esta cerrada no se puede tocar
        if (s.estado == Estado.RESUELTA || s.estado == Estado.RECHAZADA) {
            System.out.println("Error: la solicitud ya esta cerrada");
            return false;
        }
        Estado destino = Estado.desdeNombre(nuevo).orElse(null);
        if (destino == null || !s.estado.puedePasarA(destino)) {
            System.out.println("Error: transicion invalida");
            return false;
        }
        if (destino == Estado.RECHAZADA && (obs == null || obs.trim().equals(""))) {
            System.out.println("Error: para rechazar hay que indicar el motivo");
            return false;
        }
        if (destino == Estado.RESUELTA && s.resp == null) {
            System.out.println("Error: no se puede resolver sin responsable");
            return false;
        }
        s.estado = destino;
        if (destino.esFinal() && obs != null) {
            s.obs = obs;
        }
        System.out.println("Solicitud " + id + " ahora esta " + s.estado);
        return true;
    }

    public boolean asignarResponsable(int id, int idResp) {
        Solicitud s = null;
        for (Solicitud x : lst) {
            if (x.id == id) {
                s = x;
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }
        if (s.estado == Estado.RESUELTA || s.estado == Estado.RECHAZADA) {
            System.out.println("Error: la solicitud ya esta cerrada");
            return false;
        }
        Responsable r = null;
        for (Responsable x : resps) {
            if (x.id == idResp) {
                r = x;
            }
        }
        if (r == null) {
            System.out.println("Error: no existe el responsable " + idResp);
            return false;
        }
        Area area = s.tipo.getAreaResponsable();
        if (r.area != area) {
            System.out.println("Error: el responsable no pertenece al area " + area.getNombre());
            return false;
        }
        s.resp = r;
        if (s.estado == Estado.PENDIENTE) {
            s.estado = Estado.EN_PROCESO;
        }
        System.out.println("Solicitud " + id + " asignada a " + r.nombre);
        return true;
    }

    public Solicitud obtener(int id) {
        for (Solicitud x : lst) {
            if (x.id == id) {
                return x;
            }
        }
        return null;
    }

    // busca con filtros. null o 0 = no filtrar
    public List<Solicitud> buscar(String estado, String tipo, String legajo, int prioridad) {
        List<Solicitud> res = new ArrayList<>();
        for (Solicitud s : lst) {
            boolean ok = true;
            if (estado != null && !s.estado.name().equals(estado)) {
                ok = false;
            }
            if (tipo != null && !s.tipo.name().equals(tipo)) {
                ok = false;
            }
            if (legajo != null && !s.est.legajo.equals(legajo)) {
                ok = false;
            }
            if (prioridad != 0 && s.prioridad.getCodigo() != prioridad) {
                ok = false;
            }
            if (ok) {
                res.add(s);
            }
        }
        // ordenar por prioridad y despues por id (burbuja)
        for (int i = 0; i < res.size(); i++) {
            for (int j = 0; j < res.size() - 1 - i; j++) {
                Solicitud a = res.get(j);
                Solicitud b = res.get(j + 1);
                if (a.prioridad.compareTo(b.prioridad) > 0 || (a.prioridad == b.prioridad && a.id > b.id)) {
                    res.set(j, b);
                    res.set(j + 1, a);
                }
            }
        }
        return res;
    }

    public String generarReporte() {
        String d = "";
        d += "===== REPORTE DE SOLICITUDES =====\n";
        d += "Total: " + lst.size() + "\n";
        d += "-- Por estado --\n";
        int c = 0;
        for (Solicitud s : lst) {
            if (s.estado == Estado.PENDIENTE) c++;
        }
        d += "PENDIENTE: " + c + "\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.estado == Estado.EN_PROCESO) c++;
        }
        d += "EN_PROCESO: " + c + "\n";
        int res = 0;
        for (Solicitud s : lst) {
            if (s.estado == Estado.RESUELTA) res++;
        }
        d += "RESUELTA: " + res + "\n";
        int rech = 0;
        for (Solicitud s : lst) {
            if (s.estado == Estado.RECHAZADA) rech++;
        }
        d += "RECHAZADA: " + rech + "\n";
        d += "-- Por tipo --\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.tipo == TipoSolicitud.CERTIFICADO) c++;
        }
        d += "CERTIFICADO: " + c + "\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.tipo == TipoSolicitud.EQUIVALENCIA) c++;
        }
        d += "EQUIVALENCIA: " + c + "\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.tipo == TipoSolicitud.REVISION_NOTA) c++;
        }
        d += "REVISION_NOTA: " + c + "\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.tipo == TipoSolicitud.BAJA_MATERIA) c++;
        }
        d += "BAJA_MATERIA: " + c + "\n";
        d += "-- Por prioridad --\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.prioridad == Prioridad.ALTA) c++;
        }
        d += "ALTA: " + c + "\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.prioridad == Prioridad.MEDIA) c++;
        }
        d += "MEDIA: " + c + "\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.prioridad == Prioridad.BAJA) c++;
        }
        d += "BAJA: " + c + "\n";
        c = 0;
        for (Solicitud s : lst) {
            if (s.resp == null && !s.estado.esFinal()) c++;
        }
        d += "Sin responsable asignado: " + c + "\n";
        if (res + rech == 0) {
            d += "Tasa de resolucion: N/A\n";
        } else {
            double t = (double) res / (res + rech) * 100;
            t = Math.round(t * 10) / 10.0;
            d += "Tasa de resolucion: " + t + "%\n";
        }
        d += "==================================\n";
        return d;
    }

    // guarda todo en un csv
    public boolean guardar(String ruta) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(ruta));
            for (Solicitud s : lst) {
                String idR = "";
                if (s.resp != null) idR = "" + s.resp.id;
                pw.println(s.id + ";" + s.est.legajo + ";" + s.est.nombre + ";" + s.est.email + ";" + s.tipo + ";"
                        + s.desc.replace(";", ",") + ";" + s.prioridad.getCodigo() + ";" + s.estado + ";" + idR + ";" + s.fecha + ";"
                        + s.obs.replace(";", ","));
            }
            pw.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error al guardar");
            return false;
        }
    }

    // lee el csv y reemplaza lo que hay en memoria
    public boolean cargar(String ruta) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            lst.clear();
            ests.clear();
            String l;
            int max = 0;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(";", -1);
                Solicitud s = new Solicitud();
                s.id = Integer.parseInt(p[0]);
                Estudiante e = null;
                for (Estudiante x : ests) {
                    if (x.legajo.equals(p[1])) {
                        e = x;
                    }
                }
                if (e == null) {
                    e = new Estudiante(p[1], p[2], p[3]);
                    ests.add(e);
                }
                s.est = e;
                s.tipo = TipoSolicitud.valueOf(p[4]);
                s.desc = p[5];
                s.prioridad = Prioridad.desdeCodigo(Integer.parseInt(p[6])).orElseThrow();
                s.estado = Estado.valueOf(p[7]);
                if (!p[8].equals("")) {
                    for (Responsable x : resps) {
                        if (x.id == Integer.parseInt(p[8])) {
                            s.resp = x;
                        }
                    }
                }
                s.fecha = LocalDate.parse(p[9]);
                s.obs = p[10];
                lst.add(s);
                if (s.id > max) max = s.id;
            }
            br.close();
            ultimoId = max;
            return true;
        } catch (Exception e) {
            System.out.println("Error al cargar");
            return false;
        }
    }

    // TODO: borrar, ya no se usa
    // public void proc() {
    //     for (Solicitud s : lst) {
    //         if (s.prioridad == 1) System.out.println(s);
    //     }
    // }

    public List<Responsable> getResps() {
        return resps;
    }
}
