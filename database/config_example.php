<?php
/**
 * Configuración de la base de datos MAIKPET
 * 
 * EDITAR ESTOS VALORES SEGÚN TU SERVIDOR
 */

// Configuración de la base de datos
define('DB_HOST', 'localhost');
define('DB_NAME', 'maikpet_db');
define('DB_USER', 'tu_usuario_mysql');
define('DB_PASS', 'tu_password_mysql');

// Nombre del sitio
define('SITE_NAME', 'MaikPet');

// URL del sitio (sin slash al final)
define('SITE_URL', 'https://lmcosturas.com/pet');

// ============================================
// FUNCIONES DE BASE DE DATOS
// ============================================

function getConnection() {
    static $conn = null;
    
    if ($conn === null) {
        $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
        
        if ($conn->connect_error) {
            jsonResponse(['error' => 'Error de conexión: ' . $conn->connect_error], 500);
        }
        
        $conn->set_charset('utf8mb4');
    }
    
    return $conn;
}

// ============================================
// FUNCIONES DE SESIÓN
// ============================================

function initSession() {
    if (session_status() === PHP_SESSION_NONE) {
        session_start();
    }
}

function isLoggedIn() {
    initSession();
    return isset($_SESSION['usuario_id']) && $_SESSION['usuario_id'] > 0;
}

function getCurrentUserId() {
    initSession();
    return $_SESSION['usuario_id'] ?? 0;
}

function loginUser($userId, $userData) {
    initSession();
    $_SESSION['usuario_id'] = $userId;
    $_SESSION['usuario_nombre'] = $userData['nombre'];
    $_SESSION['usuario_email'] = $userData['email'];
}

function logoutUser() {
    initSession();
    unset($_SESSION['usuario_id']);
    unset($_SESSION['usuario_nombre']);
    unset($_SESSION['usuario_email']);
    session_destroy();
}

// ============================================
// FUNCIONES DE RESPUESTA JSON
// ============================================

function jsonResponse($data, $statusCode = 200) {
    http_response_code($statusCode);
    header('Content-Type: application/json; charset=utf-8');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type');
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit;
}

// ============================================
// SEGURIDAD
// ============================================

function sanitize($input) {
    if (is_array($input)) {
        return array_map('sanitize', $input);
    }
    return htmlspecialchars(trim($input), ENT_QUOTES, 'UTF-8');
}

function hashPassword($password) {
    return password_hash($password, PASSWORD_DEFAULT);
}

function verifyPassword($password, $hash) {
    return password_verify($password, $hash);
}

// ============================================
// UTILIDADES
// ============================================

function generateToken($length = 32) {
    return bin2hex(random_bytes($length));
}

function formatDate($date) {
    return date('d/m/Y H:i', strtotime($date));
}

// Manejar peticiones OPTIONS para CORS
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type');
    header('Access-Control-Max-Age: 86400');
    http_response_code(204);
    exit;
}
