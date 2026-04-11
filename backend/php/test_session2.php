<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: text/plain; charset=utf-8');

echo "=== Session Test ===\n\n";

require_once 'config.php';

echo "Session ID: " . session_id() . "\n";
echo "Session Status: " . session_status() . "\n";
echo "SESSION contents:\n";
print_r($_SESSION);

// Simular login para test
if (!isset($_SESSION['usuario_id'])) {
    echo "\nSetting test session...\n";
    $_SESSION['usuario_id'] = 1;
    $_SESSION['usuario_nombre'] = 'Test User';
    echo "Session set. ID: " . $_SESSION['usuario_id'] . "\n";
    echo "Now reload this page to test persistence.\n";
} else {
    echo "\nisLoggedIn(): " . (isLoggedIn() ? 'true' : 'false') . "\n";
}
?>
