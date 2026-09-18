package empleados.dao;

import empleados.modelo.Empleado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public void crear(Empleado e) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, departamento, salario, fecha_contratacion, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDepartamento());
            ps.setDouble(3, e.getSalario());
            ps.setDate(4, Date.valueOf(e.getFechaContratacion()));
            ps.setBoolean(5, e.isActivo());
            ps.executeUpdate();
        }
    }

    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleados";
        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Empleado e = new Empleado(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("departamento"),
                    rs.getDouble("salario"),
                    rs.getDate("fecha_contratacion").toLocalDate(),
                    rs.getBoolean("activo")
                );
                lista.add(e);
            }
        }
        return lista;
    }

    public void actualizar(Empleado e) throws SQLException {
        String sql = "UPDATE empleados SET nombre=?, departamento=?, salario=?, fecha_contratacion=?, activo=? WHERE id=?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDepartamento());
            ps.setDouble(3, e.getSalario());
            ps.setDate(4, Date.valueOf(e.getFechaContratacion()));
            ps.setBoolean(5, e.isActivo());
            ps.setInt(6, e.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM empleados WHERE id=?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
