<?php
require_once 'config.php';

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Session-Id, X-Auth-Token, X-User-Id');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if (!isLoggedIn()) {
    jsonResponse(['success' => false, 'error' => 'No autorizado']);
}

$conn = getConnection();
if (!$conn) {
    jsonResponse(['error' => 'No se pudo conectar a la base de datos'], 500);
}

$usuario_id = $_SESSION['usuario_id'];
error_log("Buscando mascotas para usuario_id: $usuario_id");

$stmt = $conn->prepare("SELECT id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, fecha_registro, imagen,
                        DATEDIFF(DATE_ADD(fecha_registro, INTERVAL 30 DAY), NOW()) as dias_restantes
                        FROM mascotas 
                        WHERE usuario_id = ?
                        ORDER BY fecha_registro DESC");
if (!$stmt) {
    error_log("Error preparando consulta: " . $conn->error);
    jsonResponse(['error' => 'Error preparando consulta: ' . $conn->error], 500);
}

$stmt->bind_param("i", $usuario_id);
if (!$stmt->execute()) {
    error_log("Error ejecutando consulta: " . $stmt->error);
    jsonResponse(['error' => 'Error ejecutando consulta: ' . $stmt->error], 500);
}
$result = $stmt->get_result();

$mascotas = [];
$count = 0;
while ($row = $result->fetch_assoc()) {
    $count++;
    $diasRestantes = max(0, (int)$row['dias_restantes']);
    
    $mascotas[] = [
        'id' => (int)$row['id'],
        'nombre' => $row['nombre'],
        'tipo' => $row['tipo'],
        'edad_meses' => (int)$row['edad_meses'],
        'vacunas' => $row['vacunas'],
        'descripcion' => $row['descripcion'],
        'direccion' => $row['direccion'],
        'lat' => $row['lat'] !== null ? floatval($row['lat']) : null,
        'lng' => $row['lng'] !== null ? floatval($row['lng']) : null,
        'imagen' => $row['imagen'],
        'dias_restantes' => $diasRestantes,
        'expira' => $diasRestantes <= 7
    ];
}

error_log("Se encontraron $count mascotas para usuario $usuario_id");
jsonResponse(['success' => true, 'mascotas' => $mascotas, 'count' => $count]);
?>
