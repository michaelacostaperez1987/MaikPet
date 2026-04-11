<?php
require_once 'config.php';

$conn = getConnection();

$result = $conn->query("ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL");

if ($result) {
    jsonResponse(['success' => true, 'message' => 'Columna imagen cambiada a MEDIUMTEXT']);
} else {
    jsonResponse(['success' => false, 'error' => 'Error: ' . $conn->error]);
}
?>
