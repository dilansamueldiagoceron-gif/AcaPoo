package sistemaasignaciondecitasmedicas.modelos;

import sistemaasignaciondecitasmedicas.interfaces.Agendable;
import java.util.ArrayList;

/**
 * Subclase Doctor: Médico que atiende las citas.
 * Hereda de Persona e implementa Agendable.
 */
public class Doctor extends Persona implements Agendable {

    private String especialidad;
    private String numeroLicencia;
    private double valorConsulta;
    private ArrayList<String> horariosOcupados; // formato "YYYY-MM-DD HH:MM"

    // ─── Constructor ───────────────────────────────────────────────────────────
    public Doctor(String id, String nombre, String apellido, String correo,
                  String telefono, String fechaNacimiento, String genero,
                  String especialidad, String numeroLicencia, double valorConsulta) {
        super(id, nombre, apellido, correo, telefono, fechaNacimiento, genero);
        this.especialidad = especialidad;
        this.numeroLicencia = numeroLicencia;
        this.valorConsulta = valorConsulta;
        this.horariosOcupados = new ArrayList<>();
    }

    // ─── @Override métodos abstractos ─────────────────────────────────────────
    @Override
    public String mostrarInformacion() {
        return String.format(
            "╔══ DR. %s %s ══╗\n" +
            "  Especialidad : %s\n" +
            "  Licencia     : %s\n" +
            "  Consulta     : $%.0f COP\n" +
            "  Contacto     : %s",
            nombre, apellido, especialidad, numeroLicencia,
            valorConsulta, getInfoContacto()
        );
    }

    @Override
    public String generarResumenConsulta() {
        return String.format("Dr. %s %s - %s (Lic. %s)", nombre, apellido, especialidad, numeroLicencia);
    }

    @Override
    public String getTipo() { return "Doctor"; }

    // ─── Implementación de Agendable ───────────────────────────────────────────
    @Override
    public String agendarCita(String fecha, String hora) {
        String clave = fecha + " " + hora;
        if (horariosOcupados.contains(clave)) {
            return "❌ El Dr. " + getNombreCompleto() + " ya tiene cita en " + clave;
        }
        horariosOcupados.add(clave);
        return "✅ Cita agendada con Dr. " + getNombreCompleto() + " el " + clave;
    }

    @Override
    public boolean cancelarCita(String idCita) {
        // Se maneja desde GestorCitas; aquí liberamos el horario
        return true;
    }

    @Override
    public boolean verificarDisponibilidad(String fecha, String hora) {
        return !horariosOcupados.contains(fecha + " " + hora);
    }

    /**
     * Libera un horario cuando se cancela una cita.
     */
    public void liberarHorario(String fecha, String hora) {
        horariosOcupados.remove(fecha + " " + hora);
    }

    // ─── Getters ───────────────────────────────────────────────────────────────
    public String getEspecialidad()    { return especialidad; }
    public String getNumeroLicencia()  { return numeroLicencia; }
    public double getValorConsulta()   { return valorConsulta; }

    public void setValorConsulta(double v) {
        if (v <= 0) throw new IllegalArgumentException("El valor debe ser mayor a 0.");
        this.valorConsulta = v;
    }

    /** Serializa a CSV */
    public String toCSV() {
        return String.format("Doctor,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.0f",
                id, nombre, apellido, correo, telefono,
                fechaNacimiento, genero, especialidad, numeroLicencia, valorConsulta);
    }
}
