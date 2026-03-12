package sistemaasignaciondecitasmedicas;

import sistemaasignaciondecitasmedicas.modelos.*;
import sistemaasignaciondecitasmedicas.servicios.GestorCitas;
import sistemaasignaciondecitasmedicas.persistencia.Persistencia;
import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║   SISTEMA DE CITAS MÉDICAS
 * ╚══════════════════════════════════════════════════════╝
 *
 * Clase Main: Punto de entrada. Menú principal del sistema.
 */
public class Main {

    private static final Scanner    scanner = new Scanner(System.in);
    private static final GestorCitas gestor = new GestorCitas();

    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║       🏥 SISTEMA DE CITAS MÉDICAS");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        // Cargar datos previos
        Persistencia.cargarPersonas(gestor);

        // Datos de demostración
        cargarDatosDemo();

        boolean ejecutando = true;
        while (ejecutando) {
            mostrarMenuPrincipal();
            int opcion = leerEnteroSeguro("Seleccione una opción: ");
            switch (opcion) 
            {
                case 1 -> menuDoctores();
                case 2 -> menuPacientes();
                case 3 -> menuCitas();
                case 4 -> menuAtencion();
                case 5 -> {
                    guardarYSalir(); ejecutando = false;
                }
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    // ─── MENÚ PRINCIPAL ────────────────────────────────────────────────────────
    private static void mostrarMenuPrincipal() {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("              MENÚ PRINCIPAL");
        System.out.println("══════════════════════════════════════════");
        System.out.println("  1. 👨‍⚕️  Gestión de Doctores");
        System.out.println("  2. 🧑  Gestión de Pacientes");
        System.out.println("  3. 📅  Gestión de Citas");
        System.out.println("  4. 🩺  Atención Médica (completar cita)");
        System.out.println("  5. 💾  Guardar y Salir");
        System.out.println("══════════════════════════════════════════");
    }

    // ─── MENÚ DOCTORES ─────────────────────────────────────────────────────────
    private static void menuDoctores() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n─── 👨‍⚕️ DOCTORES ───");
            System.out.println("  1. Listar todos los doctores");
            System.out.println("  2. Registrar nuevo doctor");
            System.out.println("  3. Ver información de doctor");
            System.out.println("  4. Ver citas de un doctor");
            System.out.println("  5. Buscar por especialidad");
            System.out.println("  0. Volver");

            int op = leerEnteroSeguro("Opción: ");
            switch (op) {
                case 1 -> System.out.println(gestor.listarDoctores());
                case 2 -> registrarDoctor();
                case 3 -> verInfoDoctor();
                case 4 -> verCitasDoctor();
                case 5 -> buscarEspecialidad();
                case 0 -> salir = true;
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    private static void registrarDoctor() {
        try {
            System.out.print("ID del doctor          : "); String id    = scanner.nextLine().trim();
            System.out.print("Nombre                 : "); String nom   = scanner.nextLine().trim();
            System.out.print("Apellido               : "); String ape   = scanner.nextLine().trim();
            System.out.print("Correo                 : "); String cor   = scanner.nextLine().trim();
            System.out.print("Teléfono               : "); String tel   = scanner.nextLine().trim();
            System.out.print("Fecha nacimiento (YYYY-MM-DD): "); String fn = scanner.nextLine().trim();
            System.out.print("Género (M/F)           : "); String gen   = scanner.nextLine().trim();
            System.out.print("Especialidad           : "); String esp   = scanner.nextLine().trim();
            System.out.print("Número de licencia     : "); String lic   = scanner.nextLine().trim();
            double val = leerDoubleSeguro("Valor consulta (COP)   : ");

            if (id.isEmpty() || nom.isEmpty()) throw new IllegalArgumentException("ID y nombre son obligatorios.");
            gestor.registrarDoctor(new Doctor(id, nom, ape, cor, tel, fn, gen, esp, lic, val));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verInfoDoctor() {
        System.out.print("ID del doctor: ");
        String id = scanner.nextLine().trim();
        try {
            Doctor d = gestor.buscarDoctor(id);
            if (d == null) System.out.println("❌ Doctor no encontrado.");
            else System.out.println(d.mostrarInformacion());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verCitasDoctor() {
        System.out.print("ID del doctor: ");
        String id = scanner.nextLine().trim();
        System.out.println(gestor.listarCitasDoctor(id));
    }

    private static void buscarEspecialidad() {
        System.out.print("Especialidad (ej: Cardiología): ");
        String esp = scanner.nextLine().trim();
        System.out.println(gestor.listarPorEspecialidad(esp));
    }

    // ─── MENÚ PACIENTES ────────────────────────────────────────────────────────
    private static void menuPacientes() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n─── 🧑 PACIENTES ───");
            System.out.println("  1. Listar todos los pacientes");
            System.out.println("  2. Registrar nuevo paciente");
            System.out.println("  3. Ver información de paciente");
            System.out.println("  4. Ver historial médico");
            System.out.println("  5. Ver citas de un paciente");
            System.out.println("  0. Volver");

            int op = leerEnteroSeguro("Opción: ");
            switch (op) {
                case 1 -> System.out.println(gestor.listarPacientes());
                case 2 -> registrarPaciente();
                case 3 -> verInfoPaciente();
                case 4 -> verHistorialPaciente();
                case 5 -> verCitasPaciente();
                case 0 -> salir = true;
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    private static void registrarPaciente() {
        try {
            System.out.print("ID del paciente        : "); String id  = scanner.nextLine().trim();
            System.out.print("Nombre                 : "); String nom = scanner.nextLine().trim();
            System.out.print("Apellido               : "); String ape = scanner.nextLine().trim();
            System.out.print("Correo                 : "); String cor = scanner.nextLine().trim();
            System.out.print("Teléfono               : "); String tel = scanner.nextLine().trim();
            System.out.print("Fecha nacimiento (YYYY-MM-DD): "); String fn = scanner.nextLine().trim();
            System.out.print("Género (M/F)           : "); String gen = scanner.nextLine().trim();
            System.out.print("Tipo de sangre (ej: O+): "); String san = scanner.nextLine().trim();
            System.out.print("EPS                    : "); String eps = scanner.nextLine().trim();
            System.out.print("Alergias (o 'ninguna') : "); String ale = scanner.nextLine().trim();

            if (id.isEmpty() || nom.isEmpty()) throw new IllegalArgumentException("ID y nombre son obligatorios.");
            gestor.registrarPaciente(new Paciente(id, nom, ape, cor, tel, fn, gen, san, eps, ale));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verInfoPaciente() {
        System.out.print("ID del paciente: ");
        String id = scanner.nextLine().trim();
        try {
            Paciente p = gestor.buscarPaciente(id);
            if (p == null) System.out.println("❌ Paciente no encontrado.");
            else System.out.println(p.mostrarInformacion());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verHistorialPaciente() {
        System.out.print("ID del paciente: ");
        String id = scanner.nextLine().trim();
        try {
            Paciente p = gestor.buscarPaciente(id);
            if (p == null) System.out.println("❌ Paciente no encontrado.");
            else System.out.println(p.verHistorial());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verCitasPaciente() {
        System.out.print("ID del paciente: ");
        String id = scanner.nextLine().trim();
        System.out.println(gestor.listarCitasPaciente(id));
    }

    // ─── MENÚ CITAS ────────────────────────────────────────────────────────────
    private static void menuCitas() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n─── 📅 CITAS MÉDICAS ───");
            System.out.println("  1. Listar todas las citas");
            System.out.println("  2. Agendar nueva cita");
            System.out.println("  3. Ver comprobante de cita");
            System.out.println("  4. Confirmar cita");
            System.out.println("  5. Cancelar cita");
            System.out.println("  0. Volver");

            int op = leerEnteroSeguro("Opción: ");
            switch (op) {
                case 1 -> System.out.println(gestor.listarCitas());
                case 2 -> agendarCita();
                case 3 -> verComprobante();
                case 4 -> confirmarCita();
                case 5 -> cancelarCita();
                case 0 -> salir = true;
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    private static void agendarCita() {
        try {
            System.out.println(gestor.listarDoctores());
            System.out.print("ID del doctor    : "); String idDoc = scanner.nextLine().trim();
            System.out.println(gestor.listarPacientes());
            System.out.print("ID del paciente  : "); String idPac = scanner.nextLine().trim();
            System.out.print("Fecha (YYYY-MM-DD): "); String fecha = scanner.nextLine().trim();
            System.out.print("Hora (HH:MM)     : "); String hora  = scanner.nextLine().trim();
            System.out.print("Motivo de consulta: "); String mot  = scanner.nextLine().trim();

            CitaMedica cita = gestor.agendarCita(idPac, idDoc, fecha, hora, mot);
            System.out.println(cita.generarComprobante());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verComprobante() {
        System.out.print("ID de la cita (ej: CITA-0001): ");
        String id = scanner.nextLine().trim();
        try {
            CitaMedica c = gestor.buscarCita(id);
            if (c == null) System.out.println("❌ Cita no encontrada.");
            else System.out.println(c.generarComprobante());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void confirmarCita() {
        System.out.print("ID de la cita: ");
        String id = scanner.nextLine().trim();
        try {
            CitaMedica c = gestor.buscarCita(id);
            if (c == null) { System.out.println("❌ Cita no encontrada."); return; }
            c.confirmar();
            System.out.println("✅ Cita " + id + " confirmada.");
        } catch (IllegalStateException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void cancelarCita() {
        System.out.print("ID de la cita: ");
        String id = scanner.nextLine().trim();
        try {
            gestor.cancelarCita(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ─── MENÚ ATENCIÓN ─────────────────────────────────────────────────────────
    private static void menuAtencion() {
        System.out.println("\n─── 🩺 ATENCIÓN MÉDICA ───");
        System.out.println(gestor.listarCitas());
        System.out.print("ID de la cita a atender: ");
        String id = scanner.nextLine().trim();
        try {
            CitaMedica c = gestor.buscarCita(id);
            if (c == null) { System.out.println("❌ Cita no encontrada."); return; }

            c.iniciarAtencion();
            System.out.println("🩺 Atención iniciada para: " + c.getPaciente().getNombreCompleto());
            System.out.println(c.getPaciente().verHistorial());

            System.out.print("Diagnóstico: "); String diag  = scanner.nextLine().trim();
            System.out.print("Receta     : "); String receta = scanner.nextLine().trim();

            c.completar(diag, receta);
            System.out.println(c.generarComprobante());
        } catch (IllegalStateException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ─── GUARDAR Y SALIR ───────────────────────────────────────────────────────
    private static void guardarYSalir() {
        Persistencia.guardarTodo(gestor);
        System.out.println("\n👋 Sistema cerrado. ¡Hasta luego!");
    }

    // ─── DATOS DE DEMOSTRACIÓN ─────────────────────────────────────────────────
    private static void cargarDatosDemo() {
        try {
            // Doctores
            gestor.registrarDoctor(new Doctor("D001", "Carlos", "Mendoza", "c.mendoza@clinica.com",
                    "3001112233", "1978-05-14", "M", "Cardiología", "LIC-8821", 120000));
            gestor.registrarDoctor(new Doctor("D002", "Laura", "Torres", "l.torres@clinica.com",
                    "3109998877", "1985-11-22", "F", "Pediatría", "LIC-4451", 90000));
            gestor.registrarDoctor(new Doctor("D003", "Andrés", "Ríos", "a.rios@clinica.com",
                    "3207776655", "1980-03-30", "M", "Neurología", "LIC-6630", 150000));
            gestor.registrarDoctor(new Doctor("D004", "Sofía", "Vargas", "s.vargas@clinica.com",
                    "3154445566", "1990-07-18", "F", "Medicina General", "LIC-2210", 70000));

            // Pacientes
            gestor.registrarPaciente(new Paciente("1024545900", "Johan", "Ramirez", "johan.ramirezp@cun.edu.co",
                    "3003578079", "1993-10-16", "M", "O+", "Sanitas", "Ninguna"));
            gestor.registrarPaciente(new Paciente("P002", "María", "López", "maria.l@email.com",
                    "3119876543", "1985-07-25", "F", "A-", "Nueva EPS", "Ninguna"));
            gestor.registrarPaciente(new Paciente("P003", "Nicolas", "Rodriguez", "johan.ramirezp@cun.edu.co",
                    "3208887766", "2000-12-01", "M", "B+", "Compensar", "Ibuprofeno"));
            gestor.registrarPaciente(new Paciente("P004", "Dilan", "", "@cun.edu.co",
                    "3153334455", "1975-04-19", "F", "AB+", "Salud Total", "Ninguna"));

            // Citas de demostración
            CitaMedica c1 = gestor.agendarCita("1024545900", "D001", "2026-03-10", "09:00", "Dolor en el pecho");
            c1.confirmar();

            CitaMedica c2 = gestor.agendarCita("P002", "D002", "2026-03-10", "10:00", "Control pediátrico");
            c2.confirmar();
            c2.iniciarAtencion();
            c2.completar("Desarrollo normal, sin anomalías", "Vitamina D 400 UI diaria");

            gestor.agendarCita("P003", "D004", "2026-03-11", "14:00", "Dolor de cabeza persistente");

            System.out.println("✅ Datos de demostración cargados.\n");
        } catch (Exception e) {
            System.out.println("⚠️  Error en datos demo: " + e.getMessage());
        }
    }

    // ─── UTILIDADES ────────────────────────────────────────────────────────────
    private static int leerEnteroSeguro(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠️  Ingrese un número entero válido.");
            }
        }
    }

    private static double leerDoubleSeguro(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("⚠️  Ingrese un número válido.");
            }
        }
    }
}
