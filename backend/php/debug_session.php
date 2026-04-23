<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: application/json; charset=utf-8');

require_once 'config.php';

$sessionId = $_SERVER['HTTP_X_SESSION_ID'] ?? 'no header';
$cookieSession = $_COOKIE['PHPSESSID'] ?? 'no cookie';

echo json_encode([
    'session_id_from_code' => session_id(),
    'header_session_id' => $sessionId,
    'cookie_session_id' => $cookieSession,
    'session_data' => $_SESSION,
], JSON_PRETTY_PRINT);
?>