package empleados.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import empleados.dao.EmpleadoDAO;
import empleados.modelo.Empleado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EmpleadoFrame extends JFrame {

    private final EmpleadoDAO dao = new EmpleadoDAO();
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtId, txtNombre, txtDepartamento, txtSalario, txtFecha, txtBuscar;
    private JCheckBox chkActivo;
    private JTable table;
    private JButton btnGuardar, btnActualizar, btnEliminar, btnLimpiar, btnExportar;
    private JLabel lblStatus, lblTotal;

    public EmpleadoFrame() {
        initUI();
        cargarDatos();
        limpiarFormulario();
    }

    private void initUI() {
        setTitle("Sistema de Gestión de Empleados");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 10, 15));
        setContentPane(mainPanel);

        // 1. Header Superior con Buscador Integrado
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        JLabel lblTitulo = new JLabel("Panel Principal de Empleados");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerPanel.add(lblTitulo, BorderLayout.WEST);

        txtBuscar = new JTextField();
        txtBuscar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍 Buscar en tiempo real...");
        txtBuscar.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        txtBuscar.setPreferredSize(new Dimension(280, 35));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        headerPanel.add(txtBuscar, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Panel de Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(" Información del Empleado "));
        formPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField();
        txtId.setEditable(false);
        txtNombre = createStyledField("Ej: Carlos Mendoza");
        txtDepartamento = createStyledField("Ej: Informática");
        txtSalario = createStyledField("Ej: 5500.00");

        // [MEJORA 1] Restricción de entrada de teclado en el Salario
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

        txtFecha = createStyledField("YYYY-MM-DD");
        chkActivo = new JCheckBox("Estado Activo");
        chkActivo.setSelected(true);

        addFormField(formPanel, gbc, 0, "ID:", txtId);
        addFormField(formPanel, gbc, 1, "Nombre Completo:", txtNombre);
        addFormField(formPanel, gbc, 2, "Departamento:", txtDepartamento);
        addFormField(formPanel, gbc, 3, "Salario (Q):", txtSalario);
        addFormField(formPanel, gbc, 4, "Fecha Ingreso:", txtFecha);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(chkActivo, gbc);

        // Panel de Botones
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGuardar = new JButton("Guardar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        btnGuardar.putClientProperty(FlatClientProperties.STYLE, "background: #2b78e4; foreground: #ffffff; bold: true; arc: 10");
        btnActualizar.putClientProperty(FlatClientProperties.STYLE, "background: #2e7d32; foreground: #ffffff; bold: true; arc: 10");
        btnEliminar.putClientProperty(FlatClientProperties.STYLE, "background: #c62828; foreground: #ffffff; bold: true; arc: 10");
        btnLimpiar.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnPanel.add(btnGuardar);
        btnPanel.add(btnActualizar);
        btnPanel.add(btnEliminar);
        btnPanel.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // 3. Tabla Principal con Alineación y Formatos
        String[] columnas = {"ID", "Nombre", "Departamento", "Salario", "Fecha", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.getTableHeader().setReorderingAllowed(false);
        table.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines: true; arc: 10");

        // [MEJORA 2] Selección de registros mediante Doble Clic
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    seleccionarFila();
                }
            }
        });

        // Alineación de celdas en la tabla
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);  // Salario
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Fecha
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Estado

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 4. Barra de Estado Inferior con Botón de Exportación CSV
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        lblStatus = new JLabel(" Sistema listo.");
        lblTotal = new JLabel("Total de empleados: 0 ");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 12));

        // [MEJORA 3] Botón Exportar CSV
        btnExportar = new JButton("📊 Exportar CSV");
        btnExportar.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnExportar.addActionListener(e -> exportarCSV());

        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightStatusPanel.add(lblTotal);
        rightStatusPanel.add(btnExportar);

        statusPanel.add(lblStatus, BorderLayout.WEST);
        statusPanel.add(rightStatusPanel, BorderLayout.EAST);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Eventos de los botones principales
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

    private JTextField createStyledField(String placeholder) {
        JTextField tf = new JTextField(15);
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        return tf;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
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
                    e.getDepartamento(),
                    String.format("Q %,.2f", e.getSalario()),
                    e.getFechaContratacion(),
                    e.isActivo() ? "Activo" : "Inactivo"
                });
            }
            actualizarContador();
            setStatus("Datos cargados correctamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Selecciona una fila de la tabla primero.");
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
            JOptionPane.showMessageDialog(this, "Selecciona una fila de la tabla primero.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea eliminar al empleado '" + txtNombre.getText() + "'?",
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

    // Exportación de filas de la tabla a un archivo CSV accesible por Excel
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
                writer.println("ID,Nombre,Departamento,Salario,Fecha,Estado");
                for (int i = 0; i < table.getRowCount(); i++) {
                    writer.println(String.format("%s,\"%s\",\"%s\",%s,%s,%s",
                        table.getValueAt(i, 0),
                        table.getValueAt(i, 1),
                        table.getValueAt(i, 2),
                        table.getValueAt(i, 3).toString().replace("Q", "").replace(",", "").trim(),
                        table.getValueAt(i, 4),
                        table.getValueAt(i, 5)
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

        String depto = txtDepartamento.getText().trim();
        if (depto.isEmpty()) {
            txtDepartamento.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            valido = false;
        }

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

        Empleado e = new Empleado(nombre, depto, salario, fecha, chkActivo.isSelected());
        if (incluirId) {
            e.setId(Integer.parseInt(txtId.getText()));
        }
        return e;
    }

    private void limpiarErroresVisuales() {
        txtNombre.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtDepartamento.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtSalario.putClientProperty(FlatClientProperties.OUTLINE, null);
        txtFecha.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    private void seleccionarFila() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtNombre.setText(tableModel.getValueAt(modelRow, 1).toString());
        txtDepartamento.setText(tableModel.getValueAt(modelRow, 2).toString());

        String salarioStr = tableModel.getValueAt(modelRow, 3).toString()
                                .replace("Q", "").replace(",", "").trim();
        txtSalario.setText(salarioStr);

        txtFecha.setText(tableModel.getValueAt(modelRow, 4).toString());
        chkActivo.setSelected(tableModel.getValueAt(modelRow, 5).toString().equals("Activo"));

        limpiarErroresVisuales();
        setModoEdicion(true);
        setStatus("Empleado seleccionado: ID " + txtId.getText());
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtDepartamento.setText("");
        txtSalario.setText("");
        txtFecha.setText("");
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
        actualizarContador();
    }

    private void actualizarContador() {
        lblTotal.setText("Total de empleados: " + table.getRowCount() + "  ");
    }

    private void setStatus(String mensaje) {
        lblStatus.setText(" " + mensaje);
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new EmpleadoFrame().setVisible(true));
    }
}