<?php
require_once 'config.php';

if (!isLoggedIn()) {
    header('HTTP/1.1 200 OK');
    jsonResponse(['success' => false, 'error' => 'No autorizado']);
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

$stmt = $conn->prepare("SELECT id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, fecha_registro, imagen,
                        DATEDIFF(DATE_ADD(fecha_registro, INTERVAL 30 DAY), NOW()) as dias_restantes
                        FROM mascotas 
                        WHERE usuario_id = ? AND fecha_registro >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                        ORDER BY fecha_registro DESC");
$stmt->bind_param("i", $usuario_id);
$stmt->execute();
$result = $stmt->get_result();

$mascotas = [];
while ($row = $result->fetch_assoc()) {
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

jsonResponse($mascotas);
?>
