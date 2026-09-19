CREATE DATABASE IF NOT EXISTS empresa_db;
USE empresa_db;
DROP TABLE IF EXISTS empleados;

CREATE TABLE IF NOT EXISTS empleados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    departamento VARCHAR(100) NOT NULL,
    salario DECIMAL(10, 2) NOT NULL,
    fecha_contratacion DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Registro prueba
INSERT INTO empleados (nombre, telefono, departamento, salario, fecha_contratacion, activo) VALUES
('Carlos Mendoza', '5555-0101', 'Informática', 6500.00, '2021-03-15', TRUE),
('Ana Lucía Gómez', '5555-0102', 'Recursos Humanos', 4800.50, '2023-01-10', TRUE),
('Roberto Estrada', '5555-0103', 'Ventas', 5200.00, '2020-06-01', TRUE),
('María Fernández', '5555-0104', 'Contabilidad', 7100.00, '2019-09-20', FALSE),
('Jorge Morales', '5555-0105', 'Informática', 3900.00, '2024-02-01', TRUE);

SELECT * FROM empleados;