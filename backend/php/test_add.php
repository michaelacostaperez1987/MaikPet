<?php
require_once 'config.php';

header('Content-Type: text/plain; charset=utf-8');

$conn = getConnection();

if (!isLoggedIn()) {
    echo "No logueado\n";
    exit;
}

echo "Logueado como usuario_id: " . $_SESSION['usuario_id'] . "\n\n";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    echo "Datos recibidos:\n";
    echo "  nombre: " . ($data['nombre'] ?? 'N/A') . "\n";
    echo "  tipo: " . ($data['tipo'] ?? 'N/A') . "\n";
    echo "  edad_meses: " . ($data['edad_meses'] ?? 'N/A') . "\n";
    echo "  vacunas: " . ($data['vacunas'] ?? 'N/A') . "\n";
    echo "  direccion: " . ($data['direccion'] ?? 'N/A') . "\n";
    echo "  lat: " . ($data['lat'] ?? 'N/A') . "\n";
    echo "  lng: " . ($data['lng'] ?? 'N/A') . "\n";
    echo "  imagen: " . (isset($data['imagen']) ? 'presente (' . strlen($data['imagen']) . ' chars)' : 'null') . "\n";
    
    // Intentar insertar
    $stmt = $conn->prepare("INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, imagen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    
    $usuario_id = $_SESSION['usuario_id'];
    $nombre = trim($data['nombre'] ?? '');
    $tipo = trim($data['tipo'] ?? '');
    $edad_meses = intval($data['edad_meses'] ?? 0);
    $vacunas = trim($data['vacunas'] ?? '');
    $descripcion = trim($data['descripcion'] ?? '');
    $direccion = trim($data['direccion'] ?? '');
    $lat = isset($data['lat']) ? floatval($data['lat']) : null;
    $lng = isset($data['lng']) ? floatval($data['lng']) : null;
    $imagen = $data['imagen'] ?? null;
    
    $stmt->bind_param("ississssdss", $usuario_id, $nombre, $tipo, $edad_meses, $vacunas, $descripcion, $direccion, $lat, $lng, $imagen);
    
    if ($stmt->execute()) {
        echo "\n✅ SUCCESS - ID: " . $stmt->insert_id . "\n";
    } else {
        echo "\n❌ ERROR: " . $stmt->error . "\n";
    }
} else {
    echo "Usa POST para probar";
}
?>
