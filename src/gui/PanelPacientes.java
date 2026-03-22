package sistemaasignaciondecitasmedicas.gui;

import sistemaasignaciondecitasmedicas.modelos.Paciente;
import sistemaasignaciondecitasmedicas.servicios.GestorCitas;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Panel de gestión de pacientes.
 */
public class PanelPacientes extends JPanel implements Refreshable {

    private final GestorCitas gestor;
    private final MainGUI     mainGUI;

    private DefaultTableModel tableModel;
    private JTable            tabla;
    private JTextField        txtBuscar;

    private static final String[] COLUMNAS = {
        "ID / Cédula", "Nombre", "Apellido", "EPS", "Tipo Sangre", "Alergias", "Teléfono"
    };

    public PanelPacientes(GestorCitas gestor, MainGUI mainGUI) {
        this.gestor  = gestor;
        this.mainGUI = mainGUI;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setBackground(MainGUI.COLOR_FONDO);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // ─── Título y búsqueda ────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel lblTitulo = new JLabel("🧑  Gestión de Pacientes");
        lblTitulo.setFont(MainGUI.FUENTE_SUBTIT);
        lblTitulo.setForeground(MainGUI.COLOR_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(18);
        txtBuscar.setFont(MainGUI.FUENTE_NORMAL);
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });
        searchPanel.add(txtBuscar);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ─── Tabla ────────────────────────────────────────────────────────────
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
        tabla.getTableHeader().setBackground(new Color(0, 120, 80));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);

        int[] anchos = {120, 120, 120, 130, 90, 110, 110};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(238, 252, 245));
                    c.setForeground(MainGUI.COLOR_TEXTO);
                } else {
                    c.setBackground(new Color(180, 235, 210));
                    c.setForeground(MainGUI.COLOR_TEXTO);
                }
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) verInfoPaciente();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(MainGUI.COLOR_BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ─── Botones ──────────────────────────────────────────────────────────
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        botonesPanel.setOpaque(false);

        JButton btnRegistrar  = crearBoton("➕  Registrar Paciente", MainGUI.COLOR_SECUNDARIO);
        JButton btnVerInfo    = crearBoton("ℹ️  Ver Info",            MainGUI.COLOR_PRIMARIO);
        JButton btnHistorial  = crearBoton("📋  Historial Médico",   new Color(180, 100, 20));
        JButton btnVerCitas   = crearBoton("📅  Ver Citas",          new Color(80, 100, 180));
        JButton btnActualizar = crearBoton("🔄  Actualizar",         new Color(100, 120, 140));

        btnRegistrar.addActionListener(e -> dialogoRegistrarPaciente());
        btnVerInfo.addActionListener(e -> verInfoPaciente());
        btnHistorial.addActionListener(e -> verHistorial());
        btnVerCitas.addActionListener(e -> verCitasPaciente());
        btnActualizar.addActionListener(e -> refresh());

        botonesPanel.add(btnRegistrar);
        botonesPanel.add(btnVerInfo);
        botonesPanel.add(btnHistorial);
        botonesPanel.add(btnVerCitas);
        botonesPanel.add(btnActualizar);
        add(botonesPanel, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        for (Paciente p : gestor.getPacientes().values()) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getNombre(), p.getApellido(),
                p.getEps(), p.getTipoSangre(),
                p.getAlergias().isEmpty() ? "Ninguna" : p.getAlergias(),
                p.getTelefono()
            });
        }
        mainGUI.setStatus("🧑  Pacientes: " + gestor.getPacientes().size() + " registrados");
    }

    private void filtrar() {
        String texto = txtBuscar.getText().toLowerCase().trim();
        tableModel.setRowCount(0);
        for (Paciente p : gestor.getPacientes().values()) {
            if (p.getNombreCompleto().toLowerCase().contains(texto)
                    || p.getId().toLowerCase().contains(texto)
                    || p.getEps().toLowerCase().contains(texto)) {
                tableModel.addRow(new Object[]{
                    p.getId(), p.getNombre(), p.getApellido(),
                    p.getEps(), p.getTipoSangre(),
                    p.getAlergias().isEmpty() ? "Ninguna" : p.getAlergias(),
                    p.getTelefono()
                });
            }
        }
    }

    private void verInfoPaciente() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un paciente.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        Paciente p = gestor.buscarPaciente(id);
        if (p == null) return;

        JTextArea area = new JTextArea(p.mostrarInformacion());
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(440, 200));
        JOptionPane.showMessageDialog(this, sp, "Información del Paciente", JOptionPane.INFORMATION_MESSAGE);
    }

    private void verHistorial() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un paciente.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        Paciente p = gestor.buscarPaciente(id);
        if (p == null) return;

        JTextArea area = new JTextArea(p.verHistorial());
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(500, 220));
        JOptionPane.showMessageDialog(this, sp, "Historial de " + p.getNombreCompleto(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void verCitasPaciente() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un paciente.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        Paciente p = gestor.buscarPaciente(id);

        JTextArea area = new JTextArea(gestor.listarCitasPaciente(id));
        area.setFont(new Font("Courier New", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(560, 220));
        JOptionPane.showMessageDialog(this, sp, "Citas de " + p.getNombreCompleto(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Diálogo registrar paciente ───────────────────────────────────────────
    private void dialogoRegistrarPaciente() {
        JDialog dialog = new JDialog(mainGUI, "Registrar Nuevo Paciente", true);
        dialog.setSize(480, 540);
        dialog.setLocationRelativeTo(mainGUI);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(new Color(0, 120, 80));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel lbl = new JLabel("🧑  Registrar Nuevo Paciente");
        lbl.setFont(MainGUI.FUENTE_SUBTIT);
        lbl.setForeground(Color.WHITE);
        header.add(lbl);
        dialog.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField fId    = campoTexto("Cédula o ID");
        JTextField fNom   = campoTexto("Nombre");
        JTextField fApe   = campoTexto("Apellido");
        JTextField fCor   = campoTexto("correo@email.com");
        JTextField fTel   = campoTexto("Teléfono");
        JTextField fFecha = campoTexto("YYYY-MM-DD");
        JComboBox<String> fGen = new JComboBox<>(new String[]{"M", "F"});
        JTextField fSan   = campoTexto("ej: O+, A-, B+");
        JTextField fEps   = campoTexto("ej: Sanitas, Compensar");
        JTextField fAle   = campoTexto("ej: Ibuprofeno, o Ninguna");

        String[] etiquetas = {"ID / Cédula *", "Nombre *", "Apellido", "Correo",
            "Teléfono", "Fecha Nac.", "Género", "Tipo Sangre", "EPS", "Alergias"};
        JComponent[] inputs = {fId, fNom, fApe, fCor, fTel, fFecha, fGen, fSan, fEps, fAle};

        for (int i = 0; i < inputs.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            JLabel etiq = new JLabel(etiquetas[i] + ":");
            etiq.setFont(MainGUI.FUENTE_NORMAL);
            etiq.setPreferredSize(new Dimension(155, 28));
            form.add(etiq, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(inputs[i], gbc);
        }

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        botones.setBackground(MainGUI.COLOR_FONDO);
        botones.setBorder(new MatteBorder(1, 0, 0, 0, MainGUI.COLOR_BORDE));

        JButton btnCancelar = crearBoton("Cancelar", new Color(150, 160, 175));
        JButton btnGuardar  = crearBoton("✅  Guardar Paciente", MainGUI.COLOR_SECUNDARIO);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                String id  = fId.getText().trim();
                String nom = fNom.getText().trim();
                if (id.isEmpty() || nom.isEmpty())
                    throw new IllegalArgumentException("ID y Nombre son obligatorios.");

                gestor.registrarPaciente(new Paciente(
                    id, nom, fApe.getText().trim(), fCor.getText().trim(),
                    fTel.getText().trim(), fFecha.getText().trim(),
                    (String) fGen.getSelectedItem(),
                    fSan.getText().trim().isEmpty() ? "N/A" : fSan.getText().trim(),
                    fEps.getText().trim().isEmpty() ? "N/A" : fEps.getText().trim(),
                    fAle.getText().trim().isEmpty() ? "Ninguna" : fAle.getText().trim()
                ));
                refresh();
                mainGUI.setStatus("✅  Paciente registrado: " + nom);
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "❌ " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        botones.add(btnCancelar);
        botones.add(btnGuardar);
        dialog.add(botones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JTextField campoTexto(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(MainGUI.FUENTE_NORMAL);
        tf.setToolTipText(placeholder);
        tf.setPreferredSize(new Dimension(220, 30));
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
