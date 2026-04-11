<?php
require_once 'config.php';

$conn = getConnection();

// Verificar estructura actual
$result = $conn->query("DESCRIBE mascotas");
$hasImagen = false;
while ($row = $result->fetch_assoc()) {
    if ($row['Field'] === 'imagen') {
        $hasImagen = true;
        echo "Columna actual: {$row['Field']} - Tipo: {$row['Type']}\n";
        break;
    }
}

// Modificar columna
$result = $conn->query("ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL");

if ($result) {
    echo json_encode(['success' => true, 'message' => 'Columna imagen cambiada a MEDIUMTEXT']);
} else {
    echo json_encode(['success' => false, 'error' => $conn->error]);
}
?>
