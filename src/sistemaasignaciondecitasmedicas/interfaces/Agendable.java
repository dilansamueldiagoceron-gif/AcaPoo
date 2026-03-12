package sistemaasignaciondecitasmedicas.interfaces;

/**
 * Interfaz Agendable: Define el comportamiento de entidades que pueden
 * agendar o gestionar citas médicas.
 */
public interface Agendable {

    /**
     * Agenda una cita para una fecha y hora dadas.
     * @param fecha  fecha en formato YYYY-MM-DD
     * @param hora   hora en formato HH:MM
     * @return mensaje confirmando o rechazando la cita
     */
    String agendarCita(String fecha, String hora);

    /**
     * Cancela una cita existente por su ID.
     * @param idCita identificador único de la cita
     * @return true si se canceló correctamente
     */
    boolean cancelarCita(String idCita);

    /**
     * Verifica si está disponible en una fecha y hora dadas.
     * @param fecha fecha a verificar
     * @param hora  hora a verificar
     * @return true si está disponible
     */
    boolean verificarDisponibilidad(String fecha, String hora);
}
