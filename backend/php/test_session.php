<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No autorizado', 'session' => $_SESSION]);
    exit;
}

echo json_encode([
    'success' => true, 
    'message' => 'Logueado correctamente',
    'usuario_id' => $_SESSION['usuario_id'] ?? null
]);
?>
