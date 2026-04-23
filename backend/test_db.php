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

if (!$conn) {
    jsonResponse(['error' => 'No se pudo conectar a la base de datos'], 500);
}

// Verificar si la tabla mascotas existe
$checkTable = $conn->query("SHOW TABLES LIKE 'mascotas'");
if (!$checkTable || $checkTable->num_rows == 0) {
    jsonResponse(['error' => 'La tabla mascotas no existe'], 500);
}

// Contar total de mascotas
$sqlTotal = "SELECT COUNT(*) as total FROM mascotas";
$resultTotal = $conn->query($sqlTotal);
$totalMascotas = $resultTotal ? $resultTotal->fetch_assoc()['total'] : 0;

// Contar mascotas de los últimos 30 días
$sql30Days = "SELECT COUNT(*) as total FROM mascotas WHERE fecha_registro >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
$result30Days = $conn->query($sql30Days);
$total30Days = $result30Days ? $result30Days->fetch_assoc()['total'] : 0;

// Mostrar algunas mascotas
$sqlSample = "SELECT m.id, m.nombre, m.tipo, m.fecha_registro, u.nombre as dueno_nombre 
              FROM mascotas m 
              LEFT JOIN usuarios u ON m.usuario_id = u.id 
              ORDER BY m.fecha_registro DESC 
              LIMIT 5";
$resultSample = $conn->query($sqlSample);
$sampleMascotas = [];
if ($resultSample) {
    while ($row = $resultSample->fetch_assoc()) {
        $sampleMascotas[] = $row;
    }
}

jsonResponse([
    'success' => true,
    'database_status' => 'connected',
    'total_mascotas' => $totalMascotas,
    'mascotas_ultimos_30_dias' => $total30Days,
    'sample_mascotas' => $sampleMascotas
]);
?>