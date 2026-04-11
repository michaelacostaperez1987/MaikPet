-- Agregar columna de imagen a la tabla mascotas
ALTER TABLE mascotas ADD COLUMN imagen VARCHAR(255) DEFAULT NULL AFTER lng;
