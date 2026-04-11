<?php
require_once 'config.php';

$conn = getConnection();

// Verificar si la columna existe
$result = $conn->query("SHOW COLUMNS FROM mascotas LIKE 'imagen'");
$columnExists = $result->num_rows > 0;

if (!$columnExists) {
    // Agregar columna si no existe
    $result = $conn->query("ALTER TABLE mascotas ADD COLUMN imagen MEDIUMTEXT DEFAULT NULL AFTER lng");
    if ($result) {
        echo json_encode(['success' => true, 'message' => 'Columna imagen agregada como MEDIUMTEXT']);
    } else {
        echo json_encode(['success' => false, 'error' => 'Error al agregar columna: ' . $conn->error]);
    }
} else {
    // Modificar columna existente
    $result = $conn->query("ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL");
    if ($result) {
        echo json_encode(['success' => true, 'message' => 'Columna imagen modificada a MEDIUMTEXT']);
    } else {
        echo json_encode(['success' => false, 'error' => 'Error al modificar columna: ' . $conn->error]);
    }
}
?>
