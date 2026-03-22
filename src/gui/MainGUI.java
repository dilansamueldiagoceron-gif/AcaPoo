package sistemaasignaciondecitasmedicas.gui;

import sistemaasignaciondecitasmedicas.modelos.*;
import sistemaasignaciondecitasmedicas.servicios.GestorCitas;
import sistemaasignaciondecitasmedicas.persistencia.Persistencia;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║   INTERFAZ GRÁFICA - SISTEMA DE CITAS MÉDICAS        ║
 * ║   Reemplaza el Main.java de consola                  ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * Para usar: ejecutar MainGUI.java en lugar de Main.java
 */
public class MainGUI extends JFrame {

    // ─── Colores del sistema ──────────────────────────────────────────────────
    public static final Color COLOR_PRIMARIO    = new Color(10, 85, 140);
    public static final Color COLOR_SECUNDARIO  = new Color(0, 140, 100);
    public static final Color COLOR_ACENTO      = new Color(230, 80, 50);
    public static final Color COLOR_FONDO       = new Color(245, 248, 252);
    public static final Color COLOR_PANEL       = new Color(255, 255, 255);
    public static final Color COLOR_TEXTO       = new Color(30, 40, 55);
    public static final Color COLOR_BORDE       = new Color(210, 220, 235);

    // ─── Fuentes ──────────────────────────────────────────────────────────────
    public static final Font FUENTE_TITULO  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FUENTE_SUBTIT  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_NORMAL  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_PEQUEÑA = new Font("Segoe UI", Font.PLAIN, 11);

    private GestorCitas gestor;
    private JTabbedPane tabbedPane;
    private PanelDoctores  panelDoctores;
    private PanelPacientes panelPacientes;
    private PanelCitas     panelCitas;
    private PanelAtencion  panelAtencion;
    private JLabel         statusLabel;

    public MainGUI() {
        gestor = new GestorCitas();
        aplicarLookAndFeel();
        Persistencia.cargarPersonas(gestor);
        cargarDatosDemo();
        initComponents();
    }

