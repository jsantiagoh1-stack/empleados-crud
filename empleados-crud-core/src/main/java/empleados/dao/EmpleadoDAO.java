package empleados.dao;

import empleados.modelo.Empleado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/empresa_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "root"; // Cambia si tu clave de MySQL es diferente
        return DriverManager.getConnection(url, user, pass);
    }

    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, departamento, salario, fecha_contratacion, activo, telefono FROM empleados";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Empleado e = new Empleado();
                e.setId(rs.getInt("id"));
                e.setNombre(rs.getString("nombre"));
                e.setDepartamento(rs.getString("departamento"));
                e.setSalario(rs.getDouble("salario"));
                
                Date fecha = rs.getDate("fecha_contratacion");
                if (fecha != null) {
                    e.setFechaContratacion(fecha.toLocalDate());
                }
                
                e.setActivo(rs.getBoolean("activo"));
                e.setTelefono(rs.getString("telefono")); // MEJORA #1
                lista.add(e);
            }
        }
        return lista;
    }

    public void crear(Empleado e) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, departamento, salario, fecha_contratacion, activo, telefono) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, e.getNombre());
            stmt.setString(2, e.getDepartamento());
            stmt.setDouble(3, e.getSalario());
            stmt.setDate(4, Date.valueOf(e.getFechaContratacion()));
            stmt.setBoolean(5, e.isActivo());
            stmt.setString(6, e.getTelefono()); // MEJORA #1
            stmt.executeUpdate();
        }
    }

    public void actualizar(Empleado e) throws SQLException {
        String sql = "UPDATE empleados SET nombre=?, departamento=?, salario=?, fecha_contratacion=?, activo=?, telefono=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, e.getNombre());
            stmt.setString(2, e.getDepartamento());
            stmt.setDouble(3, e.getSalario());
            stmt.setDate(4, Date.valueOf(e.getFechaContratacion()));
            stmt.setBoolean(5, e.isActivo());
            stmt.setString(6, e.getTelefono()); // MEJORA #1
            stmt.setInt(7, e.getId());
            stmt.executeUpdate();
        }
    }


    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM empleados WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
