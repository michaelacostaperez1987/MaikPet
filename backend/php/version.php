<?php
require_once 'config.php';

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

header('Content-Type: application/json; charset=utf-8');
echo json_encode([
    'session_status' => session_status(),
    'session_id' => session_id(),
    'session' => $_SESSION
]);
?>