package empleados.modelo;

import java.time.LocalDate;
import java.time.Period;

public class Empleado {

    private int id;
    private String nombre;
    private String telefono; // Mejora #1
    private String departamento;
    private double salario;
    private LocalDate fechaContratacion;
    private boolean activo;

    public Empleado() {}

    // Constructor sin ID (con teléfono)
    public Empleado(String nombre, String telefono, String departamento, double salario, LocalDate fechaContratacion, boolean activo) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.departamento = departamento;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }

    // Constructor completo con ID (con teléfono)
    public Empleado(int id, String nombre, String telefono, String departamento, double salario, LocalDate fechaContratacion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.departamento = departamento;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }

    // MEJORA #6: Cálculo dinámico de Antigüedad en Java
    public String getAntiguedadCalculada() {
        if (fechaContratacion == null) {
            return "N/A";
        }
        Period periodo = Period.between(fechaContratacion, LocalDate.now());
        int anios = periodo.getYears();
        int meses = periodo.getMonths();

        if (anios == 0 && meses == 0) {
            return "Menos de 1 mes";
        } else if (anios == 0) {
            return meses + (meses == 1 ? " mes" : " meses");
        } else if (meses == 0) {
            return anios + (anios == 1 ? " año" : " años");
        } else {
            return anios + (anios == 1 ? " año, " : " años, ") + meses + (meses == 1 ? " mes" : " meses");
        }
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}