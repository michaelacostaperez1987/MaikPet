<?php
header('Content-Type: text/plain; charset=utf-8');

echo "=== PHP Configuration ===\n";
echo "max_execution_time: " . ini_get('max_execution_time') . "\n";
echo "max_input_time: " . ini_get('max_input_time') . "\n";
echo "memory_limit: " . ini_get('memory_limit') . "\n";
echo "post_max_size: " . ini_get('post_max_size') . "\n";
echo "upload_max_filesize: " . ini_get('upload_max_filesize') . "\n";

echo "\n=== Content Length ===\n";
echo "CONTENT_LENGTH: " . (isset($_SERVER['CONTENT_LENGTH']) ? $_SERVER['CONTENT_LENGTH'] : 'N/A') . " bytes\n";

echo "\n=== Testing base64 insert ===\n";
require_once 'config.php';

$conn = getConnection();

// Crear tabla de test si no existe
$conn->query("CREATE TABLE IF NOT EXISTS mascotas_test (id INT AUTO_INCREMENT PRIMARY KEY, imagen MEDIUMTEXT)");

// Generar una imagen de prueba (1KB base64)
$testData = str_repeat('A', 1000); // ~1KB
$testBase64 = "data:image/jpeg;base64," . $testData;

$stmt = $conn->prepare("INSERT INTO mascotas_test (imagen) VALUES (?)");
$stmt->bind_param("s", $testBase64);

if ($stmt->execute()) {
    echo "✅ Test insert SUCCESS - ID: " . $stmt->insert_id . "\n";
} else {
    echo "❌ Test insert ERROR: " . $stmt->error . "\n";
}

// Limpiar
$conn->query("DROP TABLE IF EXISTS mascotas_test");
?>
