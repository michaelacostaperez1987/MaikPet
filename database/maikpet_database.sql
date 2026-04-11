-- =====================================================
-- MAIKPET - SCRIPT DE BASE DE DATOS COMPLETO
-- MySQL 5.7+ / MariaDB 10.3+
-- =====================================================

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS maikpet_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE maikpet_db;

-- =====================================================
-- TABLA: usuarios
-- Almacena los usuarios registrados en la app
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre completo del usuario',
    direccion VARCHAR(255) DEFAULT '' COMMENT 'Dirección del usuario',
    telefono VARCHAR(20) DEFAULT '' COMMENT 'Teléfono de contacto',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Email único del usuario',
    password VARCHAR(255) NOT NULL COMMENT 'Hash de la contraseña (password_hash)',
    fcm_token VARCHAR(255) DEFAULT NULL COMMENT 'Token de Firebase Cloud Messaging',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de registro'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla de usuarios';

-- =====================================================
-- TABLA: mascotas
-- Almacena las mascotas publicadas para adopción
-- =====================================================
CREATE TABLE IF NOT EXISTS mascotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL COMMENT 'ID del usuario que publica',
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre de la mascota',
    tipo ENUM('Perro', 'Gato') NOT NULL COMMENT 'Tipo de mascota',
    edad_meses INT NOT NULL COMMENT 'Edad en meses',
    vacunas ENUM('Si', 'No') NOT NULL DEFAULT 'No' COMMENT 'Si tiene vacunas',
    descripcion TEXT COMMENT 'Descripción adicional',
    direccion VARCHAR(255) NOT NULL COMMENT 'Dirección donde está la mascota',
    lat DOUBLE DEFAULT NULL COMMENT 'Latitud de la ubicación',
    lng DOUBLE DEFAULT NULL COMMENT 'Longitud de la ubicación',
    imagen MEDIUMTEXT DEFAULT NULL COMMENT 'Imagen en Base64',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de publicación',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario (usuario_id),
    INDEX idx_fecha (fecha_creacion),
    INDEX idx_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla de mascotas';

-- =====================================================
-- IMPORTANTE: Cambiar columna imagen a MEDIUMTEXT
-- Las imágenes base64 son largas, VARCHAR(255) NO es suficiente
-- =====================================================
ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL;

-- =====================================================
-- PROCEDIMIENTO: Auto-eliminar mascotas de más de 30 días
-- Se ejecuta automáticamente en get_mascotas.php
-- =====================================================
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS limpiar_mascotas_vencidas()
BEGIN
    DELETE FROM mascotas WHERE fecha_creacion < DATE_SUB(NOW(), INTERVAL 30 DAY);
END //
DELIMITER ;

-- =====================================================
-- USUARIO DE PRUEBA
-- Password: test123
-- Hash generado con password_hash('test123', PASSWORD_DEFAULT)
-- =====================================================
INSERT INTO usuarios (nombre, email, password, telefono, direccion) VALUES 
('Michael', 'michael@michael.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+59899123456', 'Montevideo, Uruguay')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- =====================================================
-- CONSULTAS ÚTILES
-- =====================================================

-- Ver todas las mascotas con información del dueño
-- SELECT m.*, u.nombre as dueno_nombre, u.telefono, u.email 
-- FROM mascotas m 
-- LEFT JOIN usuarios u ON m.usuario_id = u.id;

-- Contar mascotas por tipo
-- SELECT tipo, COUNT(*) as cantidad FROM mascotas GROUP BY tipo;

-- Ver usuarios con más mascotas publicadas
-- SELECT u.nombre, COUNT(m.id) as total 
-- FROM usuarios u 
-- LEFT JOIN mascotas m ON u.id = m.usuario_id 
-- GROUP BY u.id 
-- ORDER BY total DESC;

-- Eliminar mascotas de más de 30 días (manual)
-- DELETE FROM mascotas WHERE fecha_creacion < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
