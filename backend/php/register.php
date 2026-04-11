<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('HTTP/1.1 200 OK');
    jsonResponse(['success' => false, 'error' => 'Método no permitido']);
}

$data = json_decode(file_get_contents('php://input'), true);

$nombre = trim($data['nombre'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$telefono = trim($data['telefono'] ?? '');
$email = trim($data['email'] ?? '');
$password = $data['password'] ?? '';

// Validaciones
if (empty($nombre) || empty($direccion) || empty($telefono) || empty($email) || empty($password)) {
    jsonResponse(['success' => false, 'error' => 'Todos los campos son obligatorios']);
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    jsonResponse(['success' => false, 'error' => 'Email inválido']);
}

if (strlen($password) < 4) {
    jsonResponse(['success' => false, 'error' => 'La contraseña debe tener al menos 4 caracteres']);
}

$conn = getConnection();

// Verificar si el email ya existe
$stmt = $conn->prepare("SELECT id FROM usuarios WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    jsonResponse(['success' => false, 'error' => 'El email ya está registrado']);
}

// Hash de contraseña
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);

// Insertar usuario
$stmt = $conn->prepare("INSERT INTO usuarios (nombre, direccion, telefono, email, password) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("sssss", $nombre, $direccion, $telefono, $email, $hashedPassword);

if ($stmt->execute()) {
    jsonResponse(['success' => true, 'message' => 'Usuario registrado correctamente']);
} else {
    jsonResponse(['success' => false, 'error' => 'Error al registrar usuario']);
}
?>
