package sistemaasignaciondecitasmedicas.gui;

import sistemaasignaciondecitasmedicas.modelos.Doctor;
import sistemaasignaciondecitasmedicas.servicios.GestorCitas;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Panel de gestión de doctores.
 * Permite listar, registrar, ver info y buscar por especialidad.
 */
public class PanelDoctores extends JPanel implements Refreshable {

    private final GestorCitas gestor;
    private final MainGUI     mainGUI;

    private DefaultTableModel tableModel;
    private JTable            tabla;
    private JTextField        txtBuscar;

    private static final String[] COLUMNAS = {
        "ID", "Nombre", "Apellido", "Especialidad", "Licencia", "Valor Consulta", "Teléfono"
    };

    public PanelDoctores(GestorCitas gestor, MainGUI mainGUI) {
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

        JLabel lblTitulo = new JLabel("👨‍⚕️  Gestión de Doctores");
        lblTitulo.setFont(MainGUI.FUENTE_SUBTIT);
        lblTitulo.setForeground(MainGUI.COLOR_PRIMARIO);
        topPanel.add(lblTitulo, BorderLayout.WEST);

        // Búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(18);
        txtBuscar.setFont(MainGUI.FUENTE_NORMAL);
        txtBuscar.setToolTipText("Filtrar por nombre o especialidad");
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
        tabla.getTableHeader().setBackground(MainGUI.COLOR_PRIMARIO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);

        // Anchos de columnas
        int[] anchos = {80, 120, 120, 150, 100, 130, 110};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Render de colores alternos
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(238, 246, 255));
                    c.setForeground(MainGUI.COLOR_TEXTO);
                } else {
                    c.setBackground(new Color(180, 210, 240));
                    c.setForeground(MainGUI.COLOR_TEXTO);
                }
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        // Doble click para ver info
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) verInfoDoctor();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(MainGUI.COLOR_BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ─── Botones ──────────────────────────────────────────────────────────
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        botonesPanel.setOpaque(false);

        JButton btnRegistrar   = crearBoton("➕  Registrar Doctor", MainGUI.COLOR_SECUNDARIO);
        JButton btnVerInfo     = crearBoton("ℹ️  Ver Info",          MainGUI.COLOR_PRIMARIO);
        JButton btnVerCitas    = crearBoton("📅  Ver Citas",         new Color(80, 100, 180));
        JButton btnEspecialidad= crearBoton("🔍  Por Especialidad",  new Color(150, 90, 180));
        JButton btnActualizar  = crearBoton("🔄  Actualizar",        new Color(100, 120, 140));

        btnRegistrar.addActionListener(e -> dialogoRegistrarDoctor());
        btnVerInfo.addActionListener(e -> verInfoDoctor());
        btnVerCitas.addActionListener(e -> verCitasDoctor());
        btnEspecialidad.addActionListener(e -> buscarPorEspecialidad());
        btnActualizar.addActionListener(e -> refresh());

        botonesPanel.add(btnRegistrar);
        botonesPanel.add(btnVerInfo);
        botonesPanel.add(btnVerCitas);
        botonesPanel.add(btnEspecialidad);
        botonesPanel.add(btnActualizar);
        add(botonesPanel, BorderLayout.SOUTH);
    }

    // ─── Refrescar tabla ──────────────────────────────────────────────────────
    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        for (Doctor d : gestor.getDoctores().values()) {
            tableModel.addRow(new Object[]{
                d.getId(),
                d.getNombre(),
                d.getApellido(),
                d.getEspecialidad(),
                d.getNumeroLicencia(),
                fmt.format(d.getValorConsulta()),
                d.getTelefono()
            });
        }
        mainGUI.setStatus("👨‍⚕️  Doctores: " + gestor.getDoctores().size() + " registrados");
    }

    // ─── Filtro en tiempo real ────────────────────────────────────────────────
    private void filtrar() {
        String texto = txtBuscar.getText().toLowerCase().trim();
        tableModel.setRowCount(0);
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        for (Doctor d : gestor.getDoctores().values()) {
            if (d.getNombreCompleto().toLowerCase().contains(texto)
                    || d.getEspecialidad().toLowerCase().contains(texto)
                    || d.getId().toLowerCase().contains(texto)) {
                tableModel.addRow(new Object[]{
                    d.getId(), d.getNombre(), d.getApellido(),
                    d.getEspecialidad(), d.getNumeroLicencia(),
                    fmt.format(d.getValorConsulta()), d.getTelefono()
                });
            }
        }
    }

    // ─── Ver info del doctor seleccionado ────────────────────────────────────
    private void verInfoDoctor() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un doctor de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        Doctor d = gestor.buscarDoctor(id);
        if (d == null) return;

        JTextArea area = new JTextArea(d.mostrarInformacion());
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(420, 180));
        JOptionPane.showMessageDialog(this, sp, "Información del Doctor", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Ver citas del doctor seleccionado ───────────────────────────────────
    private void verCitasDoctor() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un doctor de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        Doctor d = gestor.buscarDoctor(id);

        JTextArea area = new JTextArea(gestor.listarCitasDoctor(id));
        area.setFont(new Font("Courier New", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(560, 220));
        JOptionPane.showMessageDialog(this, sp, "Citas de Dr. " + d.getNombreCompleto(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Buscar por especialidad ──────────────────────────────────────────────
    private void buscarPorEspecialidad() {
        String esp = JOptionPane.showInputDialog(this, "Ingrese la especialidad a buscar:", "Buscar Especialidad", JOptionPane.QUESTION_MESSAGE);
        if (esp == null || esp.trim().isEmpty()) return;

        JTextArea area = new JTextArea(gestor.listarPorEspecialidad(esp.trim()));
        area.setFont(new Font("Courier New", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(MainGUI.COLOR_FONDO);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(520, 200));
        JOptionPane.showMessageDialog(this, sp, "Especialidad: " + esp, JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Diálogo registrar doctor ────────────────────────────────────────────
    private void dialogoRegistrarDoctor() {
        JDialog dialog = new JDialog(mainGUI, "Registrar Nuevo Doctor", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(mainGUI);
        dialog.setLayout(new BorderLayout());

        // Header del diálogo
        JPanel header = new JPanel();
        header.setBackground(MainGUI.COLOR_PRIMARIO);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel lbl = new JLabel("👨‍⚕️  Registrar Nuevo Doctor");
        lbl.setFont(MainGUI.FUENTE_SUBTIT);
        lbl.setForeground(Color.WHITE);
        header.add(lbl);
        dialog.add(header, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField fId    = campoTexto("ej: D005");
        JTextField fNom   = campoTexto("Nombre");
        JTextField fApe   = campoTexto("Apellido");
        JTextField fCor   = campoTexto("correo@clinica.com");
        JTextField fTel   = campoTexto("Teléfono");
        JTextField fFecha = campoTexto("YYYY-MM-DD");
        JComboBox<String> fGen = new JComboBox<>(new String[]{"M", "F"});
        JTextField fEsp   = campoTexto("ej: Cardiología");
        JTextField fLic   = campoTexto("ej: LIC-0001");
        JTextField fVal   = campoTexto("ej: 120000");

        String[][] campos = {
            {"ID *", null}, {"Nombre *", null}, {"Apellido", null},
            {"Correo", null}, {"Teléfono", null}, {"Fecha Nac.", null},
            {"Género", null}, {"Especialidad *", null}, {"Licencia", null},
            {"Valor Consulta (COP)", null}
        };
        JComponent[] inputs = {fId, fNom, fApe, fCor, fTel, fFecha, fGen, fEsp, fLic, fVal};

        for (int i = 0; i < inputs.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel etiq = new JLabel(campos[i][0] + ":");
            etiq.setFont(MainGUI.FUENTE_NORMAL);
            etiq.setForeground(MainGUI.COLOR_TEXTO);
            etiq.setPreferredSize(new Dimension(160, 28));
            form.add(etiq, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(inputs[i], gbc);
        }

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(null);
        dialog.add(scrollForm, BorderLayout.CENTER);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        botones.setBackground(MainGUI.COLOR_FONDO);
        botones.setBorder(new MatteBorder(1, 0, 0, 0, MainGUI.COLOR_BORDE));

        JButton btnCancelar = crearBoton("Cancelar", new Color(150, 160, 175));
        JButton btnGuardar  = crearBoton("✅  Guardar Doctor", MainGUI.COLOR_SECUNDARIO);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                String id  = fId.getText().trim();
                String nom = fNom.getText().trim();
                String ape = fApe.getText().trim();
                String cor = fCor.getText().trim();
                String tel = fTel.getText().trim();
                String fn  = fFecha.getText().trim();
                String gen = (String) fGen.getSelectedItem();
                String esp = fEsp.getText().trim();
                String lic = fLic.getText().trim();
                String valStr = fVal.getText().trim().replace(",", ".");

                if (id.isEmpty() || nom.isEmpty() || esp.isEmpty())
                    throw new IllegalArgumentException("ID, Nombre y Especialidad son obligatorios.");

                double val = valStr.isEmpty() ? 0 : Double.parseDouble(valStr);
                gestor.registrarDoctor(new Doctor(id, nom, ape, cor, tel, fn, gen, esp, lic, val));
                refresh();
                mainGUI.setStatus("✅  Doctor registrado: Dr. " + nom + " " + ape);
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

    // ─── Utilidades UI ────────────────────────────────────────────────────────
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