    private void aplicarLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", COLOR_FONDO);
            UIManager.put("Table.background", COLOR_PANEL);
            UIManager.put("Table.alternateRowColor", new Color(238, 244, 252));
            UIManager.put("Table.selectionBackground", new Color(10, 85, 140, 60));
            UIManager.put("Table.selectionForeground", COLOR_TEXTO);
            UIManager.put("TabbedPane.selected", COLOR_PRIMARIO);
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setTitle("Sistema de Citas Médicas");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(960, 600));
        getContentPane().setBackground(COLOR_FONDO);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });

        setLayout(new BorderLayout(0, 0));

        // ─── Header ──────────────────────────────────────────────────────────
        add(crearHeader(), BorderLayout.NORTH);

        // ─── Paneles por pestaña ──────────────────────────────────────────────
        panelDoctores  = new PanelDoctores(gestor, this);
        panelPacientes = new PanelPacientes(gestor, this);
        panelCitas     = new PanelCitas(gestor, this);
        panelAtencion  = new PanelAtencion(gestor, this);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(FUENTE_NORMAL);
        tabbedPane.setBackground(COLOR_FONDO);

        tabbedPane.addTab("  👨‍⚕️  Doctores  ",  panelDoctores);
        tabbedPane.addTab("  🧑  Pacientes  ", panelPacientes);
        tabbedPane.addTab("  📅  Citas      ", panelCitas);
        tabbedPane.addTab("  🩺  Atención   ", panelAtencion);

        // Refrescar pestaña al seleccionarla
        tabbedPane.addChangeListener(e -> {
            Component comp = tabbedPane.getSelectedComponent();
            if (comp instanceof Refreshable) ((Refreshable) comp).refresh();
        });

        add(tabbedPane, BorderLayout.CENTER);

        // ─── Barra de estado ─────────────────────────────────────────────────
        statusLabel = new JLabel("  ✅  Sistema listo — datos de demostración cargados");
        statusLabel.setFont(FUENTE_PEQUEÑA);
        statusLabel.setForeground(new Color(80, 100, 120));
        statusLabel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, COLOR_BORDE),
                new EmptyBorder(5, 10, 5, 10)));
        statusLabel.setBackground(new Color(235, 242, 252));
        statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.SOUTH);
    }

    // ─── Header principal ─────────────────────────────────────────────────────
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARIO);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Logo y título
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel icono = new JLabel("🏥");
        icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titlePanel.setOpaque(false);
        JLabel titulo = new JLabel("Sistema de Citas Médicas");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Gestión integral de doctores, pacientes y citas");
        subtitulo.setFont(FUENTE_PEQUEÑA);
        subtitulo.setForeground(new Color(180, 210, 240));
        titlePanel.add(titulo);
        titlePanel.add(subtitulo);
        left.add(icono);
        left.add(titlePanel);
        header.add(left, BorderLayout.WEST);

        // Botón guardar
        JButton btnGuardar = crearBotonHeader("💾  Guardar datos", COLOR_SECUNDARIO);
        btnGuardar.addActionListener(e -> {
            Persistencia.guardarTodo(gestor);
            setStatus("✅  Datos guardados correctamente");
            JOptionPane.showMessageDialog(this, "Datos guardados correctamente en personas.csv y citas.csv",
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
        });
        header.add(btnGuardar, BorderLayout.EAST);

        return header;
    }

    private JButton crearBotonHeader(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // ─── Métodos públicos ─────────────────────────────────────────────────────
    public void setStatus(String mensaje) {
        statusLabel.setText("  " + mensaje);
    }

    public void refreshAll() {
        panelDoctores.refresh();
        panelPacientes.refresh();
        panelCitas.refresh();
        panelAtencion.refresh();
    }

    public GestorCitas getGestor() {
        return gestor;
    }

    // ─── Confirmar salida ─────────────────────────────────────────────────────
    private void confirmarSalida() {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desea guardar los datos antes de salir?",
                "Cerrar sistema", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            Persistencia.guardarTodo(gestor);
            dispose();
        } else if (op == JOptionPane.NO_OPTION) {
            dispose();
        }
    }

    // ─── Datos de demostración ────────────────────────────────────────────────
    private void cargarDatosDemo() {
        try {
            gestor.registrarDoctor(new Doctor("D001", "Carlos", "Mendoza",
                    "c.mendoza@clinica.com", "3001112233", "1978-05-14", "M",
                    "Cardiología", "LIC-8821", 120000));
            gestor.registrarDoctor(new Doctor("D002", "Laura", "Torres",
                    "l.torres@clinica.com", "3109998877", "1985-11-22", "F",
                    "Pediatría", "LIC-4451", 90000));
            gestor.registrarDoctor(new Doctor("D003", "Andrés", "Ríos",
                    "a.rios@clinica.com", "3207776655", "1980-03-30", "M",
                    "Neurología", "LIC-6630", 150000));
            gestor.registrarDoctor(new Doctor("D004", "Sofía", "Vargas",
                    "s.vargas@clinica.com", "3154445566", "1990-07-18", "F",
                    "Medicina General", "LIC-2210", 70000));

            gestor.registrarPaciente(new Paciente("1024545900", "Giovan", "Ramirez",
                    "johan.ramirezp@cun.edu.co", "3003578079", "1993-10-16", "M",
                    "O+", "Sanitas", "Ninguna"));
            gestor.registrarPaciente(new Paciente("P002", "María", "López",
                    "maria.l@email.com", "3119876543", "1985-07-25", "F",
                    "A-", "Nueva EPS", "Ninguna"));
            gestor.registrarPaciente(new Paciente("P003", "Nicolas", "Rodriguez",
                    "n.rodriguez@email.com", "3208887766", "2000-12-01", "M",
                    "B+", "Compensar", "Ibuprofeno"));
            gestor.registrarPaciente(new Paciente("P004", "Dilan", "García",
                    "d.garcia@cun.edu.co", "3153334455", "1975-04-19", "M",
                    "AB+", "Salud Total", "Ninguna"));

            CitaMedica c1 = gestor.agendarCita("1024545900", "D001", "2026-03-20", "09:00", "Dolor en el pecho");
            c1.confirmar();
            CitaMedica c2 = gestor.agendarCita("P002", "D002", "2026-03-20", "10:00", "Control pediátrico");
            c2.confirmar();
            c2.iniciarAtencion();
            c2.completar("Desarrollo normal, sin anomalías", "Vitamina D 400 UI diaria");
            gestor.agendarCita("P003", "D004", "2026-03-21", "14:00", "Dolor de cabeza persistente");
            CitaMedica c4 = gestor.agendarCita("P004", "D003", "2026-03-22", "11:00", "Migraña severa");
            c4.confirmar();

        } catch (Exception e) {
            System.out.println("⚠️ Error en datos demo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
