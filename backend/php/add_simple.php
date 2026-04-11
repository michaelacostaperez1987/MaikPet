<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

require_once 'config.php';

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No autorizado']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Solo POST']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

$nombre = trim($data['nombre'] ?? '');
$tipo = trim($data['tipo'] ?? '');
$edad_meses = intval($data['edad_meses'] ?? 0);
$vacunas = trim($data['vacunas'] ?? '');
$descripcion = trim($data['descripcion'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$lat = isset($data['lat']) && $data['lat'] !== null ? floatval($data['lat']) : null;
$lng = isset($data['lng']) && $data['lng'] !== null ? floatval($data['lng']) : null;
$imagen = $data['imagen'] ?? null;

if (empty($nombre) || empty($tipo) || $edad_meses <= 0 || empty($vacunas) || empty($direccion)) {
    echo json_encode(['success' => false, 'error' => 'Campos requeridos']);
    exit;
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

// Usar query() directo en vez de prepare() para ver si el problema es el prepared statement
$sql = "INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, imagen) VALUES ($usuario_id, '$nombre', '$tipo', $edad_meses, '$vacunas', '$descripcion', '$direccion', " . ($lat ?? "NULL") . ", " . ($lng ?? "NULL") . ", " . ($imagen ? "'" . $conn->real_escape_string($imagen) . "'" : "NULL") . ")";

$result = $conn->query($sql);

if ($result) {
    $id = $conn->insert_id;
    echo json_encode([
        'success' => true,
        'message' => 'Mascota registrada correctamente',
        'id' => $id
    ]);
} else {
    echo json_encode(['success' => false, 'error' => 'Error SQL: ' . $conn->error]);
}
?>
