<?php
require_once 'config.php';

// Habilitar logging de errores
error_reporting(E_ALL);
ini_set('display_errors', 1);

$logFile = '/tmp/add_mascota_debug.log';
file_put_contents($logFile, "=== " . date('Y-m-d H:i:s') . " ===\n", FILE_APPEND);

$conn = getConnection();

// Log headers
file_put_contents($logFile, "Headers: " . print_r(getallheaders(), true), FILE_APPEND);

// Log session
file_put_contents($logFile, "Session: " . print_r($_SESSION, true), FILE_APPEND);

if (!isLoggedIn()) {
    file_put_contents($logFile, "NOT LOGGED IN\n", FILE_APPEND);
    jsonResponse(['success' => false, 'error' => 'No autorizado']);
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonResponse(['success' => false, 'error' => 'Método no permitido']);
}

$data = json_decode(file_get_contents('php://input'), true);
file_put_contents($logFile, "Data received: " . json_encode($data) . "\n", FILE_APPEND);

$nombre = trim($data['nombre'] ?? '');
$tipo = trim($data['tipo'] ?? '');
$edad_meses = intval($data['edad_meses'] ?? 0);
$vacunas = trim($data['vacunas'] ?? '');
$descripcion = trim($data['descripcion'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$lat = isset($data['lat']) && $data['lat'] !== null ? floatval($data['lat']) : null;
$lng = isset($data['lng']) && $data['lng'] !== null ? floatval($data['lng']) : null;
$imagen = $data['imagen'] ?? null;

// Log processed data
file_put_contents($logFile, "Nombre: $nombre, Tipo: $tipo, Edad: $edad_meses\n", FILE_APPEND);
file_put_contents($logFile, "Imagen length: " . ($imagen ? strlen($imagen) : 'null') . "\n", FILE_APPEND);

if (empty($nombre) || empty($tipo) || $edad_meses <= 0 || empty($vacunas) || empty($direccion)) {
    jsonResponse(['success' => false, 'error' => 'Todos los campos son obligatorios']);
}

$usuario_id = $_SESSION['usuario_id'];

$stmt = $conn->prepare("INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, imagen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("ississssdss", $usuario_id, $nombre, $tipo, $edad_meses, $vacunas, $descripcion, $direccion, $lat, $lng, $imagen);

if ($stmt->execute()) {
    $id = $stmt->insert_id;
    file_put_contents($logFile, "SUCCESS - ID: $id\n", FILE_APPEND);
    jsonResponse([
        'success' => true,
        'message' => 'Mascota registrada correctamente',
        'id' => $id
    ]);
} else {
    $error = $stmt->error;
    file_put_contents($logFile, "ERROR: $error\n", FILE_APPEND);
    jsonResponse(['success' => false, 'error' => 'Error al registrar: ' . $error]);
}
?>
