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

$id = intval($data['id'] ?? 0);
$nombre = trim($data['nombre'] ?? '');
$tipo = trim($data['tipo'] ?? '');
$edad_meses = intval($data['edad_meses'] ?? 0);
$vacunas = trim($data['vacunas'] ?? '');
$descripcion = trim($data['descripcion'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$lat = isset($data['lat']) && $data['lat'] !== null ? floatval($data['lat']) : null;
$lng = isset($data['lng']) && $data['lng'] !== null ? floatval($data['lng']) : null;
$imagen = $data['imagen'] ?? null;

if ($id <= 0) {
    echo json_encode(['success' => false, 'error' => 'ID invalido']);
    exit;
}

if (empty($nombre) || empty($tipo) || $edad_meses <= 0 || empty($vacunas)) {
    echo json_encode(['success' => false, 'error' => 'Campos requeridos']);
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

// Verificar propiedad
$check = $conn->query("SELECT id FROM mascotas WHERE id = $id AND usuario_id = $usuario_id");
if ($check->num_rows === 0) {
    echo json_encode(['success' => false, 'error' => 'No autorizado']);
    exit;
}

$descripcion_esc = $conn->real_escape_string($descripcion);
$direccion_esc = $conn->real_escape_string($direccion);
$lat_val = $lat ?? 'NULL';
$lng_val = $lng ?? 'NULL';
$imagen_val = $imagen ? "'" . $conn->real_escape_string($imagen) . "'" : 'NULL';

$sql = "UPDATE mascotas SET nombre='$nombre', tipo='$tipo', edad_meses=$edad_meses, vacunas='$vacunas', descripcion='$descripcion_esc', direccion='$direccion_esc', lat=$lat_val, lng=$lng_val, imagen=$imagen_val WHERE id=$id AND usuario_id=$usuario_id";

$result = $conn->query($sql);

if ($result) {
    echo json_encode([
        'success' => true,
        'message' => 'Mascota actualizada correctamente'
    ]);
} else {
    echo json_encode(['success' => false, 'error' => 'Error SQL: ' . $conn->error]);
}
?>
