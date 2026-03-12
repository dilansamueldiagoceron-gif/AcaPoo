package sistemaasignaciondecitasmedicas.servicios;

import sistemaasignaciondecitasmedicas.modelos.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase GestorCitas: Administra doctores, pacientes y citas médicas.
 * Usa HashMap para búsquedas eficientes y ArrayList para listados.
 */
public class GestorCitas {

    // ─── Colecciones ───────────────────────────────────────────────────────────
    private HashMap<String, Doctor>     doctores;   // id -> Doctor
    private HashMap<String, Paciente>   pacientes;  // id -> Paciente
    private ArrayList<CitaMedica>       citas;
    private HashMap<String, ArrayList<CitaMedica>> citasPorEspecialidad;

    private int contadorCitas;

    // ─── Constructor ───────────────────────────────────────────────────────────
    public GestorCitas() {
        doctores              = new HashMap<>();
        pacientes             = new HashMap<>();
        citas                 = new ArrayList<>();
        citasPorEspecialidad  = new HashMap<>();
        contadorCitas         = 1;
    }

    // ─── DOCTORES ──────────────────────────────────────────────────────────────

    /** Registra un nuevo doctor. */
    public void registrarDoctor(Doctor d) {
        if (doctores.containsKey(d.getId()))
            throw new IllegalArgumentException("Ya existe un doctor con ID: " + d.getId());
        doctores.put(d.getId(), d);
        citasPorEspecialidad.computeIfAbsent(d.getEspecialidad(), k -> new ArrayList<>());
        System.out.println("✅ Doctor registrado: Dr. " + d.getNombreCompleto());
    }

    /** Busca un doctor por ID. */
    public Doctor buscarDoctor(String id) {
        return doctores.get(id);
    }

    /** Lista todos los doctores. */
    public String listarDoctores() {
        if (doctores.isEmpty()) return "⚠️  No hay doctores registrados.";
        StringBuilder sb = new StringBuilder("\n👨‍⚕️ DOCTORES DISPONIBLES:\n");
        sb.append("═══════════════════════════════════════════════════════\n");
        for (Doctor d : doctores.values()) {
            sb.append(String.format("  [%s] Dr. %-25s | %-20s | $%.0f COP%n",
                    d.getId(), d.getNombreCompleto(), d.getEspecialidad(), d.getValorConsulta()));
        }
        return sb.toString();
    }

    /** Lista doctores por especialidad. */
    public String listarPorEspecialidad(String especialidad) {
        StringBuilder sb = new StringBuilder("\n🔹 Especialidad: " + especialidad + "\n");
        boolean encontrado = false;
        for (Doctor d : doctores.values()) {
            if (d.getEspecialidad().equalsIgnoreCase(especialidad)) {
                sb.append("  ").append(d.toString()).append("\n");
                encontrado = true;
            }
        }
        if (!encontrado) sb.append("  No hay doctores en esta especialidad.\n");
        return sb.toString();
    }

    // ─── PACIENTES ─────────────────────────────────────────────────────────────

    /** Registra un nuevo paciente. */
    public void registrarPaciente(Paciente p) {
        if (pacientes.containsKey(p.getId()))
            throw new IllegalArgumentException("Ya existe un paciente con ID: " + p.getId());
        pacientes.put(p.getId(), p);
        System.out.println("✅ Paciente registrado: " + p.getNombreCompleto());
    }

    /** Busca un paciente por ID. */
    public Paciente buscarPaciente(String id) {
        return pacientes.get(id);
    }

    /** Lista todos los pacientes. */
    public String listarPacientes() {
        if (pacientes.isEmpty()) return "⚠️  No hay pacientes registrados.";
        StringBuilder sb = new StringBuilder("\n🧑‍🤝‍🧑 PACIENTES REGISTRADOS:\n");
        sb.append("═══════════════════════════════════════════════════════\n");
        for (Paciente p : pacientes.values()) {
            sb.append(String.format("  [%s] %-25s | EPS: %-15s | Sangre: %s%n",
                    p.getId(), p.getNombreCompleto(), p.getEps(), p.getTipoSangre()));
        }
        return sb.toString();
    }

