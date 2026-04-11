<?php
require_once 'config.php';

if (isLoggedIn()) {
    jsonResponse([
        'logueado' => true,
        'usuario' => [
            'id' => $_SESSION['usuario_id'],
            'nombre' => $_SESSION['usuario_nombre'],
            'email' => $_SESSION['usuario_email']
        ]
    ]);
} else {
    jsonResponse(['logueado' => false]);
}
?>
