-- Cambiar columna imagen de VARCHAR(255) a MEDIUMTEXT para almacenar imágenes base64
ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL;
