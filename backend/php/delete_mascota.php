<?php
require_once 'config.php';

if (!isLoggedIn()) {
    header('HTTP/1.1 200 OK');
    jsonResponse(['success' => false, 'error' => 'No autorizado']);
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST' && $_SERVER['REQUEST_METHOD'] !== 'DELETE') {
    header('HTTP/1.1 200 OK');
    jsonResponse(['success' => false, 'error' => 'Método no permitido']);
}

$data = json_decode(file_get_contents('php://input'), true);
$mascota_id = intval($data['id'] ?? 0);

if ($mascota_id <= 0) {
    jsonResponse(['success' => false, 'error' => 'ID de mascota inválido']);
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

// Verificar que la mascota pertenece al usuario
$stmt = $conn->prepare("SELECT id FROM mascotas WHERE id = ? AND usuario_id = ?");
$stmt->bind_param("ii", $mascota_id, $usuario_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    jsonResponse(['success' => false, 'error' => 'Mascota no encontrada o no autorizada']);
}

// Eliminar mascota
$stmt = $conn->prepare("DELETE FROM mascotas WHERE id = ? AND usuario_id = ?");
$stmt->bind_param("ii", $mascota_id, $usuario_id);

if ($stmt->execute()) {
    jsonResponse(['success' => true, 'message' => 'Mascota eliminada']);
} else {
    jsonResponse(['success' => false, 'error' => 'Error al eliminar']);
}
?>
