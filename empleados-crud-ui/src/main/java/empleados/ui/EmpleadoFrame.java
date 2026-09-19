package empleados.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import empleados.dao.EmpleadoDAO;
import empleados.modelo.Empleado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EmpleadoFrame extends JFrame {

    private final EmpleadoDAO dao = new EmpleadoDAO();
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtId, txtNombre, txtTelefono, txtSalario, txtBuscar;
    private JComboBox<String> cbDepartamento;
    private JFormattedTextField txtFecha;
    private JCheckBox chkActivo;
    private JTable table;
    private JButton btnGuardar, btnActualizar, btnEliminar, btnLimpiar, btnExportar, btnImportar;
    private JToggleButton btnTema;
    private JLabel lblStatus, lblTotal;

    public EmpleadoFrame() {
        initUI();
        configurarAtajosTeclado();
        cargarDatos();
        limpiarFormulario();
    }

    private void initUI() {
        setTitle("Sistema de Gestión de Empleados - Variante B");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 12));
        mainPanel.setBorder(new EmptyBorder(15, 15, 12, 15));
        setContentPane(mainPanel);

        // 1. Cabecera
        JPanel headerTop = new JPanel(new BorderLayout(10, 10));
        JLabel lblTitulo = new JLabel("Panel Principal de Empleados");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        btnTema = new JToggleButton("🌙 Modo Oscuro");
        btnTema.setSelected(true);
        btnTema.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnTema.addActionListener(e -> alternarTema());

        txtBuscar = new JTextField();
        txtBuscar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍 Buscar en tiempo real...");
        txtBuscar.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        txtBuscar.setPreferredSize(new Dimension(270, 35));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.add(btnTema);
        rightHeader.add(txtBuscar);

        headerTop.add(lblTitulo, BorderLayout.WEST);
        headerTop.add(rightHeader, BorderLayout.EAST);

        JPanel northContainer = new JPanel(new BorderLayout(0, 10));
        northContainer.add(headerTop, BorderLayout.NORTH);
        northContainer.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);
        mainPanel.add(northContainer, BorderLayout.NORTH);

        // 2. Formulario Lateral
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(" Datos del Registro "),
            new EmptyBorder(10, 10, 10, 10)
        ));
        formPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField();
        txtId.setEditable(false);
        txtNombre = createStyledField("Ej: Carlos Mendoza");
        txtTelefono = createStyledField("Ej: 5555-1234"); // MEJORA #1

        String[] departamentos = {"Informática", "Recursos Humanos", "Ventas", "Contabilidad", "Administración", "Operaciones"};
        cbDepartamento = new JComboBox<>(departamentos);
        cbDepartamento.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        txtSalario = createStyledField("Ej: 5500.00");
        txtSalario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) {
                    evt.consume();
                }
                if (c == '.' && txtSalario.getText().contains(".")) {
                    evt.consume();
                }
            }
        });

        try {
            MaskFormatter mascaraFecha = new MaskFormatter("####-##-##");
            mascaraFecha.setPlaceholderCharacter('_');
            txtFecha = new JFormattedTextField(mascaraFecha);
            txtFecha.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
            txtFecha.setColumns(15);
        } catch (ParseException e) {
            txtFecha = new JFormattedTextField();
        }

        chkActivo = new JCheckBox("Empleado Activo");
        chkActivo.setSelected(true);

        addFormField(formPanel, gbc, 0, "ID:", txtId);
        addFormField(formPanel, gbc, 1, "Nombre Completo:", txtNombre);
        addFormField(formPanel, gbc, 2, "Teléfono:", txtTelefono); // MEJORA #1
        addFormField(formPanel, gbc, 3, "Departamento:", cbDepartamento);
        addFormField(formPanel, gbc, 4, "Salario (Q):", txtSalario);
        addFormField(formPanel, gbc, 5, "Fecha Ingreso:", txtFecha);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(chkActivo, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 12, 0);
        formPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);

        // Botones
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGuardar = new JButton("➕ Guardar");
        btnActualizar = new JButton("✏️ Actualizar");
        btnEliminar = new JButton("🗑️ Eliminar");
        btnLimpiar = new JButton("🧹 Limpiar");

        btnGuardar.putClientProperty(FlatClientProperties.STYLE, "background: #2b78e4; foreground: #ffffff; bold: true; arc: 10");
        btnActualizar.putClientProperty(FlatClientProperties.STYLE, "background: #2e7d32; foreground: #ffffff; bold: true; arc: 10");
        btnEliminar.putClientProperty(FlatClientProperties.STYLE, "background: #c62828; foreground: #ffffff; bold: true; arc: 10");
        btnLimpiar.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnPanel.add(btnGuardar);
        btnPanel.add(btnActualizar);
        btnPanel.add(btnEliminar);
        btnPanel.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // 3. Tabla Principal (Incluye 'Teléfono' - MEJORA #1 y 'Antigüedad' - MEJORA #6)
        String[] columnas = {"ID", "Nombre", "Teléfono", "Departamento", "Salario", "Fecha", "Antigüedad", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(34);
        table.getTableHeader().setReorderingAllowed(false);

        table.putClientProperty(FlatClientProperties.STYLE, ""
            + "showHorizontalLines: true;"
            + "showVerticalLines: true;"
            + "alternateRowColor: $Table.alternateRowColor;"
            + "arc: 10");

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    seleccionarFila();
                }
            }
        });

        // Renderizadores
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Renderer para la columna Estado (índice 7)
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(130); // MEJORA #6
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 4. Barra de Estado
        JPanel statusContent = new JPanel(new BorderLayout(10, 0));
        statusContent.setBorder(new EmptyBorder(6, 5, 2, 5));
        
        lblStatus = new JLabel(" Sistema listo.");
        lblTotal = new JLabel("Cargando métricas...");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnImportar = new JButton("📥 Importar CSV");
        btnImportar.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnImportar.addActionListener(e -> importarCSV());

        btnExportar = new JButton("📊 Exportar CSV");
        btnExportar.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnExportar.addActionListener(e -> exportarCSV());

        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightStatusPanel.add(lblTotal);
        rightStatusPanel.add(btnImportar);
        rightStatusPanel.add(btnExportar);

        statusContent.add(lblStatus, BorderLayout.WEST);
        statusContent.add(rightStatusPanel, BorderLayout.EAST);

        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);
        southContainer.add(statusContent, BorderLayout.SOUTH);
        
        mainPanel.add(southContainer, BorderLayout.SOUTH);

        // Eventos
        btnGuardar.addActionListener(e -> guardarEmpleado());
        btnActualizar.addActionListener(e -> actualizarEmpleado());
        btnEliminar.addActionListener(e -> eliminarEmpleado());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                seleccionarFila();
            }
        });
    }

    private void configurarAtajosTeclado() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "guardar");
        am.put("guardar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnActualizar.isEnabled()) actualizarEmpleado();
                else guardarEmpleado();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "limpiar");
        am.put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { limpiarFormulario(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        am.put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() != -1) eliminarEmpleado();
            }
        });
    }

    private void alternarTema() {
        if (btnTema.isSelected()) {
            FlatDarkLaf.setup();
            btnTema.setText("🌙 Modo Oscuro");
        } else {
            FlatLightLaf.setup();
            btnTema.setText("☀️ Modo Claro");
        }
        FlatLaf.updateUI();
    }

    private JTextField createStyledField(String placeholder) {
        JTextField tf = new JTextField(15);
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        return tf;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        panel.add(field, gbc);
    }

    private void setModoEdicion(boolean enEdicion) {
        btnGuardar.setEnabled(!enEdicion);
        btnActualizar.setEnabled(enEdicion);
        btnEliminar.setEnabled(enEdicion);
    }

    private void cargarDatos() {
        tableModel.setRowCount(0);
        try {
            List<Empleado> lista = dao.listarTodos();
            for (Empleado e : lista) {
                tableModel.addRow(new Object[]{
                    e.getId(),
                    e.getNombre(),
                    e.getTelefono(),            // MEJORA #1
                    e.getDepartamento(),
                    String.format("Q %,.2f", e.getSalario()),
                    e.getFechaContratacion(),
                    e.getAntiguedadCalculada(), // MEJORA #6
                    e.isActivo() ? "Activo" : "Inactivo"
                });
            }
            actualizarContadorYNomina();
            setStatus("Datos cargados correctamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error SQL: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            setStatus("Error al cargar datos.");
        }
    }

    private void guardarEmpleado() {
        try {
            Empleado e = obtenerEmpleadoDesdeFormulario(false);
            dao.crear(e);
            cargarDatos();
            limpiarFormulario();
            setStatus("Empleado registrado con éxito.");
            JOptionPane.showMessageDialog(this, "Empleado registrado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarEmpleado() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila primero.");
            return;
        }
        try {
            Empleado e = obtenerEmpleadoDesdeFormulario(true);
            dao.actualizar(e);
            cargarDatos();
            limpiarFormulario();
            setStatus("Empleado actualizado con éxito.");
            JOptionPane.showMessageDialog(this, "Registro actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarEmpleado() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila primero.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea eliminar a '" + txtNombre.getText() + "'?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.eliminar(Integer.parseInt(txtId.getText()));
                cargarDatos();
                limpiarFormulario();
                setStatus("Empleado eliminado con éxito.");
                JOptionPane.showMessageDialog(this, "Empleado eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage());
                setStatus("Error al eliminar registro.");
            }
        }
    }

    private void importarCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo CSV de empleados");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine(); // Saltar cabecera
                int creados = 0;
                while ((line = reader.readLine()) != null) {
                    String[] datos = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (datos.length >= 7) {
                        String nombre = datos[1].replace("\"", "").trim();
                        String telefono = datos[2].replace("\"", "").trim();
                        String depto = datos[3].replace("\"", "").trim();
                        double salario = Double.parseDouble(datos[4].trim());
                        LocalDate fecha = LocalDate.parse(datos[5].trim());
                        boolean activo = datos[datos.length - 1].trim().equalsIgnoreCase("Activo");

                        Empleado e = new Empleado(nombre, telefono, depto, salario, fecha, activo);
                        dao.crear(e);
                        creados++;
                    }
                }
                cargarDatos();
                setStatus("Importación masiva completada.");
                JOptionPane.showMessageDialog(this, "Se importaron " + creados + " registros correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al importar archivo CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportarCSV() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte de empleados (CSV)");
        fileChooser.setSelectedFile(new File("reporte_empleados.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("ID,Nombre,Telefono,Departamento,Salario,Fecha,Antiguedad,Estado");
                for (int i = 0; i < table.getRowCount(); i++) {
                    writer.println(String.format("%s,\"%s\",\"%s\",\"%s\",%s,%s,\"%s\",%s",
                        table.getValueAt(i, 0),
                        table.getValueAt(i, 1),
                        table.getValueAt(i, 2),
                        table.getValueAt(i, 3),
                        table.getValueAt(i, 4).toString().replace("Q", "").replace(",", "").trim(),
                        table.getValueAt(i, 5),
                        table.getValueAt(i, 6),
                        table.getValueAt(i, 7)
                    ));
                }
                setStatus("Archivo CSV exportado exitosamente.");
                JOptionPane.showMessageDialog(this, "Reporte CSV exportado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Empleado obtenerEmpleadoDesdeFormulario(boolean incluirId) {
        limpiarErroresVisuales();
        boolean valido = true;

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            txtNombre.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            valido = false;
        }

        String telefono = txtTelefono.getText().trim();
        String depto = (String) cbDepartamento.getSelectedItem();

        double salario = 0;
        try {
            salario = Double.parseDouble(txtSalario.getText().trim().replace(",", "."));
            if (salario < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            txtSalario.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            valido = false;
        }

        LocalDate fecha = null;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim());
        } catch (DateTimeParseException e) {
            txtFecha.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            valido = false;
        }

        if (!valido) {
            throw new IllegalArgumentException("Por favor, completa los campos resaltados en rojo correctamente.");
        }

        Empleado e = new Empleado(nombre, telefono, depto, salario, fecha, chkActivo.isSelected());
        if (incluirId) {
            e.setId(Integer.parseInt(txtId.getText()));
        }
        return e;
    }

    private void limpiarErroresVisuales() {
        txtNombre.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtTelefono.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtSalario.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtFecha.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    private void seleccionarFila() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtNombre.setText(tableModel.getValueAt(modelRow, 1).toString());
        
        Object telObj = tableModel.getValueAt(modelRow, 2);
        txtTelefono.setText(telObj != null ? telObj.toString() : "");
        
        cbDepartamento.setSelectedItem(tableModel.getValueAt(modelRow, 3).toString());

        String salarioStr = tableModel.getValueAt(modelRow, 4).toString()
                                .replace("Q", "").replace(",", "").trim();
        txtSalario.setText(salarioStr);

        txtFecha.setText(tableModel.getValueAt(modelRow, 5).toString());
        chkActivo.setSelected(tableModel.getValueAt(modelRow, 7).toString().equals("Activo"));

        limpiarErroresVisuales();
        setModoEdicion(true);
        setStatus("Empleado seleccionado: ID " + txtId.getText());
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        cbDepartamento.setSelectedIndex(0);
        txtSalario.setText("");
        txtFecha.setValue(null);
        chkActivo.setSelected(true);
        limpiarErroresVisuales();
        table.clearSelection();
        setModoEdicion(false);
        setStatus("Formulario listo para nuevo registro.");
    }

    private void filtrar() {
        String query = txtBuscar.getText().trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
        actualizarContadorYNomina();
    }

    private void actualizarContadorYNomina() {
        int total = table.getRowCount();
        int activos = 0;
        double totalNomina = 0.0;

        for (int i = 0; i < total; i++) {
            String salarioStr = table.getValueAt(i, 4).toString().replace("Q", "").replace(",", "").trim();
            String estadoStr = table.getValueAt(i, 7).toString();

            try {
                totalNomina += Double.parseDouble(salarioStr);
            } catch (NumberFormatException ignored) {}

            if ("Activo".equals(estadoStr)) {
                activos++;
            }
        }

        lblTotal.setText(String.format("Empleados: %d (%d Activos)  |  Nómina Total: Q %,.2f  ", total, activos, totalNomina));
    }

    private void setStatus(String mensaje) {
        lblStatus.setText(" " + mensaje);
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            String estado = String.valueOf(value);

            if ("Activo".equals(estado)) {
                label.setText("● Activo");
                label.setForeground(new Color(46, 125, 50));
            } else {
                label.setText("● Inactivo");
                label.setForeground(new Color(198, 40, 40));
            }

            if (isSelected) {
                label.setForeground(table.getSelectionForeground());
            }
            return label;
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new EmpleadoFrame().setVisible(true));
    }
}