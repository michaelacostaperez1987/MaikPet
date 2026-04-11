<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

require_once 'config.php';

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No logueado', 'session_id' => session_id()]);
    exit;
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

echo json_encode([
    'success' => true,
    'message' => 'Logueado OK',
    'usuario_id' => $usuario_id,
    'session_id' => session_id()
]);
?>
