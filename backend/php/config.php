<?php
// Solo define constantes y funciones, no ejecuta nada

define('DB_HOST', '192.185.112.105');
define('DB_USER', 'lmcostur_macostas');
define('DB_PASS', 'r2rE6h3r9W');
define('DB_NAME', 'lmcostur_test');
define('DB_PORT', 3306);

define('FCM_URL', 'https://fcm.googleapis.com/v1/projects/maikpet-676bf/messages:send');
define('SERVICE_ACCOUNT_FILE', __DIR__ . '/maikpet-676bf-firebase-adminsdk-fbsvc-0eec82c200.json');

function getConnection() {
    static $conn = null;
    if ($conn === null) {
        $conn = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME, DB_PORT);
        if ($conn->connect_error) {
            return null;
        }
        $conn->set_charset("utf8mb4");
    }
    return $conn;
}

function isLoggedIn() {
    // Verificar header de token con user_id (app móvil)
    $authToken = $_SERVER['HTTP_X_AUTH_TOKEN'] ?? '';
    $userId = $_SERVER['HTTP_X_USER_ID'] ?? '';
    
    // Si tenemos un token y user_id válidos, considerar logueado
    if (!empty($authToken) && !empty($userId) && $authToken !== 'logout') {
        // Guardar en sesión para uso posterior
        if (session_status() === PHP_SESSION_NONE) {
            ini_set('session.cookie_httponly', 1);
            session_start();
        }
        $_SESSION['usuario_id'] = intval($userId);
        return true;
    }
    
    // Verificar sesión PHP tradicional
    if (session_status() === PHP_SESSION_NONE) {
        ini_set('session.cookie_httponly', 1);
        session_start();
    }
    return isset($_SESSION['usuario_id']) && !empty($_SESSION['usuario_id']);
}

function jsonResponse($data, $status = 200) {
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

function saveDeviceToken($userId, $token) {
    $conn = getConnection();
    if (!$conn) return false;
    $tokenEsc = $conn->real_escape_string($token);
    $sql = "INSERT INTO device_tokens (user_id, token) VALUES ($userId, '$tokenEsc') 
            ON DUPLICATE KEY UPDATE token = '$tokenEsc', updated_at = NOW()";
    return $conn->query($sql);
}

function getTokensExceptUser($excludeUserId) {
    $conn = getConnection();
    $tokens = [];
    if (!$conn) return $tokens;
    
    $checkTable = $conn->query("SHOW TABLES LIKE 'device_tokens'");
    if (!$checkTable || $checkTable->num_rows == 0) {
        return $tokens;
    }
    
    $sql = "SELECT DISTINCT token FROM device_tokens 
            WHERE user_id != $excludeUserId AND token IS NOT NULL AND token != ''";
    $result = $conn->query($sql);
    if (!$result) return $tokens;
    
    while ($row = $result->fetch_assoc()) {
        $tokens[] = $row['token'];
    }
    return $tokens;
}

function getAccessToken() {
    $serviceAccount = json_decode(file_get_contents(SERVICE_ACCOUNT_FILE), true);
    
    $now = time();
    
    $jwtHeader = base64_encode(json_encode(['typ' => 'JWT', 'alg' => 'RS256']));
    
    $jwtPayload = base64_encode(json_encode([
        'iss' => $serviceAccount['client_email'],
        'sub' => $serviceAccount['client_email'],
        'aud' => $serviceAccount['token_uri'],
        'iat' => $now,
        'exp' => $now + 3600,
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging'
    ]));
    
    $signingInput = $jwtHeader . '.' . $jwtPayload;
    
    $signature = '';
    openssl_sign($signingInput, $signature, $serviceAccount['private_key'], 'sha256');
    $jwtSignature = base64_encode($signature);
    
    $jwt = $signingInput . '.' . $jwtSignature;
    
    $ch = curl_init($serviceAccount['token_uri']);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt
    ]));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    
    $response = curl_exec($ch);
    curl_close($ch);
    
    $data = json_decode($response, true);
    
    return $data['access_token'] ?? null;
}

function sendPushNotificationV1($tokens, $title, $body, $data = []) {
    if (empty($tokens)) {
        return ['success' => false, 'message' => 'No hay tokens'];
    }
    
    $accessToken = getAccessToken();
    if (!$accessToken) {
        return ['success' => false, 'error' => 'No se pudo obtener token de acceso'];
    }
    
    $successCount = 0;
    $errors = [];
    
    foreach ($tokens as $token) {
        $message = [
            'message' => [
                'token' => $token,
                'notification' => [
                    'title' => $title,
                    'body' => $body
                ],
                'data' => $data,
                'android' => [
                    'priority' => 'high',
                    'notification' => [
                        'icon' => 'ic_launcher_foreground'
                    ]
                ]
            ]
        ];
        
        $ch = curl_init(FCM_URL);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Authorization: Bearer ' . $accessToken,
            'Content-Type: application/json'
        ]);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);
        
        $result = json_decode($response, true);
        
        if ($httpCode == 200 && isset($result['name'])) {
            $successCount++;
        } else {
            $errors[] = $result['error']['message'] ?? 'Unknown error';
        }
    }
    
    return [
        'success' => $successCount > 0,
        'sent' => $successCount,
        'total' => count($tokens),
        'errors' => $errors
    ];
}

function notifyNewMascota($mascota, $excludeUserId) {
    $tokens = getTokensExceptUser($excludeUserId);
    if (empty($tokens)) {
        return ['success' => false, 'message' => 'No hay dispositivos'];
    }
    
    $tipo = $mascota['tipo'];
    $emoji = ($tipo == 'Perro') ? '🐕' : '🐱';
    $title = "$emoji {$mascota['nombre']} busca hogar!";
    $body = "{$mascota['tipo']} - {$mascota['edad_meses']} meses - {$mascota['direccion']}";
    
    return sendPushNotificationV1($tokens, $title, $body, [
        'mascota_id' => strval($mascota['id']),
        'tipo' => $mascota['tipo'],
        'nombre' => $mascota['nombre']
    ]);
}
?>