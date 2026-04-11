<?php
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');

require_once 'config.php';

$debug = [];

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No autorizado', 'debug' => $debug]);
    exit;
}

$debug['logueado'] = true;
$debug['usuario_id'] = $_SESSION['usuario_id'] ?? null;

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Solo POST', 'debug' => $debug]);
    exit;
}

$raw = file_get_contents('php://input');
$debug['raw_length'] = strlen($raw);
$debug['raw_preview'] = substr($raw, 0, 100);

$data = json_decode($raw, true);
if ($data === null) {
    $debug['json_error'] = json_last_error_msg();
    echo json_encode(['success' => false, 'error' => 'JSON invalido', 'debug' => $debug]);
    exit;
}

$nombre = trim($data['nombre'] ?? '');
$tipo = trim($data['tipo'] ?? '');
$edad_meses = intval($data['edad_meses'] ?? 0);
$vacunas = trim($data['vacunas'] ?? '');
$descripcion = trim($data['descripcion'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$lat = isset($data['lat']) && $data['lat'] !== null ? floatval($data['lat']) : null;
$lng = isset($data['lng']) && $data['lng'] !== null ? floatval($data['lng']) : null;
$imagen = $data['imagen'] ?? null;

$debug['parsed'] = [
    'nombre' => $nombre,
    'tipo' => $tipo,
    'edad' => $edad_meses,
    'vacunas' => $vacunas,
    'direccion' => $direccion,
    'lat' => $lat,
    'lng' => $lng,
    'imagen_len' => $imagen ? strlen($imagen) : 0
];

if (empty($nombre) || empty($tipo) || $edad_meses <= 0 || empty($vacunas) || empty($direccion)) {
    echo json_encode(['success' => false, 'error' => 'Campos invalidos', 'debug' => $debug]);
    exit;
}

try {
    $conn = getConnection();
    $usuario_id = $_SESSION['usuario_id'];
    
    $stmt = $conn->prepare("INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, imagen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("ississssdss", $usuario_id, $nombre, $tipo, $edad_meses, $vacunas, $descripcion, $direccion, $lat, $lng, $imagen);
    
    if ($stmt->execute()) {
        $id = $stmt->insert_id;
        echo json_encode([
            'success' => true,
            'message' => 'Mascota registrada correctamente',
            'id' => $id,
            'debug' => $debug
        ]);
    } else {
        echo json_encode(['success' => false, 'error' => 'Error SQL: ' . $stmt->error, 'debug' => $debug]);
    }
} catch (Exception $e) {
    echo json_encode(['success' => false, 'error' => 'Excepcion: ' . $e->getMessage(), 'debug' => $debug]);
}
?>
