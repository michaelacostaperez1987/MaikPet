<?php
require_once 'config.php';

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Session-Id, X-Auth-Token, X-User-Id');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$conn = getConnection();

if (!$conn) {
    jsonResponse(['error' => 'No se pudo conectar a la base de datos'], 500);
}

// Verificar si hay usuarios
$sqlUsers = "SELECT COUNT(*) as total FROM usuarios";
$resultUsers = $conn->query($sqlUsers);
$totalUsers = $resultUsers ? $resultUsers->fetch_assoc()['total'] : 0;

// Si no hay usuarios, crear uno de prueba
if ($totalUsers == 0) {
    $sqlInsertUser = "INSERT INTO usuarios (nombre, direccion, telefono, email, password, edad, fecha_registro) 
                      VALUES ('Usuario Prueba', 'Montevideo, Uruguay', '099123456', 'test@test.com', '123456', 30, NOW())";
    if ($conn->query($sqlInsertUser)) {
        $userId = $conn->insert_id;
        jsonResponse(['success' => true, 'message' => 'Usuario de prueba creado', 'user_id' => $userId]);
    } else {
        jsonResponse(['error' => 'Error al crear usuario: ' . $conn->error], 500);
    }
} else {
    // Verificar si hay mascotas
    $sqlMascotas = "SELECT COUNT(*) as total FROM mascotas";
    $resultMascotas = $conn->query($sqlMascotas);
    $totalMascotas = $resultMascotas ? $resultMascotas->fetch_assoc()['total'] : 0;
    
    if ($totalMascotas == 0) {
        // Obtener un usuario para asociar la mascota
        $sqlGetUser = "SELECT id FROM usuarios LIMIT 1";
        $resultUser = $conn->query($sqlGetUser);
        if ($resultUser && $row = $resultUser->fetch_assoc()) {
            $userId = $row['id'];
            
            // Insertar mascotas de prueba
            $mascotas = [
                ['nombre' => 'Firulais', 'tipo' => 'Perro', 'edad_meses' => 24, 'vacunas' => 'Si', 
                 'direccion' => 'Montevideo Centro', 'lat' => -34.9011, 'lng' => -56.1645,
                 'descripcion' => 'Perro juguetón y cariñoso'],
                ['nombre' => 'Michi', 'tipo' => 'Gato', 'edad_meses' => 12, 'vacunas' => 'Si',
                 'direccion' => 'Punta Carretas', 'lat' => -34.9236, 'lng' => -56.1597,
                 'descripcion' => 'Gato tranquilo y limpio'],
                ['nombre' => 'Luna', 'tipo' => 'Perro', 'edad_meses' => 18, 'vacunas' => 'No',
                 'direccion' => 'Carrasco', 'lat' => -34.8861, 'lng' => -56.0544,
                 'descripcion' => 'Perrita mediana, necesita hogar']
            ];
            
            $inserted = 0;
            foreach ($mascotas as $mascota) {
                $sql = "INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, direccion, lat, lng, descripcion, fecha_registro)
                        VALUES ($userId, '{$mascota['nombre']}', '{$mascota['tipo']}', {$mascota['edad_meses']}, '{$mascota['vacunas']}', 
                                '{$mascota['direccion']}', {$mascota['lat']}, {$mascota['lng']}, '{$mascota['descripcion']}', NOW())";
                if ($conn->query($sql)) {
                    $inserted++;
                }
            }
            
            jsonResponse(['success' => true, 'message' => "Se insertaron $inserted mascotas de prueba"]);
        } else {
            jsonResponse(['error' => 'No se encontró usuario para asociar mascotas'], 500);
        }
    } else {
        jsonResponse(['success' => true, 'message' => 'Ya hay datos en la base de datos', 'total_usuarios' => $totalUsers, 'total_mascotas' => $totalMascotas]);
    }
}
?>