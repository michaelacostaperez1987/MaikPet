<?php
require_once 'config.php';

header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');

echo "<pre>";

// Test 1: Conexión a base de datos
echo "=== Test 1: Conexión a base de datos ===\n";
$conn = getConnection();
if ($conn) {
    echo "✓ Conexión exitosa\n";
    
    // Test 2: Verificar tablas
    echo "\n=== Test 2: Verificar tablas ===\n";
    $tables = ['usuarios', 'mascotas', 'device_tokens'];
    foreach ($tables as $table) {
        $check = $conn->query("SHOW TABLES LIKE '$table'");
        if ($check && $check->num_rows > 0) {
            echo "✓ Tabla '$table' existe\n";
        } else {
            echo "✗ Tabla '$table' NO existe\n";
        }
    }
    
    // Test 3: Contar datos
    echo "\n=== Test 3: Contar datos ===\n";
    $sqlUsers = "SELECT COUNT(*) as total FROM usuarios";
    $resultUsers = $conn->query($sqlUsers);
    $totalUsers = $resultUsers ? $resultUsers->fetch_assoc()['total'] : 0;
    echo "Total usuarios: $totalUsers\n";
    
    $sqlMascotas = "SELECT COUNT(*) as total FROM mascotas";
    $resultMascotas = $conn->query($sqlMascotas);
    $totalMascotas = $resultMascotas ? $resultMascotas->fetch_assoc()['total'] : 0;
    echo "Total mascotas: $totalMascotas\n";
    
    // Test 4: Verificar algunas mascotas
    echo "\n=== Test 4: Verificar algunas mascotas ===\n";
    $sqlSample = "SELECT m.id, m.nombre, m.tipo, m.lat, m.lng, m.fecha_registro, u.nombre as dueno 
                  FROM mascotas m 
                  LEFT JOIN usuarios u ON m.usuario_id = u.id 
                  ORDER BY m.fecha_registro DESC 
                  LIMIT 5";
    $resultSample = $conn->query($sqlSample);
    if ($resultSample && $resultSample->num_rows > 0) {
        echo "Mascotas encontradas:\n";
        while ($row = $resultSample->fetch_assoc()) {
            echo "- {$row['nombre']} ({$row['tipo']}) - Lat: {$row['lat']}, Lng: {$row['lng']} - Dueño: {$row['dueno']}\n";
        }
    } else {
        echo "No hay mascotas en la base de datos\n";
    }
    
    // Test 5: Verificar autenticación por headers
    echo "\n=== Test 5: Autenticación por headers ===\n";
    echo "Headers recibidos:\n";
    foreach ($_SERVER as $key => $value) {
        if (strpos($key, 'HTTP_') === 0) {
            $headerName = str_replace('HTTP_', '', $key);
            $headerName = str_replace('_', '-', $headerName);
            echo "  $headerName: $value\n";
        }
    }
    
    // Test 6: Probar función isLoggedIn()
    echo "\n=== Test 6: Probar isLoggedIn() ===\n";
    $isLogged = isLoggedIn();
    echo "isLoggedIn() retorna: " . ($isLogged ? 'true' : 'false') . "\n";
    if (session_status() === PHP_SESSION_ACTIVE) {
        echo "Sesión activa, usuario_id: " . ($_SESSION['usuario_id'] ?? 'no definido') . "\n";
    }
    
} else {
    echo "✗ Error de conexión a base de datos\n";
}

echo "\n=== Test 7: Configuración PHP ===\n";
echo "PHP Version: " . phpversion() . "\n";
echo "session.save_path: " . ini_get('session.save_path') . "\n";
echo "session.cookie_httponly: " . ini_get('session.cookie_httponly') . "\n";

echo "\n=== Test 8: Headers CORS ===\n";
echo "Access-Control-Allow-Origin: *\n";
echo "Access-Control-Allow-Methods: GET, POST, OPTIONS\n";
echo "Access-Control-Allow-Headers: Content-Type, X-Session-Id, X-Auth-Token, X-User-Id\n";

echo "</pre>";
?>