package sistemaasignaciondecitasmedicas.gui;

import sistemaasignaciondecitasmedicas.modelos.*;
import sistemaasignaciondecitasmedicas.servicios.GestorCitas;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Panel de gestión de citas médicas.
 * Permite agendar, confirmar, cancelar y ver comprobantes.
 */
public class PanelCitas extends JPanel implements Refreshable {

    private final GestorCitas gestor;
    private final MainGUI     mainGUI;

    private DefaultTableModel tableModel;
    private JTable            tabla;

    private static final String[] COLUMNAS = {
        "ID Cita", "Fecha", "Hora", "Paciente", "Doctor", "Especialidad", "Motivo", "Estado"
    };

    // Colores por estado
    private static final Color COL_PENDIENTE  = new Color(255, 243, 205);
    private static final Color COL_CONFIRMADA = new Color(209, 236, 241);
    private static final Color COL_EN_CURSO   = new Color(255, 220, 180);
    private static final Color COL_COMPLETADA = new Color(212, 237, 218);
    private static final Color COL_CANCELADA  = new Color(248, 215, 218);

    public PanelCitas(GestorCitas gestor, MainGUI mainGUI) {
        this.gestor  = gestor;
        this.mainGUI = mainGUI;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setBackground(MainGUI.COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // ─── Título ───────────────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel lblTitulo = new JLabel("📅  Gestión de Citas Médicas");
        lblTitulo.setFont(MainGUI.FUENTE_SUBTIT);
        lblTitulo.setForeground(MainGUI.COLOR_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.WEST);

        // Leyenda de estados
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        leyenda.setOpaque(false);
        leyenda.add(estadoChip("Pendiente",  COL_PENDIENTE));
        leyenda.add(estadoChip("Confirmada", COL_CONFIRMADA));
        leyenda.add(estadoChip("En curso",   COL_EN_CURSO));
        leyenda.add(estadoChip("Completada", COL_COMPLETADA));
        leyenda.add(estadoChip("Cancelada",  COL_CANCELADA));
        topPanel.add(leyenda, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ─── Tabla ────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(tableModel);
        tabla.setFont(MainGUI.FUENTE_NORMAL);
        tabla.setRowHeight(32);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(70, 60, 160));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);

        int[] anchos = {90, 90, 65, 130, 130, 120, 160, 90};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Renderer con colores por estado
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String estado = (String) tableModel.getValueAt(row, 7);
                    Color bg = colorPorEstado(estado);
                    c.setBackground(bg);
                    c.setForeground(MainGUI.COLOR_TEXTO);
                } else {
                    c.setBackground(new Color(180, 200, 240));
                    c.setForeground(MainGUI.COLOR_TEXTO);
                }
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) verComprobante();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(MainGUI.COLOR_BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ─── Botones ──────────────────────────────────────────────────────────
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        botonesPanel.setOpaque(false);

        JButton btnAgendar   = crearBoton("📅  Agendar Cita",    MainGUI.COLOR_SECUNDARIO);
        JButton btnComprobante = crearBoton("🧾  Comprobante",   new Color(80, 100, 180));
        JButton btnConfirmar = crearBoton("✅  Confirmar",        new Color(30, 140, 80));
        JButton btnCancelar  = crearBoton("❌  Cancelar Cita",   MainGUI.COLOR_ACENTO);
        JButton btnActualizar= crearBoton("🔄  Actualizar",       new Color(100, 120, 140));

        btnAgendar.addActionListener(e -> dialogoAgendarCita());
        btnComprobante.addActionListener(e -> verComprobante());
        btnConfirmar.addActionListener(e -> confirmarCita());
        btnCancelar.addActionListener(e -> cancelarCita());
        btnActualizar.addActionListener(e -> refresh());

        botonesPanel.add(btnAgendar);
        botonesPanel.add(btnComprobante);
        botonesPanel.add(btnConfirmar);
        botonesPanel.add(btnCancelar);
        botonesPanel.add(btnActualizar);
        add(botonesPanel, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        for (CitaMedica c : gestor.getCitas()) {
            tableModel.addRow(new Object[]{
                c.getIdCita(),
                c.getFecha(),
                c.getHora(),
                c.getPaciente().getNombreCompleto(),
                "Dr. " + c.getDoctor().getNombreCompleto(),
                c.getDoctor().getEspecialidad(),
                c.getMotivo(),
                c.getEstado()
            });
        }
        mainGUI.setStatus("📅  Citas: " + gestor.getCitas().size() + " registradas");
    }

    private void verComprobante() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una cita.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        CitaMedica c = gestor.buscarCita(id);
        if (c == null) return;

        JTextArea area = new JTextArea(c.generarComprobante());
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(500, 310));
        JOptionPane.showMessageDialog(this, sp, "Comprobante de Cita", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmarCita() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una cita.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        try {
            CitaMedica c = gestor.buscarCita(id);
            c.confirmar();
            refresh();
            mainGUI.setStatus("✅  Cita " + id + " confirmada");
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelarCita() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una cita.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cancelar la cita " + id + "?",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            gestor.cancelarCita(id);
            refresh();
            mainGUI.setStatus("🚫  Cita " + id + " cancelada");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Diálogo agendar cita ─────────────────────────────────────────────────
    private void dialogoAgendarCita() {
        JDialog dialog = new JDialog(mainGUI, "Agendar Nueva Cita", true);
        dialog.setSize(520, 440);
        dialog.setLocationRelativeTo(mainGUI);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(new Color(70, 60, 160));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel lbl = new JLabel("📅  Agendar Nueva Cita Médica");
        lbl.setFont(MainGUI.FUENTE_SUBTIT);
        lbl.setForeground(Color.WHITE);
        header.add(lbl);
        dialog.add(header, BorderLayout.NORTH);

        // Construir combos con datos reales
        DefaultComboBoxModel<String> modelDoc = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<String> modelPac = new DefaultComboBoxModel<>();

        gestor.getDoctores().values().forEach(d ->
            modelDoc.addElement(d.getId() + " - Dr. " + d.getNombreCompleto() + " (" + d.getEspecialidad() + ")")
        );
        gestor.getPacientes().values().forEach(p ->
            modelPac.addElement(p.getId() + " - " + p.getNombreCompleto())
        );

        JComboBox<String> cbDoctor   = new JComboBox<>(modelDoc);
        JComboBox<String> cbPaciente = new JComboBox<>(modelPac);
        cbDoctor.setFont(MainGUI.FUENTE_NORMAL);
        cbPaciente.setFont(MainGUI.FUENTE_NORMAL);

        JTextField fFecha  = campoTexto("YYYY-MM-DD  ej: 2026-04-15");
        JTextField fHora   = campoTexto("HH:MM  ej: 09:00");
        JTextField fMotivo = campoTexto("Motivo de consulta");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.anchor = GridBagConstraints.WEST;

        String[] etiquetas = {"Doctor *", "Paciente *", "Fecha *", "Hora *", "Motivo *"};
        JComponent[] inputs = {cbDoctor, cbPaciente, fFecha, fHora, fMotivo};

        for (int i = 0; i < inputs.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            JLabel etiq = new JLabel(etiquetas[i] + ":");
            etiq.setFont(MainGUI.FUENTE_NORMAL);
            etiq.setPreferredSize(new Dimension(100, 28));
            form.add(etiq, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(inputs[i], gbc);
        }

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        botones.setBackground(MainGUI.COLOR_FONDO);
        botones.setBorder(new MatteBorder(1, 0, 0, 0, MainGUI.COLOR_BORDE));

        JButton btnCancelar = crearBoton("Cancelar", new Color(150, 160, 175));
        JButton btnAgendar  = crearBoton("📅  Agendar Cita", new Color(70, 60, 160));

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnAgendar.addActionListener(e -> {
            try {
                if (modelDoc.getSize() == 0 || modelPac.getSize() == 0)
                    throw new IllegalArgumentException("No hay doctores o pacientes registrados.");

                String docSelec = (String) cbDoctor.getSelectedItem();
                String pacSelec = (String) cbPaciente.getSelectedItem();
                String idDoc = docSelec.split(" - ")[0].trim();
                String idPac = pacSelec.split(" - ")[0].trim();
                String fecha = fFecha.getText().trim();
                String hora  = fHora.getText().trim();
                String motivo= fMotivo.getText().trim();

                if (fecha.isEmpty() || hora.isEmpty() || motivo.isEmpty())
                    throw new IllegalArgumentException("Fecha, hora y motivo son obligatorios.");

                CitaMedica cita = gestor.agendarCita(idPac, idDoc, fecha, hora, motivo);
                refresh();
                mainGUI.setStatus("📅  Cita agendada: " + cita.getIdCita());
                dialog.dispose();

                // Mostrar comprobante inmediatamente
                JTextArea area = new JTextArea(cita.generarComprobante());
                area.setFont(new Font("Courier New", Font.PLAIN, 13));
                area.setEditable(false);
                JScrollPane sp = new JScrollPane(area);
                sp.setPreferredSize(new Dimension(480, 280));
                JOptionPane.showMessageDialog(mainGUI, sp, "Cita Agendada Exitosamente ✅", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        botones.add(btnCancelar);
        botones.add(btnAgendar);
        dialog.add(botones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────
    private Color colorPorEstado(String estado) {
        if (estado == null) return Color.WHITE;
        switch (estado) {
            case CitaMedica.PENDIENTE:  return COL_PENDIENTE;
            case CitaMedica.CONFIRMADA: return COL_CONFIRMADA;
            case CitaMedica.EN_CURSO:   return COL_EN_CURSO;
            case CitaMedica.COMPLETADA: return COL_COMPLETADA;
            case CitaMedica.CANCELADA:  return COL_CANCELADA;
            default: return Color.WHITE;
        }
    }

    private JLabel estadoChip(String texto, Color color) {
        JLabel chip = new JLabel("  " + texto + "  ");
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        chip.setBackground(color);
        chip.setForeground(MainGUI.COLOR_TEXTO);
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,30)));
        return chip;
    }

    private JTextField campoTexto(String tooltip) {
        JTextField tf = new JTextField();
        tf.setFont(MainGUI.FUENTE_NORMAL);
        tf.setToolTipText(tooltip);
        tf.setPreferredSize(new Dimension(300, 30));
        return tf;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }
}
