package empleados.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import empleados.dao.EmpleadoDAO;
import empleados.modelo.Empleado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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
    private JButton btnGuardar, btnActualizar, btnEliminar, btnLimpiar;

    public EmpleadoFrame() {
        initUI();
        cargarDatos();
    }

    private void initUI() {
        setTitle("Sistema de Gestión de Empleados");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // Header Superior con Buscador Integrado
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

        // Panel de Formulario
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

        // Panel de Botones con Estilos Coloridos
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

        // Tabla Principal
        String[] columnas = {"ID", "Nombre", "Departamento", "Salario", "Fecha", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.getTableHeader().setReorderingAllowed(false);
        table.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines: true; arc: 10");

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        mainPanel.add(scrollPane, BorderLayout.CENTER);

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

    private void cargarDatos() {
        tableModel.setRowCount(0);
        try {
            List<Empleado> lista = dao.listarTodos();
            for (Empleado e : lista) {
                tableModel.addRow(new Object[]{
                    e.getId(), e.getNombre(), e.getDepartamento(),
                    String.format("%.2f", e.getSalario()),
                    e.getFechaContratacion(),
                    e.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarEmpleado() {
        try {
            Empleado e = obtenerEmpleadoDesdeFormulario(false);
            dao.crear(e);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Empleado registrado correctamente.");
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
            JOptionPane.showMessageDialog(this, "Registro actualizado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarEmpleado() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila de la tabla primero.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Deseas eliminar este registro?", "Confirmación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.eliminar(Integer.parseInt(txtId.getText()));
                cargarDatos();
                limpiarFormulario();
                JOptionPane.showMessageDialog(this, "Empleado eliminado.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage());
            }
        }
    }

    private Empleado obtenerEmpleadoDesdeFormulario(boolean incluirId) {
        String nombre = txtNombre.getText().trim();
        String depto = txtDepartamento.getText().trim();
        if (nombre.isEmpty() || depto.isEmpty()) throw new IllegalArgumentException("Nombre y Departamento son obligatorios.");

        double salario;
        try { salario = Double.parseDouble(txtSalario.getText().trim().replace(",", ".")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("El salario debe ser un número válido."); }

        LocalDate fecha;
        try { fecha = LocalDate.parse(txtFecha.getText().trim()); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Fecha inválida (usa formato YYYY-MM-DD)."); }

        Empleado e = new Empleado(nombre, depto, salario, fecha, chkActivo.isSelected());
        if (incluirId) e.setId(Integer.parseInt(txtId.getText()));
        return e;
    }

    private void seleccionarFila() {
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtNombre.setText(tableModel.getValueAt(modelRow, 1).toString());
        txtDepartamento.setText(tableModel.getValueAt(modelRow, 2).toString());
        txtSalario.setText(tableModel.getValueAt(modelRow, 3).toString().replace(",", "."));
        txtFecha.setText(tableModel.getValueAt(modelRow, 4).toString());
        chkActivo.setSelected(tableModel.getValueAt(modelRow, 5).toString().equals("Activo"));
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtDepartamento.setText("");
        txtSalario.setText("");
        txtFecha.setText("");
        chkActivo.setSelected(true);
        table.clearSelection();
    }

    private void filtrar() {
        String query = txtBuscar.getText().trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new EmpleadoFrame().setVisible(true));
    }
}