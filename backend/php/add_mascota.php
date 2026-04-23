<?php
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Session-Id, X-Auth-Token, X-User-Id');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'config.php'; // isLoggedIn() ya maneja la autenticación

if (!isLoggedIn()) {
    jsonResponse(['success' => false, 'error' => 'No autorizado']);
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonResponse(['success' => false, 'error' => 'Metodo no permitido']);
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
    jsonResponse(['success' => false, 'error' => 'Todos los campos son obligatorios']);
}

$palabrasProhibidas = [
    'venta', 'vendo', 'precio', 'dolares', 'ufs', 'u$s', 
    'comprar', 'compro', 'costo', 'valor', 'permuta', 
    'cambio', 'canje', 'cruza', 'cruzar', 'aparear', 
    'monta', 'reproducir', 'cria', 'criar', 'pedigri', 'acoplar', 'cubrir'
];
$descripcionLower = mb_strtolower($descripcion);

foreach ($palabrasProhibidas as $palabra) {
    if (strpos($descripcionLower, $palabra) !== false) {
        jsonResponse(['success' => false, 'error' => 'No se permiten ventas, cruzas ni permutas. Solo adopciones gratuitas.']);
    }
}

$conn = getConnection();

// Obtener user_id desde header o sesión
$userIdFromHeader = $_SERVER['HTTP_X_USER_ID'] ?? '';
$usuario_id = !empty($userIdFromHeader) ? intval($userIdFromHeader) : ($_SESSION['usuario_id'] ?? 0);

if ($usuario_id <= 0) {
    jsonResponse(['success' => false, 'error' => 'Usuario no identificado']);
}

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
    
    $mascota = [
        'id' => $id,
        'nombre' => $nombre,
        'tipo' => $tipo,
        'edad_meses' => $edad_meses,
        'direccion' => $direccion
    ];
    
    notifyNewMascota($mascota, $usuario_id);
    
    jsonResponse([
        'success' => true,
        'message' => 'Mascota registrada correctamente',
        'id' => $id
    ]);
} else {
    jsonResponse(['success' => false, 'error' => 'Error al registrar: ' . $conn->error]);
}
?>