<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

error_reporting(E_ALL);
ini_set('display_errors', 1);

$log = fopen('/tmp/add_log.txt', 'a');
fwrite($log, "\n\n=== " . date('Y-m-d H:i:s') . " ===\n");

if (!isLoggedIn()) {
    $msg = "NO LOGUEADO";
    fwrite($log, $msg . "\n");
    fclose($log);
    echo json_encode(['success' => false, 'error' => $msg]);
    exit;
}

fwrite($log, "Logueado: " . $_SESSION['usuario_id'] . "\n");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fwrite($log, "Metodo: " . $_SERVER['REQUEST_METHOD'] . "\n");
    fclose($log);
    echo json_encode(['success' => false, 'error' => 'Solo POST']);
    exit;
}

$raw = file_get_contents('php://input');
fwrite($log, "Raw input length: " . strlen($raw) . "\n");
fwrite($log, "Raw input: " . substr($raw, 0, 500) . "\n");

$data = json_decode($raw, true);
if ($data === null) {
    fwrite($log, "JSON decode error: " . json_last_error_msg() . "\n");
    fclose($log);
    echo json_encode(['success' => false, 'error' => 'JSON invalido']);
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

fwrite($log, "nombre: $nombre\n");
fwrite($log, "tipo: $tipo\n");
fwrite($log, "edad: $edad_meses\n");
fwrite($log, "vacunas: $vacunas\n");
fwrite($log, "direccion: $direccion\n");
fwrite($log, "lat: $lat\n");
fwrite($log, "lng: $lng\n");
fwrite($log, "imagen: " . ($imagen ? "presente (" . strlen($imagen) . ")" : "null") . "\n");

if (empty($nombre) || empty($tipo) || $edad_meses <= 0 || empty($vacunas) || empty($direccion)) {
    fwrite($log, "Validacion fallida\n");
    fclose($log);
    echo json_encode(['success' => false, 'error' => 'Campos invalidos']);
    exit;
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

$stmt = $conn->prepare("INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, imagen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("ississssdss", $usuario_id, $nombre, $tipo, $edad_meses, $vacunas, $descripcion, $direccion, $lat, $lng, $imagen);

if ($stmt->execute()) {
    $id = $stmt->insert_id;
    fwrite($log, "SUCCESS - ID: $id\n");
    fclose($log);
    echo json_encode([
        'success' => true,
        'message' => 'Mascota registrada correctamente',
        'id' => $id
    ]);
} else {
    $error = $stmt->error;
    fwrite($log, "ERROR: $error\n");
    fclose($log);
    echo json_encode(['success' => false, 'error' => 'Error al registrar: ' . $error]);
}
?>
