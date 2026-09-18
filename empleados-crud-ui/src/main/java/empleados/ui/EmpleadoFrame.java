package empleados.ui;

import empleados.dao.EmpleadoDAO;
import empleados.modelo.Empleado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EmpleadoFrame extends JFrame {
    private JTextField txtNombre, txtDepartamento, txtSalario, txtFecha;
    private JCheckBox chkActivo;
    private JTable table;
    private DefaultTableModel model;
    private EmpleadoDAO dao = new EmpleadoDAO();
    private int idSeleccionado = -1;

    public EmpleadoFrame() {
        setTitle("Sistema de Gestión de Empleados");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Formulario
        JPanel panelForm = new JPanel(new GridLayout(6, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Empleado"));

        panelForm.add(new JLabel("Nombre Completo:"));
        txtNombre = new JTextField();
        panelForm.add(txtNombre);

        panelForm.add(new JLabel("Departamento:"));
        txtDepartamento = new JTextField();
        panelForm.add(txtDepartamento);

        panelForm.add(new JLabel("Salario Mensual (Q):"));
        txtSalario = new JTextField();
        panelForm.add(txtSalario);

        panelForm.add(new JLabel("Fecha Contratación (AAAA-MM-DD):"));
        txtFecha = new JTextField();
        panelForm.add(txtFecha);

        panelForm.add(new JLabel("Estado:"));
        chkActivo = new JCheckBox("Activo", true);
        panelForm.add(chkActivo);

        JButton btnGuardar = new JButton("Guardar / Actualizar");
        JButton btnLimpiar = new JButton("Limpiar Formulario");
        panelForm.add(btnGuardar);
        panelForm.add(btnLimpiar);

        add(panelForm, BorderLayout.NORTH);

        // Tabla
        model = new DefaultTableModel(new String[]{"ID", "Nombre", "Departamento", "Salario", "Fecha Contratación", "Estado"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Botón inferior
        JPanel panelBotones = new JPanel();
        JButton btnEliminar = new JButton("Eliminar Empleado Seleccionado");
        panelBotones.add(btnEliminar);
        add(panelBotones, BorderLayout.SOUTH);

        // Listeners
        btnGuardar.addActionListener(e -> guardarEmpleado());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnEliminar.addActionListener(e -> eliminarEmpleado());

        table.getSelectionModel().addListSelectionListener(e -> seleccionarFila());

        cargarTabla();
    }

    private void cargarTabla() {
        model.setRowCount(0);
        try {
            List<Empleado> empleados = dao.listarTodos();
            for (Empleado emp : empleados) {
                model.addRow(new Object[]{
                    emp.getId(),
                    emp.getNombre(),
                    emp.getDepartamento(),
                    String.format("Q%.2f", emp.getSalario()),
                    emp.getFechaContratacion(),
                    emp.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar los empleados: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarEmpleado() {
        String nombre = txtNombre.getText().trim();
        String departamento = txtDepartamento.getText().trim();
        String salarioStr = txtSalario.getText().trim();
        String fechaStr = txtFecha.getText().trim();
        boolean activo = chkActivo.isSelected();

        if (nombre.isEmpty() || departamento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre y el departamento no pueden estar vacíos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double salario;
        try {
            salario = Double.parseDouble(salarioStr);
            if (salario <= 0) {
                JOptionPane.showMessageDialog(this, "El salario debe ser un monto mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un salario numérico válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr);
            if (fecha.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "La fecha de contratación no puede ser una fecha futura.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "La fecha debe tener el formato AAAA-MM-DD (ej: 2024-03-15).", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (idSeleccionado == -1) {
                dao.crear(new Empleado(nombre, departamento, salario, fecha, activo));
                JOptionPane.showMessageDialog(this, "Empleado registrado con éxito.");
            } else {
                dao.actualizar(new Empleado(idSeleccionado, nombre, departamento, salario, fecha, activo));
                JOptionPane.showMessageDialog(this, "Empleado actualizado con éxito.");
            }
            limpiarCampos();
            cargarTabla();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error en la base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarEmpleado() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de eliminar definitivamente este empleado del sistema?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.eliminar(idSeleccionado);
                limpiarCampos();
                cargarTabla();
                JOptionPane.showMessageDialog(this, "Empleado eliminado del sistema.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarFila() {
        int row = table.getSelectedRow();
        if (row != -1) {
            idSeleccionado = (int) model.getValueAt(row, 0);
            txtNombre.setText((String) model.getValueAt(row, 1));
            txtDepartamento.setText((String) model.getValueAt(row, 2));
            
            String salarioClean = model.getValueAt(row, 3).toString().replace("Q", "").replace(",", "");
            txtSalario.setText(salarioClean);
            
            txtFecha.setText(model.getValueAt(row, 4).toString());
            chkActivo.setSelected(model.getValueAt(row, 5).toString().equals("Activo"));
        }
    }

    private void limpiarCampos() {
        idSeleccionado = -1;
        txtNombre.setText("");
        txtDepartamento.setText("");
        txtSalario.setText("");
        txtFecha.setText("");
        chkActivo.setSelected(true);
        table.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmpleadoFrame().setVisible(true));
    }
}
