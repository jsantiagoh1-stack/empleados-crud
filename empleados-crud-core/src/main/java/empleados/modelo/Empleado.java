package empleados.modelo;

import java.time.LocalDate;
import java.time.Period;

public class Empleado {
    private int id;
    private String nombre;
    private String departamento;
    private double salario;
    private LocalDate fechaContratacion;
    private boolean activo;
    private String telefono; //Mejora #1

    public Empleado() {}

    public Empleado(String nombre, String departamento, double salario, LocalDate fechaContratacion, boolean activo) {
        this.nombre = nombre;
        this.departamento = departamento;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
        this.telefono = telefono;
    }

    public Empleado(int id, String nombre, String departamento, double salario, LocalDate fechaContratacion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.departamento = departamento;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
        this.telefono = telefono;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) {this.telefono = telefono;}
}