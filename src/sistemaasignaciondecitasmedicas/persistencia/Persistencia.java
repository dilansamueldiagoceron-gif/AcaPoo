package sistemaasignaciondecitasmedicas.persistencia;

import sistemaasignaciondecitasmedicas.modelos.*;
import sistemaasignaciondecitasmedicas.servicios.GestorCitas;
import java.io.*;

/**
 * Clase Persistencia: Guarda y carga todos los datos del sistema en archivos CSV.
 */
public class Persistencia {

    private static final String ARCHIVO_PERSONAS = "personas.csv";
    private static final String ARCHIVO_CITAS    = "citas.csv";

    // ─── GUARDAR ───────────────────────────────────────────────────────────────

    /** Guarda doctores y pacientes en personas.csv */
    public static void guardarPersonas(GestorCitas gestor) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_PERSONAS))) {
            bw.write("tipo,id,nombre,apellido,correo,telefono,fechaNac,genero,campo1,campo2,campo3");
            bw.newLine();
            for (Doctor d : gestor.getDoctores().values()) {
                bw.write(d.toCSV());
                bw.newLine();
            }
            for (Paciente p : gestor.getPacientes().values()) {
                bw.write(p.toCSV());
                bw.newLine();
            }
            System.out.println("💾 Personas guardadas en " + ARCHIVO_PERSONAS);
        } catch (IOException e) {
            System.out.println("❌ Error guardando personas: " + e.getMessage());
        }
    }

    /** Guarda citas en citas.csv */
    public static void guardarCitas(GestorCitas gestor) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_CITAS))) {
            bw.write("idCita,idPaciente,idDoctor,fecha,hora,motivo,estado,diagnostico");
            bw.newLine();
            for (CitaMedica c : gestor.getCitas()) {
                bw.write(c.toCSV());
                bw.newLine();
            }
            System.out.println("💾 Citas guardadas en " + ARCHIVO_CITAS);
        } catch (IOException e) {
            System.out.println("❌ Error guardando citas: " + e.getMessage());
        }
    }

    // ─── CARGAR ────────────────────────────────────────────────────────────────

    /** Carga personas desde el CSV al gestor. */
    public static void cargarPersonas(GestorCitas gestor) {
        File archivo = new File(ARCHIVO_PERSONAS);
        if (!archivo.exists()) {
            System.out.println("⚠️  No se encontró archivo de personas. Se iniciará vacío.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            br.readLine(); // saltar encabezado
            int cargados = 0;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split(",");
                try {
                    if (d[0].equalsIgnoreCase("Doctor") && d.length >= 11) {
                        Doctor doc = new Doctor(d[1], d[2], d[3], d[4], d[5], d[6], d[7],
                                d[8], d[9], Double.parseDouble(d[10]));
                        gestor.registrarDoctor(doc);
                        cargados++;
                    } else if (d[0].equalsIgnoreCase("Paciente") && d.length >= 11) {
                        Paciente pac = new Paciente(d[1], d[2], d[3], d[4], d[5], d[6], d[7],
                                d[8], d[9], d[10]);
                        gestor.registrarPaciente(pac);
                        cargados++;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️  Línea ignorada: " + linea);
                }
            }
            System.out.println("📂 " + cargados + " persona(s) cargada(s) desde CSV.");
        } catch (IOException e) {
            System.out.println("❌ Error cargando personas: " + e.getMessage());
        }
    }

    /** Guarda todo el sistema de una vez.
     * @param gestor */
    public static void guardarTodo(GestorCitas gestor) {
        guardarPersonas(gestor);
        guardarCitas(gestor);
        System.out.println("✅ Sistema guardado correctamente.");
    }
}