    // ─── CITAS ─────────────────────────────────────────────────────────────────

    /**
     * Agenda una nueva cita médica.
     * Valida disponibilidad del doctor antes de crear la cita.
     */
    public CitaMedica agendarCita(String idPaciente, String idDoctor,
                                   String fecha, String hora, String motivo) {
        Paciente paciente = buscarPaciente(idPaciente);
        Doctor doctor     = buscarDoctor(idDoctor);

        if (paciente == null) throw new IllegalArgumentException("Paciente no encontrado: " + idPaciente);
        if (doctor == null)   throw new IllegalArgumentException("Doctor no encontrado: " + idDoctor);
        if (!doctor.verificarDisponibilidad(fecha, hora))
            throw new IllegalStateException("El Dr. " + doctor.getNombreCompleto()
                    + " no está disponible el " + fecha + " a las " + hora);

        String idCita = String.format("CITA-%04d", contadorCitas++);
        CitaMedica cita = new CitaMedica(idCita, paciente, doctor, fecha, hora, motivo);
        doctor.agendarCita(fecha, hora); // bloquea el horario
        citas.add(cita);
        citasPorEspecialidad.computeIfAbsent(doctor.getEspecialidad(), k -> new ArrayList<>()).add(cita);

        System.out.println("📅 " + cita);
        return cita;
    }

    /** Busca una cita por ID. */
    public CitaMedica buscarCita(String idCita) {
        for (CitaMedica c : citas) {
            if (c.getIdCita().equalsIgnoreCase(idCita)) return c;
        }
        return null;
    }

    /** Lista todas las citas. */
    public String listarCitas() {
        if (citas.isEmpty()) return "⚠️  No hay citas registradas.";
        StringBuilder sb = new StringBuilder("\n📅 CITAS MÉDICAS:\n");
        sb.append("═══════════════════════════════════════════════════════\n");
        for (CitaMedica c : citas) sb.append("  ").append(c).append("\n");
        return sb.toString();
    }

    /** Lista citas de un paciente específico. */
    public String listarCitasPaciente(String idPaciente) {
        StringBuilder sb = new StringBuilder("\n📋 Citas del paciente:\n");
        boolean encontrado = false;
        for (CitaMedica c : citas) {
            if (c.getPaciente().getId().equalsIgnoreCase(idPaciente)) {
                sb.append("  ").append(c).append("\n");
                encontrado = true;
            }
        }
        if (!encontrado) sb.append("  No tiene citas registradas.\n");
        return sb.toString();
    }

    /** Lista citas de un doctor específico. */
    public String listarCitasDoctor(String idDoctor) {
        StringBuilder sb = new StringBuilder("\n📋 Citas del doctor:\n");
        boolean encontrado = false;
        for (CitaMedica c : citas) {
            if (c.getDoctor().getId().equalsIgnoreCase(idDoctor)) {
                sb.append("  ").append(c).append("\n");
                encontrado = true;
            }
        }
        if (!encontrado) sb.append("  No tiene citas registradas.\n");
        return sb.toString();
    }

    /** Cancela una cita por ID. */
    public void cancelarCita(String idCita) {
        CitaMedica c = buscarCita(idCita);
        if (c == null) throw new IllegalArgumentException("Cita no encontrada: " + idCita);
        c.cancelar();
        System.out.println("🚫 Cita " + idCita + " cancelada.");
    }

    // ─── Getters ───────────────────────────────────────────────────────────────
    public HashMap<String, Doctor>   getDoctores()  { return doctores; }
    public HashMap<String, Paciente> getPacientes() { return pacientes; }
    public ArrayList<CitaMedica>     getCitas()     { return citas; }
}
