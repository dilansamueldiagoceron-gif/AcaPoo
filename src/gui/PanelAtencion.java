package sistemaasignaciondecitasmedicas.gui;

import sistemaasignaciondecitasmedicas.modelos.*;
import sistemaasignaciondecitasmedicas.servicios.GestorCitas;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Atención Médica.
 * Permite seleccionar una cita confirmada, ver el historial del paciente
 * y completar la cita con diagnóstico y receta.
 */
public class PanelAtencion extends JPanel implements Refreshable {

    private final GestorCitas gestor;
    private final MainGUI     mainGUI;

    private DefaultTableModel tableModel;
    private JTable            tabla;

    // Panel de atención activo
    private JPanel    panelAtencionActiva;
    private JLabel    lblCitaInfo;
    private JLabel    lblPacienteInfo;
    private JTextArea txtHistorial;
    private JTextArea txtDiagnostico;
    private JTextArea txtReceta;
    private JButton   btnIniciar;
    private JButton   btnCompletar;

    private CitaMedica citaActual = null;

    private static final String[] COLUMNAS = {
        "ID Cita", "Fecha", "Hora", "Paciente", "Doctor", "Estado"
    };

    public PanelAtencion(GestorCitas gestor, MainGUI mainGUI) {
        this.gestor  = gestor;
        this.mainGUI = mainGUI;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setBackground(MainGUI.COLOR_FONDO);
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // ─── Panel izquierdo: lista de citas ──────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(440, 0));

        JLabel lblTitulo = new JLabel("🩺  Atención Médica");
        lblTitulo.setFont(MainGUI.FUENTE_SUBTIT);
        lblTitulo.setForeground(MainGUI.COLOR_PRIMARIO);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));
        leftPanel.add(lblTitulo, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(tableModel);
        tabla.setFont(MainGUI.FUENTE_NORMAL);
        tabla.setRowHeight(30);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(140, 60, 100));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);

        // Renderer con colores por estado
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String estado = (String) tableModel.getValueAt(row, 5);
                    if (CitaMedica.CONFIRMADA.equals(estado)) {
                        c.setBackground(new Color(209, 236, 241));
                    } else if (CitaMedica.EN_CURSO.equals(estado)) {
                        c.setBackground(new Color(255, 220, 180));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 240, 252));
                    }
                    c.setForeground(MainGUI.COLOR_TEXTO);
                } else {
                    c.setBackground(new Color(200, 170, 220));
                    c.setForeground(MainGUI.COLOR_TEXTO);
                }
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarCitaSeleccionada();
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(MainGUI.COLOR_BORDE));
        leftPanel.add(scroll, BorderLayout.CENTER);

        JLabel lblAyuda = new JLabel("↑ Seleccione una cita Confirmada o En Curso para atender");
        lblAyuda.setFont(MainGUI.FUENTE_PEQUEÑA);
        lblAyuda.setForeground(new Color(100, 120, 150));
        lblAyuda.setBorder(new EmptyBorder(6, 0, 0, 0));
        leftPanel.add(lblAyuda, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // ─── Panel derecho: formulario de atención ────────────────────────────
        panelAtencionActiva = new JPanel(new BorderLayout(0, 10));
        panelAtencionActiva.setBackground(Color.WHITE);
        panelAtencionActiva.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(MainGUI.COLOR_BORDE),
                new EmptyBorder(18, 20, 18, 20)));

        // Placeholder inicial
        JLabel lblPlaceholder = new JLabel(
            "<html><center><br><br>🩺<br><br>" +
            "<b>Seleccione una cita de la lista</b><br><br>" +
            "<font color='#607080'>Solo se pueden atender citas con estado<br>" +
            "<b>Confirmada</b> o <b>En Curso</b></font></center></html>");
        lblPlaceholder.setFont(MainGUI.FUENTE_NORMAL);
        lblPlaceholder.setHorizontalAlignment(JLabel.CENTER);
        panelAtencionActiva.add(lblPlaceholder, BorderLayout.CENTER);

        add(panelAtencionActiva, BorderLayout.CENTER);
    }

    private void cargarCitaSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row < 0) return;
        String id = (String) tableModel.getValueAt(row, 0);
        citaActual = gestor.buscarCita(id);
        if (citaActual == null) return;

        mostrarFormularioAtencion();
    }

    private void mostrarFormularioAtencion() {
        panelAtencionActiva.removeAll();
        panelAtencionActiva.setLayout(new BorderLayout(0, 10));

        // ─── Header con info de la cita ───────────────────────────────────────
        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        headerPanel.setBackground(new Color(248, 240, 255));
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 230)),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel lblCita = new JLabel("🩺  Cita: " + citaActual.getIdCita()
                + "  —  " + citaActual.getFecha() + " a las " + citaActual.getHora());
        lblCita.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCita.setForeground(new Color(100, 50, 140));

        JLabel lblDoc = new JLabel("👨‍⚕️  Doctor: Dr. " + citaActual.getDoctor().getNombreCompleto()
                + "  (" + citaActual.getDoctor().getEspecialidad() + ")");
        lblDoc.setFont(MainGUI.FUENTE_NORMAL);

        JLabel lblPac = new JLabel("🧑  Paciente: " + citaActual.getPaciente().getNombreCompleto()
                + "  —  EPS: " + citaActual.getPaciente().getEps()
                + "  —  Sangre: " + citaActual.getPaciente().getTipoSangre());
        lblPac.setFont(MainGUI.FUENTE_NORMAL);

        JLabel lblMotivo = new JLabel("📋  Motivo: " + citaActual.getMotivo());
        lblMotivo.setFont(MainGUI.FUENTE_NORMAL);

        Color colorEstado = citaActual.getEstado().equals(CitaMedica.CONFIRMADA) ?
                new Color(0, 130, 80) : new Color(180, 80, 20);
        JLabel lblEstado = new JLabel("Estado actual:  " + citaActual.getEstado());
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstado.setForeground(colorEstado);

        headerPanel.add(lblCita);
        headerPanel.add(lblDoc);
        headerPanel.add(lblPac);
        headerPanel.add(lblMotivo);
        headerPanel.add(lblEstado);
        panelAtencionActiva.add(headerPanel, BorderLayout.NORTH);

        // ─── Historial del paciente ───────────────────────────────────────────
        JPanel centroPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        centroPanel.setOpaque(false);

        JPanel histPanel = new JPanel(new BorderLayout(0, 4));
        histPanel.setOpaque(false);
        JLabel lblH = new JLabel("📋  Historial del Paciente:");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 12));
        histPanel.add(lblH, BorderLayout.NORTH);
        txtHistorial = new JTextArea(citaActual.getPaciente().verHistorial());
        txtHistorial.setFont(new Font("Courier New", Font.PLAIN, 11));
        txtHistorial.setEditable(false);
        txtHistorial.setBackground(new Color(248, 252, 248));
        txtHistorial.setRows(4);
        JScrollPane spH = new JScrollPane(txtHistorial);
        spH.setBorder(BorderFactory.createLineBorder(MainGUI.COLOR_BORDE));
        histPanel.add(spH, BorderLayout.CENTER);
        centroPanel.add(histPanel);

        // ─── Diagnóstico ──────────────────────────────────────────────────────
        JPanel diagPanel = new JPanel(new BorderLayout(0, 4));
        diagPanel.setOpaque(false);
        JLabel lblD = new JLabel("🔬  Diagnóstico:");
        lblD.setFont(new Font("Segoe UI", Font.BOLD, 12));
        diagPanel.add(lblD, BorderLayout.NORTH);
        txtDiagnostico = new JTextArea();
        txtDiagnostico.setFont(MainGUI.FUENTE_NORMAL);
        txtDiagnostico.setRows(3);
        txtDiagnostico.setLineWrap(true);
        txtDiagnostico.setWrapStyleWord(true);
        txtDiagnostico.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Pre-llenar si ya tiene diagnóstico
        if (!citaActual.getDiagnostico().isEmpty())
            txtDiagnostico.setText(citaActual.getDiagnostico());

        JScrollPane spD = new JScrollPane(txtDiagnostico);
        spD.setBorder(BorderFactory.createLineBorder(MainGUI.COLOR_BORDE));
        diagPanel.add(spD, BorderLayout.CENTER);
        centroPanel.add(diagPanel);

        // ─── Receta ───────────────────────────────────────────────────────────
        JPanel recPanel = new JPanel(new BorderLayout(0, 4));
        recPanel.setOpaque(false);
        JLabel lblR = new JLabel("💊  Receta Médica:");
        lblR.setFont(new Font("Segoe UI", Font.BOLD, 12));
        recPanel.add(lblR, BorderLayout.NORTH);
        txtReceta = new JTextArea();
        txtReceta.setFont(MainGUI.FUENTE_NORMAL);
        txtReceta.setRows(3);
        txtReceta.setLineWrap(true);
        txtReceta.setWrapStyleWord(true);
        txtReceta.setBorder(new EmptyBorder(6, 8, 6, 8));

        if (!citaActual.getReceta().isEmpty())
            txtReceta.setText(citaActual.getReceta());

        JScrollPane spR = new JScrollPane(txtReceta);
        spR.setBorder(BorderFactory.createLineBorder(MainGUI.COLOR_BORDE));
        recPanel.add(spR, BorderLayout.CENTER);
        centroPanel.add(recPanel);

        panelAtencionActiva.add(centroPanel, BorderLayout.CENTER);

        // ─── Botones de acción ────────────────────────────────────────────────
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botonesPanel.setOpaque(false);

        boolean esConfirmada = CitaMedica.CONFIRMADA.equals(citaActual.getEstado());
        boolean esEnCurso    = CitaMedica.EN_CURSO.equals(citaActual.getEstado());
        boolean esCompletada = CitaMedica.COMPLETADA.equals(citaActual.getEstado());

        btnIniciar = crearBotonGrande("▶  Iniciar Atención", new Color(180, 100, 20));
        btnIniciar.setEnabled(esConfirmada);
        btnIniciar.addActionListener(e -> iniciarAtencion());

        btnCompletar = crearBotonGrande("✅  Completar Cita", new Color(30, 140, 80));
        btnCompletar.setEnabled(esEnCurso);
        btnCompletar.addActionListener(e -> completarCita());

        JButton btnComprobante = crearBotonGrande("🧾  Ver Comprobante", new Color(80, 100, 180));
        btnComprobante.setEnabled(esCompletada);
        btnComprobante.addActionListener(e -> mostrarComprobante());

        botonesPanel.add(btnIniciar);
        botonesPanel.add(btnCompletar);
        botonesPanel.add(btnComprobante);

        if (esCompletada) {
            JLabel lblComp = new JLabel("  ✅  Esta cita ya fue completada");
            lblComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblComp.setForeground(new Color(30, 140, 80));
            botonesPanel.add(lblComp);
        }

        panelAtencionActiva.add(botonesPanel, BorderLayout.SOUTH);

        panelAtencionActiva.revalidate();
        panelAtencionActiva.repaint();
    }

    private void iniciarAtencion() {
        if (citaActual == null) return;
        try {
            citaActual.iniciarAtencion();
            mainGUI.setStatus("🩺  Atención iniciada: " + citaActual.getIdCita());
            mostrarFormularioAtencion(); // refrescar estado
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completarCita() {
        if (citaActual == null) return;
        String diag   = txtDiagnostico.getText().trim();
        String receta = txtReceta.getText().trim();

        if (diag.isEmpty() || receta.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "❌ Debe ingresar el diagnóstico y la receta para completar la cita.",
                    "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            citaActual.completar(diag, receta);
            mainGUI.setStatus("✅  Cita completada: " + citaActual.getIdCita());
            mostrarFormularioAtencion();
            refresh();

            // Mostrar comprobante final
            mostrarComprobante();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarComprobante() {
        if (citaActual == null) return;
        JTextArea area = new JTextArea(citaActual.generarComprobante());
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(510, 320));
        JOptionPane.showMessageDialog(mainGUI, sp, "Comprobante Final de Cita ✅", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        // Mostrar citas atendibles (no canceladas ni pendientes sin confirmar optionally)
        for (CitaMedica c : gestor.getCitas()) {
            String est = c.getEstado();
            // Mostrar todas excepto canceladas en la lista de atención
            if (!CitaMedica.CANCELADA.equals(est)) {
                tableModel.addRow(new Object[]{
                    c.getIdCita(),
                    c.getFecha(),
                    c.getHora(),
                    c.getPaciente().getNombreCompleto(),
                    "Dr. " + c.getDoctor().getNombreCompleto(),
                    c.getEstado()
                });
            }
        }

        long atendibles = gestor.getCitas().stream()
            .filter(c -> CitaMedica.CONFIRMADA.equals(c.getEstado()))
            .count();
        mainGUI.setStatus("🩺  Atención Médica — " + atendibles + " cita(s) pendientes de atender");

        // Si la cita actual sigue válida, recargar su vista
        if (citaActual != null) mostrarFormularioAtencion();
    }

    private JButton crearBotonGrande(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }
}
