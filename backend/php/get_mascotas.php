<?php
require_once 'config.php';

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Session-Id, X-Auth-Token, X-User-Id');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$conn = getConnection();

$sql = "SELECT m.id, m.nombre, m.tipo, m.edad_meses, m.vacunas, m.descripcion, m.direccion, m.lat, m.lng, m.fecha_registro, m.imagen,
               u.id AS dueno_id, u.nombre AS dueno_nombre, u.telefono AS dueno_telefono, u.email AS dueno_email
        FROM mascotas m 
        INNER JOIN usuarios u ON m.usuario_id = u.id 
        WHERE m.fecha_registro >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        ORDER BY m.fecha_registro DESC";

$result = $conn->query($sql);

if (!$result) {
    jsonResponse(['error' => 'Error en consulta: ' . $conn->error], 500);
}

$mascotas = [];
while ($row = $result->fetch_assoc()) {
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
        'dueno' => [
            'id' => (int)$row['dueno_id'],
            'nombre' => $row['dueno_nombre'],
            'telefono' => $row['dueno_telefono'],
            'email' => $row['dueno_email']
        ]
    ];
}

jsonResponse(['success' => true, 'mascotas' => $mascotas]);
?>
