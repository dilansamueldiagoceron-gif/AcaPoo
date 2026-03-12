package sistemaasignaciondecitasmedicas.modelos;

/**
 * Clase CitaMedica: Representa una cita médica entre un paciente y un doctor.
 */
public class CitaMedica {

    // ─── Estados posibles ──────────────────────────────────────────────────────
    public static final String PENDIENTE   = "Pendiente";
    public static final String CONFIRMADA  = "Confirmada";
    public static final String EN_CURSO    = "En curso";
    public static final String COMPLETADA  = "Completada";
    public static final String CANCELADA   = "Cancelada";

    // ─── Atributos ─────────────────────────────────────────────────────────────
    private String idCita;
    private Paciente paciente;
    private Doctor doctor;
    private String fecha;         // YYYY-MM-DD
    private String hora;          // HH:MM
    private String motivo;
    private String estado;
    private String diagnostico;   // se llena al completar la cita
    private String receta;        // se llena al completar la cita

    // ─── Constructor ───────────────────────────────────────────────────────────
    public CitaMedica(String idCita, Paciente paciente, Doctor doctor,
                      String fecha, String hora, String motivo) {
        this.idCita    = idCita;
        this.paciente  = paciente;
        this.doctor    = doctor;
        this.fecha     = fecha;
        this.hora      = hora;
        this.motivo    = motivo;
        this.estado    = PENDIENTE;
        this.diagnostico = "";
        this.receta      = "";
    }

    // ─── Métodos ───────────────────────────────────────────────────────────────

    /**
     * Confirma la cita si está en estado Pendiente.
     */
    public void confirmar() {
        if (!estado.equals(PENDIENTE))
            throw new IllegalStateException("Solo se pueden confirmar citas Pendientes.");
        estado = CONFIRMADA;
    }

    /**
     * Inicia la atención de la cita.
     */
    public void iniciarAtencion() {
        if (!estado.equals(CONFIRMADA))
            throw new IllegalStateException("La cita debe estar Confirmada para iniciar atención.");
        estado = EN_CURSO;
    }

    /**
     * Completa la cita con diagnóstico y receta.
     * @param diagnostico resultado médico
     * @param receta      medicamentos recetados
     */
    public void completar(String diagnostico, String receta) {
        if (!estado.equals(EN_CURSO))
            throw new IllegalStateException("La cita debe estar En curso para completarse.");
        this.diagnostico = diagnostico;
        this.receta = receta;
        this.estado = COMPLETADA;
        // Registrar diagnóstico en el historial del paciente
        paciente.agregarDiagnostico(fecha + " - Dr. " + doctor.getNombreCompleto()
                + " (" + doctor.getEspecialidad() + "): " + diagnostico);
    }

    /**
     * Cancela la cita y libera el horario del doctor.
     */
    public void cancelar() {
        if (estado.equals(COMPLETADA))
            throw new IllegalStateException("No se puede cancelar una cita ya completada.");
        doctor.liberarHorario(fecha, hora);
        estado = CANCELADA;
    }

    /**
     * Genera el comprobante completo de la cita.
     */
    public String generarComprobante() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n══════════════════════════════════════════════════\n");
        sb.append(String.format("        COMPROBANTE DE CITA #%s\n", idCita));
        sb.append("══════════════════════════════════════════════════\n");
        sb.append(String.format("  Estado    : %s\n", estado));
        sb.append(String.format("  Fecha     : %s a las %s\n", fecha, hora));
        sb.append(String.format("  Paciente  : %s\n", paciente.getNombreCompleto()));
        sb.append(String.format("  EPS       : %s\n", paciente.getEps()));
        sb.append(String.format("  Doctor    : Dr. %s\n", doctor.getNombreCompleto()));
        sb.append(String.format("  Especialidad: %s\n", doctor.getEspecialidad()));
        sb.append(String.format("  Motivo    : %s\n", motivo));
        sb.append(String.format("  Valor     : $%.0f COP\n", doctor.getValorConsulta()));
        if (!diagnostico.isEmpty()) {
            sb.append("──────────────────────────────────────────────────\n");
            sb.append(String.format("  Diagnóstico: %s\n", diagnostico));
            sb.append(String.format("  Receta     : %s\n", receta));
        }
        sb.append("══════════════════════════════════════════════════\n");
        return sb.toString();
    }

    // ─── Getters ───────────────────────────────────────────────────────────────
    public String    getIdCita()    { return idCita; }
    public Paciente  getPaciente()  { return paciente; }
    public Doctor    getDoctor()    { return doctor; }
    public String    getFecha()     { return fecha; }
    public String    getHora()      { return hora; }
    public String    getMotivo()    { return motivo; }
    public String    getEstado()    { return estado; }
    public String    getDiagnostico(){ return diagnostico; }
    public String    getReceta()    { return receta; }

    /** Serializa a CSV */
    public String toCSV() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                idCita, paciente.getId(), doctor.getId(),
                fecha, hora, motivo, estado,
                diagnostico.replace(",", ";"));
    }

    @Override
    public String toString() {
        return String.format("Cita #%s | %s | Dr. %s | Paciente: %s | Estado: %s",
                idCita, fecha + " " + hora,
                doctor.getNombreCompleto(),
                paciente.getNombreCompleto(), estado);
    }
}
