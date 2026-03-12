package sistemaasignaciondecitasmedicas.modelos;

import sistemaasignaciondecitasmedicas.interfaces.Consultable;

/**
 * Clase Abstracta Persona: Base de Doctor y Paciente.
 * Implementa Consultable para cumplir el contrato de interfaz.
 */
public abstract class Persona implements Consultable {

    // ─── Atributos ─────────────────────────────────────────────────────────────
    protected String id;
    protected String nombre;
    protected String apellido;
    protected String correo;
    protected String telefono;
    protected String fechaNacimiento; // formato YYYY-MM-DD
    protected String genero;

    // ─── Constructor ───────────────────────────────────────────────────────────
    public Persona(String id, String nombre, String apellido,
                   String correo, String telefono, String fechaNacimiento, String genero) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
    }

    // ─── Método Abstracto ──────────────────────────────────────────────────────
    /**
     * Cada subclase muestra su información de forma específica.
     */
    public abstract String mostrarInformacion();

    // ─── Implementación de Consultable ─────────────────────────────────────────
    @Override
    public String getInfoContacto() {
        return String.format("📞 Tel: %s | ✉️  Correo: %s", telefono, correo);
    }

    // ─── Getters y Setters ─────────────────────────────────────────────────────
    public String getId()             { return id; }
    public String getNombre()         { return nombre; }
    public String getApellido()       { return apellido; }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    public String getCorreo()         { return correo; }
    public String getTelefono()       { return telefono; }
    public String getFechaNacimiento(){ return fechaNacimiento; }
    public String getGenero()         { return genero; }

    public void setCorreo(String correo)     { this.correo = correo; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s | %s", getTipo(), nombre, apellido, getInfoContacto());
    }
}
