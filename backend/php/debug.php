<?php
header('Content-Type: application/json; charset=utf-8');
echo json_encode([
    'method' => $_SERVER['REQUEST_METHOD'],
    'raw_input' => file_get_contents('php://input'),
    'headers' => getallheaders(),
    'session' => isset($_SESSION) ? $_SESSION : 'no session'
]);
?>