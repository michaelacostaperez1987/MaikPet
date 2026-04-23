<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// ========== CONFIGURACIÓN DE TU BASE DE DATOS ==========
define('DB_HOST', '192.185.112.105');
define('DB_USER', 'lmcostur_macostas');
define('DB_PASS', 'r2rE6h3r9W');
define('DB_NAME', 'lmcostur_test');
define('DB_PORT', 3306);

// Iniciar sesión con configuraciones seguras
if (session_status() === PHP_SESSION_NONE) {
    ini_set('session.cookie_httponly', 1);
    ini_set('session.use_only_cookies', 1);
    ini_set('session.cookie_secure', 0);
    // Si se proporciona X-Session-Id en el header, usarlo para recuperar sesión existente
    $sessionId = null;
    if (function_exists('getallheaders')) {
        $headers = getallheaders();
        if (isset($headers['X-Session-Id']) && !empty($headers['X-Session-Id'])) {
            $sessionId = $headers['X-Session-Id'];
        }
    } else {
        // Alternativa para servidores sin getallheaders()
        $headerKey = 'HTTP_X_SESSION_ID';
        if (isset($_SERVER[$headerKey]) && !empty($_SERVER[$headerKey])) {
            $sessionId = $_SERVER[$headerKey];
        }
    }
    if ($sessionId) {
        session_id($sessionId);
    }
    session_start();
    
    // Debug: log session info
    error_log('PHP Session started: id=' . session_id() . ', user_id=' . ($_SESSION['usuario_id'] ?? 'none'));
}

/**
 * Obtener conexión a la base de datos
 */
function getConnection() {
    static $conn = null;
    
    if ($conn === null) {
        $conn = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME, DB_PORT);
        
        if ($conn->connect_error) {
            error_log("Error de conexión MySQL: " . $conn->connect_error);
            http_response_code(500);
            echo json_encode(['error' => 'Error de conexión a la base de datos']);
            exit();
        }
        
        $conn->set_charset("utf8mb4");
    }
    
    return $conn;
}

/**
 * Verificar si el usuario está logueado
 */
function isLoggedIn() {
    return isset($_SESSION['usuario_id']) && !empty($_SESSION['usuario_id']);
}

/**
 * Obtener usuario actual
 */
function getCurrentUser() {
    if (!isLoggedIn()) return null;
    return [
        'id' => $_SESSION['usuario_id'],
        'nombre' => $_SESSION['usuario_nombre'],
        'email' => $_SESSION['usuario_email']
    ];
}

/**
 * Responder con JSON
 */
function jsonResponse($data, $status = 200) {
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

/**
 * Responder con error
 */
function jsonError($message, $status = 400) {
    jsonResponse(['error' => $message], $status);
}
?>
