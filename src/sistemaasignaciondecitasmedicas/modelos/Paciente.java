package sistemaasignaciondecitasmedicas.modelos;

import java.util.ArrayList;

/**
 * Subclase Paciente: Persona que solicita atención médica.
 * Hereda de Persona y gestiona su propio historial.
 */
public class Paciente extends Persona {

    private String tipoSangre;
    private String eps;                           // Entidad promotora de salud
    private String alergias;
    private ArrayList<String> historialDiagnosticos; // diagnósticos previos

    // ─── Constructor ───────────────────────────────────────────────────────────
    public Paciente(String id, String nombre, String apellido, String correo,
                    String telefono, String fechaNacimiento, String genero,
                    String tipoSangre, String eps, String alergias) {
        super(id, nombre, apellido, correo, telefono, fechaNacimiento, genero);
        this.tipoSangre = tipoSangre;
        this.eps = eps;
        this.alergias = alergias;
        this.historialDiagnosticos = new ArrayList<>();
    }

    // ─── @Override ─────────────────────────────────────────────────────────────
    @Override
    public String mostrarInformacion() {
        return String.format(
            "╔══ PACIENTE: %s %s ══╗\n" +
            "  EPS          : %s\n" +
            "  Tipo sangre  : %s\n" +
            "  Alergias     : %s\n" +
            "  Diagnósticos : %d registrado(s)\n" +
            "  Contacto     : %s",
            nombre, apellido, eps, tipoSangre,
            alergias.isEmpty() ? "Ninguna" : alergias,
            historialDiagnosticos.size(), getInfoContacto()
        );
    }

    @Override
    public String generarResumenConsulta() {
        return String.format("Paciente: %s %s | EPS: %s | Sangre: %s",
                nombre, apellido, eps, tipoSangre);
    }

    @Override
    public String getTipo() { return "Paciente"; }

    // ─── Métodos ───────────────────────────────────────────────────────────────

    /**
     * Agrega un diagnóstico al historial médico del paciente.
     */
    public void agregarDiagnostico(String diagnostico) {
        historialDiagnosticos.add(diagnostico);
    }

    /**
     * Muestra el historial de diagnósticos del paciente.
     */
    public String verHistorial() {
        if (historialDiagnosticos.isEmpty()) {
            return "📋 " + getNombreCompleto() + " no tiene diagnósticos previos.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📋 Historial de ").append(getNombreCompleto()).append(":\n");
        for (int i = 0; i < historialDiagnosticos.size(); i++) {
            sb.append(String.format("  %d. %s%n", i + 1, historialDiagnosticos.get(i)));
        }
        return sb.toString();
    }

    // ─── Getters ───────────────────────────────────────────────────────────────
    public String getTipoSangre()  { return tipoSangre; }
    public String getEps()         { return eps; }
    public String getAlergias()    { return alergias; }
    public ArrayList<String> getHistorialDiagnosticos() { return historialDiagnosticos; }

    /** Serializa a CSV */
    public String toCSV() {
        return String.format("Paciente,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                id, nombre, apellido, correo, telefono,
                fechaNacimiento, genero, tipoSangre, eps, alergias);
    }
}
