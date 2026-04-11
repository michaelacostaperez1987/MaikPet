-- ============================================
-- SCRIPT SQL PARA MAIKPET
-- Ejecutar en phpMyAdmin
-- ============================================

-- TABLA DE USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) DEFAULT '',
    direccion VARCHAR(255) DEFAULT '',
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- TABLA DE MASCOTAS
CREATE TABLE IF NOT EXISTS mascotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('Perro', 'Gato') NOT NULL,
    edad_meses INT NOT NULL DEFAULT 0,
    vacunas ENUM('Si', 'No') NOT NULL DEFAULT 'No',
    descripcion TEXT,
    direccion VARCHAR(255) NOT NULL,
    lat DECIMAL(10, 8) DEFAULT NULL,
    lng DECIMAL(11, 8) DEFAULT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- INDEX PARA MEJORAR RENDIMIENTO
CREATE INDEX idx_usuario ON mascotas(usuario_id);
CREATE INDEX idx_tipo ON mascotas(tipo);
CREATE INDEX idx_coordenadas ON mascotas(lat, lng);

-- ============================================
-- DATOS DE PRUEBA - USUARIOS
-- Password: password123
-- ============================================
INSERT INTO usuarios (nombre, email, password, telefono, direccion) VALUES
('Maria Garcia', 'maria@ejemplo.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '099 123 456', '18 de Julio 1234, Montevideo'),
('Juan Perez', 'juan@ejemplo.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '098 234 567', 'Av. Italia 5678, Montevideo'),
('Ana Rodriguez', 'ana@ejemplo.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '097 345 678', 'Rivera 3142, Montevideo');

-- ============================================
-- DATOS DE PRUEBA - MASCOTAS
-- ============================================
INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng) VALUES
(1, 'Luna', 'Perro', 24, 'Si', 'Golden retriever muy juguetona y cariñosa.', '18 de Julio 1234, Montevideo, Uruguay', -34.9011, -56.1645),
(1, 'Simba', 'Gato', 12, 'Si', 'Gato naranja muy tranquilo, ideal para departamento.', '18 de Julio 1234, Montevideo, Uruguay', -34.9011, -56.1645),
(2, 'Max', 'Perro', 36, 'Si', 'Pastor alemán entrenado, muy obediente y protector.', 'Ciudad Vieja, Montevideo, Uruguay', -34.9075, -56.2031),
(2, 'Mia', 'Gato', 8, 'No', 'Gatita negra muy curiosa.', 'Av. Brasil 2890, Pocitos, Montevideo, Uruguay', -34.9171, -56.1540),
(3, 'Rocky', 'Perro', 48, 'Si', 'Bull terrier ingles, muy fiel y leal.', 'Rivera 3142, Buceo, Montevideo, Uruguay', -34.8941, -56.1649),
(3, 'Nala', 'Gato', 6, 'Si', 'Hembra mestiza, muy mansa y glotona.', 'Av. Rivera 4567, Malvín, Montevideo, Uruguay', -34.8850, -56.0850),
(1, 'Beethoven', 'Perro', 18, 'Si', 'Beagle muy activo, necesita espacio para correr.', 'Jackson 1234, Cordón, Montevideo, Uruguay', -34.9200, -56.1700),
(2, 'Luna', 'Gato', 14, 'Si', 'Persa hembra, tranquila y independiente.', 'Ellauri 567, Punta Carretas, Montevideo, Uruguay', -34.9340, -56.1580),
(3, 'Toby', 'Perro', 30, 'Si', 'Mezcla de labrador, muy amigable con otros perros.', 'Av. 8 de Octubre 2833, La Blanqueada, Montevideo, Uruguay', -34.9000, -56.1500),
(1, 'Mishi', 'Gato', 10, 'No', 'Gatito común, busca familia amorosa.', 'Av. Agraciada 4256, Prado, Montevideo, Uruguay', -34.8600, -56.1700);
