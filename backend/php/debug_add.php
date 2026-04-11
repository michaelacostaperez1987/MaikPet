<?php
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

echo json_encode([
    'success' => true,
    'message' => 'Debug OK - datos recibidos',
    'data' => [
        'nombre' => $data['nombre'] ?? null,
        'tipo' => $data['tipo'] ?? null,
        'edad_meses' => $data['edad_meses'] ?? null,
        'has_image' => isset($data['imagen'])
    ]
]);
?>
