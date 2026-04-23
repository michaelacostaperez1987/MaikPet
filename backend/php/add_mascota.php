<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

require_once 'config.php';

if (!isLoggedIn()) {
    error_log('add_mascota: No autorizado. Session ID: ' . session_id() . ', SESSION: ' . json_encode($_SESSION));
    echo json_encode(['success' => false, 'error' => 'No autorizado', 'debug' => [
        'session_id' => session_id(),
        'session_user_id' => $_SESSION['usuario_id'] ?? null,
        'headers_received' => function_exists('getallheaders') ? getallheaders() : []
    ]]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Metodo no permitido']);
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
    echo json_encode(['success' => false, 'error' => 'Todos los campos son obligatorios']);
    exit;
}

// Validar que no sea venta o reproduccion
$palabrasProhibidas = [
    'venta', 'vendo', 'precio', 'dolares', 'ufs', 'u$s', 
    'comprar', 'compro', 'costo', 'valor', 'permuta', 
    'cambio', 'canje', 'cruza', 'cruzar', 'aparear', 
    'monta', 'reproducir', 'cria', 'criar', 'pedigri', 'acoplar', 'cubrir'
];
$descripcionLower = mb_strtolower($descripcion);

foreach ($palabrasProhibidas as $palabra) {
    if (strpos($descripcionLower, $palabra) !== false) {
        echo json_encode(['success' => false, 'error' => 'No se permiten ventas, cruzas ni permutas. Solo adopciones gratuitas.']);
        exit;
    }
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

$nombre_esc = $conn->real_escape_string($nombre);
$tipo_esc = $conn->real_escape_string($tipo);
$vacunas_esc = $conn->real_escape_string($vacunas);
$descripcion_esc = $conn->real_escape_string($descripcion);
$direccion_esc = $conn->real_escape_string($direccion);
$lat_val = $lat ?? 'NULL';
$lng_val = $lng ?? 'NULL';
$imagen_val = $imagen ? "'" . $conn->real_escape_string($imagen) . "'" : 'NULL';

$sql = "INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, imagen) VALUES ($usuario_id, '$nombre_esc', '$tipo_esc', $edad_meses, '$vacunas_esc', '$descripcion_esc', '$direccion_esc', $lat_val, $lng_val, $imagen_val)";

$result = $conn->query($sql);

if ($result) {
    $id = $conn->insert_id;
    echo json_encode([
        'success' => true,
        'message' => 'Mascota registrada correctamente',
        'id' => $id
    ]);
} else {
    echo json_encode(['success' => false, 'error' => 'Error al registrar: ' . $conn->error]);
}
?>
