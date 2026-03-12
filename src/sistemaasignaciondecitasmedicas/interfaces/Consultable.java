package sistemaasignaciondecitasmedicas.interfaces;

/**
 * Interfaz Consultable: Define el comportamiento de entidades que participan
 * en una consulta médica.
 */
public interface Consultable {

    /**
     * Genera el resumen de la consulta médica.
     * @return resumen en texto de la consulta
     */
    String generarResumenConsulta();

    /**
     * Retorna la información de contacto de la entidad.
     * @return información de contacto como String
     */
    String getInfoContacto();

    /**
     * Retorna el tipo de entidad (Doctor, Paciente, etc.).
     * @return tipo como String
     */
    String getTipo();
}
