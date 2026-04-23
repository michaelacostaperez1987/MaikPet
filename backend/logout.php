<?php
require_once 'config.php';

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Session-Id, X-Auth-Token, X-User-Id');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Limpiar sesión PHP
session_destroy();

// También limpiar cualquier dato de sesión que pueda quedar
session_start(); // Iniciar nueva sesión para limpiar
$_SESSION = array(); // Vaciar array de sesión
session_destroy(); // Destruir nuevamente

// Para autenticación por headers, devolver instrucciones para limpiar
jsonResponse([
    'success' => true, 
    'message' => 'Sesión cerrada',
    'clearHeaders' => true, // Indicar al cliente que limpie headers
    'instructions' => 'Eliminar X-Session-Id, X-Auth-Token, X-User-Id de futuras solicitudes'
]);
?>
