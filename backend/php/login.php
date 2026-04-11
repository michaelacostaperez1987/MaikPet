<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('HTTP/1.1 200 OK');
    jsonResponse(['success' => false, 'error' => 'Método no permitido']);
}

$data = json_decode(file_get_contents('php://input'), true);

$email = trim($data['email'] ?? '');
$password = $data['password'] ?? '';

if (empty($email) || empty($password)) {
    jsonResponse(['success' => false, 'error' => 'Email y contraseña son obligatorios']);
}

$conn = getConnection();

$stmt = $conn->prepare("SELECT id, nombre, email, telefono, direccion, password FROM usuarios WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    jsonResponse(['success' => false, 'error' => 'Email o contraseña incorrectos']);
}

$usuario = $result->fetch_assoc();

if (!password_verify($password, $usuario['password'])) {
    jsonResponse(['success' => false, 'error' => 'Email o contraseña incorrectos']);
}

// Guardar sesión
$_SESSION['usuario_id'] = $usuario['id'];
$_SESSION['usuario_nombre'] = $usuario['nombre'];
$_SESSION['usuario_email'] = $usuario['email'];

jsonResponse([
    'success' => true,
    'usuario' => [
        'id' => $usuario['id'],
        'nombre' => $usuario['nombre'],
        'email' => $usuario['email'],
        'telefono' => $usuario['telefono'],
        'direccion' => $usuario['direccion']
    ]
]);
?>
