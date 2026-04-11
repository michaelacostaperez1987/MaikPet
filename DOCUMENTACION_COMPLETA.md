# MANUAL COMPLETO DE MAIKPET - App de Adopción de Mascotas

## ÍNDICE
1. [Descripción del Proyecto](#1-descripción-del-proyecto)
2. [Requisitos del Sistema](#2-requisitos-del-sistema)
3. [Estructura del Proyecto](#3-estructura-del-proyecto)
4. [Base de Datos](#4-base-de-datos)
5. [Backend PHP](#5-backend-php)
6. [Android App](#6-android-app)
7. [Configuración de APIs](#7-configuración-de-apis)
8. [Compilación y Ejecución](#8-compilación-y-ejecución)
9. [Publicación en el Servidor](#9-publicación-en-el-servidor)
10. [Solución de Problemas](#10-solución-de-problemas)

---

## 1. DESCRIPCIÓN DEL PROYECTO

**MaikPet** es una aplicación móvil Android para adopción gratuita de mascotas.

### Funcionalidades Principales:
- 🗺️ Mapa interactivo con mascotas cercanas
- 📋 Lista de adopciones con fotos
- ➕ Publicar mascotas para adopción
- ✏️ Editar y eliminar publicaciones propias
- 👤 Login y registro de usuarios
- 💬 Contactar por WhatsApp
- 🔔 Notificaciones push (Firebase)
- 📍 Autocompletado de direcciones
- 🚫 Validación contra ventas y cruzas
- 🏠 Pantalla de inicio informativa

### Tecnologías Utilizadas:
| Componente | Tecnología |
|------------|------------|
| Frontend | Android (Kotlin + Jetpack Compose) |
| Backend | PHP 7+ |
| Base de Datos | MySQL 5.7+ |
| Maps | Google Maps API |
| Imágenes | Base64 en MySQL (MEDIUMTEXT) |
| Push | Firebase Cloud Messaging |
| DI | Hilt |
| Networking | Retrofit + OkHttp |

---

## 2. REQUISITOS DEL SISTEMA

### Software Necesario:
1. **Android Studio** (Hedgehog o superior)
   - Descarga: https://developer.android.com/studio
   
2. **XAMPP** o servidor con PHP (para desarrollo local)
   - Descarga: https://www.apachefriends.org/

3. **Git** (opcional)

### Cuentas Requeridas:
1. **Google Cloud Console** (para Maps)
   - https://console.cloud.google.com/
   
2. **Firebase Console** (para Push)
   - https://console.firebase.google.com/

3. **Hosting con PHP + MySQL**
   - Recomendado: Hostinger, cPanel, etc.

---

## 3. ESTRUCTURA DEL PROYECTO

### Estructura General:
```
Pet2/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/pet/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MaikPetApplication.kt
│   │   │   ├── data/
│   │   │   │   ├── api/MaikPetApi.kt
│   │   │   │   ├── model/Models.kt
│   │   │   │   └── repository/
│   │   │   │       ├── MaikPetRepository.kt
│   │   │   │       └── GeocodingRepository.kt
│   │   │   ├── di/NetworkModule.kt
│   │   │   ├── firebase/MaikPetFirebaseService.kt
│   │   │   ├── ui/
│   │   │   │   ├── components/Components.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── MapaScreen.kt
│   │   │   │   │   ├── AdopcionScreen.kt
│   │   │   │   │   ├── DarAdopcionScreen.kt
│   │   │   │   │   ├── MisMascotasScreen.kt
│   │   │   │   │   ├── EditarMascotaScreen.kt
│   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   └── AcercaDeScreen.kt
│   │   │   │   └── theme/
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Color.kt
│   │   │   ├── util/SoundPlayer.kt
│   │   │   ├── worker/CheckNewMascotasWorker.kt
│   │   │   └── viewmodel/MainViewModel.kt
│   │   ├── res/
│   │   │   ├── raw/ (sonidos bark.mp3, meow.mp3)
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
├── gradle.properties
└── google-services.json
```

### Backend PHP:
```
pet/
├── config.php
├── register.php
├── login.php
├── logout.php
├── get_session.php
├── get_mascotas.php
├── get_mis_mascotas.php
├── add_mascota.php
├── update_mascota.php
└── delete_mascota.php
```

---

## 4. BASE DE DATOS

### Script SQL Completo:

```sql
-- =====================================================
-- MAIKPET - SCRIPT DE BASE DE DATOS
-- =====================================================

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS maikpet_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE maikpet_db;

-- =====================================================
-- TABLA: usuarios
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) DEFAULT '',
    telefono VARCHAR(20) DEFAULT '',
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fcm_token VARCHAR(255) DEFAULT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: mascotas
-- =====================================================
CREATE TABLE IF NOT EXISTS mascotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('Perro', 'Gato') NOT NULL,
    edad_meses INT NOT NULL,
    vacunas ENUM('Si', 'No') NOT NULL DEFAULT 'No',
    descripcion TEXT,
    direccion VARCHAR(255) NOT NULL,
    lat DOUBLE DEFAULT NULL,
    lng DOUBLE DEFAULT NULL,
    imagen MEDIUMTEXT DEFAULT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario (usuario_id),
    INDEX idx_fecha (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- IMPORTANTE: Cambiar columna imagen a MEDIUMTEXT
-- (Las imágenes base64 son largas, VARCHAR no es suficiente)
-- =====================================================
ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL;

-- =====================================================
-- USUARIO DE PRUEBA
-- =====================================================
INSERT INTO usuarios (nombre, email, password, telefono) VALUES 
('Michael', 'michael@michael.com', '$2y$10$YourHashedPasswordHere', '+59899123456');
```

### Cambiar Configuración de la Base de Datos:

Edita `config.php` en el backend con tus datos:
```php
define('DB_HOST', 'localhost');      // Host de la base de datos
define('DB_USER', 'tu_usuario');     // Usuario de MySQL
define('DB_PASS', 'tu_password');    // Password de MySQL
define('DB_NAME', 'maikpet_db');     // Nombre de la base de datos
define('DB_PORT', 3306);             // Puerto de MySQL
```

---

## 5. BACKEND PHP

### 5.1 config.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// =====================================================
// CONFIGURACIÓN - CAMBIAR ESTOS VALORES
// =====================================================
define('DB_HOST', 'localhost');
define('DB_USER', 'tu_usuario');
define('DB_PASS', 'tu_password');
define('DB_NAME', 'maikpet_db');
define('DB_PORT', 3306);

// Iniciar sesión
if (session_status() === PHP_SESSION_NONE) {
    ini_set('session.cookie_httponly', 1);
    ini_set('session.use_only_cookies', 1);
    ini_set('session.cookie_secure', 0);
    session_start();
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
 * Responder con JSON
 */
function jsonResponse($data, $status = 200) {
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}
?>
```

### 5.2 register.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Método no permitido']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

$nombre = trim($data['nombre'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$telefono = trim($data['telefono'] ?? '');
$email = trim($data['email'] ?? '');
$password = $data['password'] ?? '';

if (empty($nombre) || empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'error' => 'Todos los campos son obligatorios']);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'error' => 'Email inválido']);
    exit;
}

$conn = getConnection();

// Verificar si el email ya existe
$check = $conn->query("SELECT id FROM usuarios WHERE email = '$email'");
if ($check->num_rows > 0) {
    echo json_encode(['success' => false, 'error' => 'El email ya está registrado']);
    exit;
}

$passwordHash = password_hash($password, PASSWORD_DEFAULT);

$sql = "INSERT INTO usuarios (nombre, direccion, telefono, email, password) VALUES ('$nombre', '$direccion', '$telefono', '$email', '$passwordHash')";

if ($conn->query($sql)) {
    $_SESSION['usuario_id'] = $conn->insert_id;
    $_SESSION['usuario_nombre'] = $nombre;
    $_SESSION['usuario_email'] = $email;
    
    echo json_encode([
        'success' => true,
        'message' => 'Registro exitoso',
        'user' => [
            'id' => $conn->insert_id,
            'nombre' => $nombre,
            'email' => $email
        ]
    ]);
} else {
    echo json_encode(['success' => false, 'error' => 'Error al registrar']);
}
?>
```

### 5.3 login.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Método no permitido']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

$email = trim($data['email'] ?? '');
$password = $data['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'error' => 'Email y password son obligatorios']);
    exit;
}

$conn = getConnection();
$result = $conn->query("SELECT * FROM usuarios WHERE email = '$email'");

if ($result->num_rows === 0) {
    echo json_encode(['success' => false, 'error' => 'Credenciales inválidas']);
    exit;
}

$user = $result->fetch_assoc();

if (!password_verify($password, $user['password'])) {
    echo json_encode(['success' => false, 'error' => 'Credenciales inválidas']);
    exit;
}

$_SESSION['usuario_id'] = $user['id'];
$_SESSION['usuario_nombre'] = $user['nombre'];
$_SESSION['usuario_email'] = $user['email'];

echo json_encode([
    'success' => true,
    'message' => 'Login exitoso',
    'user' => [
        'id' => $user['id'],
        'nombre' => $user['nombre'],
        'email' => $user['email']
    ]
]);
?>
```

### 5.4 get_mascotas.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

// Auto-eliminar mascotas de más de 30 días
$conn = getConnection();
$conn->query("DELETE FROM mascotas WHERE fecha_creacion < DATE_SUB(NOW(), INTERVAL 30 DAY)");

$result = $conn->query("
    SELECT m.*, u.nombre as dueno_nombre, u.telefono as dueno_telefono, u.email as dueno_email
    FROM mascotas m
    LEFT JOIN usuarios u ON m.usuario_id = u.id
    ORDER BY m.fecha_creacion DESC
");

$mascotas = [];
while ($row = $result->fetch_assoc()) {
    $mascotas[] = [
        'id' => (int)$row['id'],
        'nombre' => $row['nombre'],
        'tipo' => $row['tipo'],
        'edad_meses' => (int)$row['edad_meses'],
        'vacunas' => $row['vacunas'],
        'descripcion' => $row['descripcion'],
        'direccion' => $row['direccion'],
        'lat' => $row['lat'] ? (float)$row['lat'] : null,
        'lng' => $row['lng'] ? (float)$row['lng'] : null,
        'imagen' => $row['imagen'],
        'dueno' => [
            'nombre' => $row['dueno_nombre'],
            'telefono' => $row['dueno_telefono'],
            'email' => $row['dueno_email']
        ]
    ];
}

echo json_encode($mascotas);
?>
```

### 5.5 get_mis_mascotas.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No autorizado']);
    exit;
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

$result = $conn->query("
    SELECT m.*, u.nombre as dueno_nombre, u.telefono as dueno_telefono, u.email as dueno_email
    FROM mascotas m
    LEFT JOIN usuarios u ON m.usuario_id = u.id
    WHERE m.usuario_id = $usuario_id
    ORDER BY m.fecha_creacion DESC
");

$mascotas = [];
while ($row = $result->fetch_assoc()) {
    $mascotas[] = [
        'id' => (int)$row['id'],
        'nombre' => $row['nombre'],
        'tipo' => $row['tipo'],
        'edad_meses' => (int)$row['edad_meses'],
        'vacunas' => $row['vacunas'],
        'descripcion' => $row['descripcion'],
        'direccion' => $row['direccion'],
        'lat' => $row['lat'] ? (float)$row['lat'] : null,
        'lng' => $row['lng'] ? (float)$row['lng'] : null,
        'imagen' => $row['imagen'],
        'dueno' => [
            'nombre' => $row['dueno_nombre'],
            'telefono' => $row['dueno_telefono'],
            'email' => $row['dueno_email']
        ]
    ];
}

echo json_encode($mascotas);
?>
```

### 5.6 add_mascota.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No autorizado']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Método no permitido']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

$nombre = trim($data['nombre'] ?? '');
$tipo = trim($data['tipo'] ?? '');
$edad_meses = intval($data['edad_meses'] ?? 0);
$vacunas = trim($data['vacunas'] ?? '');
$descripcion = trim($data['descripcion'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$lat = isset($data['lat']) && $data['lat'] !== null ? floatval($data['lat']) : null;
$lng = isset($data['lng']) && $data['lng'] !== null ? floatval($data['lng']) : null;
$imagen = $data['imagen'] ?? null;

if (empty($nombre) || empty($tipo) || $edad_meses <= 0 || empty($vacunas) || empty($direccion)) {
    echo json_encode(['success' => false, 'error' => 'Todos los campos son obligatorios']);
    exit;
}

// Validar que no sea venta o reproduccion
$palabrasProhibidas = [
    'venta', 'vendo', 'precio', 'dolares', 'ufs', 'u$s', 
    'comprar', 'compro', 'costo', 'valor', 'permuta', 
    'cambio', 'canje', 'cruza', 'cruzar', 'aparear', 
    'monta', 'reproducir', 'cria', 'criar', 'pedigri', 'acoplar', 'cubrir'
];
$descripcionLower = mb_strtolower($descripcion);

foreach ($palabrasProhibidas as $palabra) {
    if (strpos($descripcionLower, $palabra) !== false) {
        echo json_encode(['success' => false, 'error' => 'No se permiten ventas, cruzas ni permutas. Solo adopciones gratuitas.']);
        exit;
    }
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

$nombre_esc = $conn->real_escape_string($nombre);
$tipo_esc = $conn->real_escape_string($tipo);
$vacunas_esc = $conn->real_escape_string($vacunas);
$descripcion_esc = $conn->real_escape_string($descripcion);
$direccion_esc = $conn->real_escape_string($direccion);
$lat_val = $lat ?? 'NULL';
$lng_val = $lng ?? 'NULL';
$imagen_val = $imagen ? "'" . $conn->real_escape_string($imagen) . "'" : 'NULL';

$sql = "INSERT INTO mascotas (usuario_id, nombre, tipo, edad_meses, vacunas, descripcion, direccion, lat, lng, imagen) VALUES ($usuario_id, '$nombre_esc', '$tipo_esc', $edad_meses, '$vacunas_esc', '$descripcion_esc', '$direccion_esc', $lat_val, $lng_val, $imagen_val)";

$result = $conn->query($sql);

if ($result) {
    $id = $conn->insert_id;
    echo json_encode([
        'success' => true,
        'message' => 'Mascota registrada correctamente',
        'id' => $id
    ]);
} else {
    echo json_encode(['success' => false, 'error' => 'Error al registrar: ' . $conn->error]);
}
?>
```

### 5.7 update_mascota.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No autorizado']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Solo POST']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);

$id = intval($data['id'] ?? 0);
$nombre = trim($data['nombre'] ?? '');
$tipo = trim($data['tipo'] ?? '');
$edad_meses = intval($data['edad_meses'] ?? 0);
$vacunas = trim($data['vacunas'] ?? '');
$descripcion = trim($data['descripcion'] ?? '');
$direccion = trim($data['direccion'] ?? '');
$lat = isset($data['lat']) && $data['lat'] !== null ? floatval($data['lat']) : null;
$lng = isset($data['lng']) && $data['lng'] !== null ? floatval($data['lng']) : null;
$imagen = $data['imagen'] ?? null;

if ($id <= 0) {
    echo json_encode(['success' => false, 'error' => 'ID inválido']);
    exit;
}

if (empty($nombre) || empty($tipo) || $edad_meses <= 0 || empty($vacunas)) {
    echo json_encode(['success' => false, 'error' => 'Campos requeridos']);
    exit;
}

// Validar que no sea venta o reproduccion
$palabrasProhibidas = [
    'venta', 'vendo', 'precio', 'dolares', 'ufs', 'u$s', 
    'comprar', 'compro', 'costo', 'valor', 'permuta', 
    'cambio', 'canje', 'cruza', 'cruzar', 'aparear', 
    'monta', 'reproducir', 'cria', 'criar', 'pedigri', 'acoplar', 'cubrir'
];
$descripcionLower = mb_strtolower($descripcion);

foreach ($palabrasProhibidas as $palabra) {
    if (strpos($descripcionLower, $palabra) !== false) {
        echo json_encode(['success' => false, 'error' => 'No se permiten ventas, cruzas ni permutas. Solo adopciones gratuitas.']);
        exit;
    }
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

// Verificar propiedad
$check = $conn->query("SELECT id FROM mascotas WHERE id = $id AND usuario_id = $usuario_id");
if ($check->num_rows === 0) {
    echo json_encode(['success' => false, 'error' => 'No autorizado']);
    exit;
}

$descripcion_esc = $conn->real_escape_string($descripcion);
$direccion_esc = $conn->real_escape_string($direccion);
$lat_val = $lat ?? 'NULL';
$lng_val = $lng ?? 'NULL';
$imagen_val = $imagen ? "'" . $conn->real_escape_string($imagen) . "'" : 'NULL';

$sql = "UPDATE mascotas SET nombre='$nombre', tipo='$tipo', edad_meses=$edad_meses, vacunas='$vacunas', descripcion='$descripcion_esc', direccion='$direccion_esc', lat=$lat_val, lng=$lng_val, imagen=$imagen_val WHERE id=$id AND usuario_id=$usuario_id";

$result = $conn->query($sql);

if ($result) {
    echo json_encode([
        'success' => true,
        'message' => 'Mascota actualizada correctamente'
    ]);
} else {
    echo json_encode(['success' => false, 'error' => 'Error SQL: ' . $conn->error]);
}
?>
```

### 5.8 delete_mascota.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if (!isLoggedIn()) {
    echo json_encode(['success' => false, 'error' => 'No autorizado']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'DELETE' && $_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Método no permitido']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);
$id = intval($data['id'] ?? 0);

if ($id <= 0) {
    echo json_encode(['success' => false, 'error' => 'ID inválido']);
    exit;
}

$conn = getConnection();
$usuario_id = $_SESSION['usuario_id'];

$result = $conn->query("DELETE FROM mascotas WHERE id = $id AND usuario_id = $usuario_id");

if ($result) {
    echo json_encode(['success' => true, 'message' => 'Mascota eliminada']);
} else {
    echo json_encode(['success' => false, 'error' => 'Error al eliminar']);
}
?>
```

### 5.9 logout.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

session_destroy();
echo json_encode(['success' => true, 'message' => 'Sesión cerrada']);
?>
```

### 5.10 get_session.php
```php
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

if (isLoggedIn()) {
    echo json_encode([
        'logged_in' => true,
        'user' => [
            'id' => $_SESSION['usuario_id'],
            'nombre' => $_SESSION['usuario_nombre'],
            'email' => $_SESSION['usuario_email']
        ]
    ]);
} else {
    echo json_encode(['logged_in' => false]);
}
?>
```

---

## 6. ANDROID APP

### 6.1 build.gradle (nivel proyecto)
```groovy
// build.gradle
plugins {
    id 'com.android.application' version '8.2.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.20' apply false
    id 'com.google.dagger.hilt.android' version '2.48' apply false
    id 'com.google.gms.google-services' version '4.4.0' apply false
    id 'com.google.devtools.ksp' version '1.9.20-1.0.14' apply false
}
```

### 6.2 build.gradle (nivel app)
```groovy
// app/build.gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
    id 'com.google.gms.google-services'
}

android {
    namespace 'com.example.pet'
    compileSdk 35

    defaultConfig {
        applicationId "com.example.pet"
        minSdk 24
        targetSdk 35
        versionCode 1
        versionName "1.0"
        
        manifestPlaceholders = [MAPS_API_KEY: "TU_API_KEY_DE_GOOGLE_MAPS"]
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_21
        targetCompatibility JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = '21'
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion '1.5.14'
    }
}

dependencies {
    // Core
    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.4'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4'
    implementation 'androidx.activity:activity-compose:1.9.1'
    implementation platform('androidx.compose:compose-bom:2024.06.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.material:material-icons-extended'
    implementation 'androidx.navigation:navigation-compose:2.7.7'
    
    // Hilt
    implementation "com.google.dagger:hilt-android:2.48"
    ksp "com.google.dagger:hilt-compiler:2.48"
    implementation 'androidx.hilt:hilt-navigation-compose:1.2.0'
    
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Google Maps
    implementation 'com.google.maps.android:maps-compose:6.2.1'
    implementation 'com.google.android.gms:play-services-maps:19.0.0'
    implementation 'com.google.android.gms:play-services-location:21.3.0'
    
    // Coil
    implementation 'io.coil-kt:coil-compose:2.6.0'
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:33.1.0')
    implementation 'com.google.firebase:firebase-messaging-ktx'
    
    // WorkManager
    implementation 'androidx.work:work-runtime-ktx:2.9.0'
}

ksp {
    arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
}
```

### 6.3 gradle.properties
```properties
MAPS_API_KEY=AIzaSyBBoKc50__YlKGaRBkP3Z7Xgek2q0bA7IA
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

### 6.4 AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <application
        android:name=".MaikPetApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Pet"
        android:usesCleartextTraffic="true">
        
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="${MAPS_API_KEY}" />
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Pet">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".firebase.MaikPetFirebaseService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

### 6.5 NetworkModule.kt
```kotlin
package com.example.pet.di

import com.example.pet.data.api.MaikPetApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // CAMBIAR ESTA URL POR LA DE TU SERVIDOR
    private const val BASE_URL = "https://tu-dominio.com/pet/"
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val cookieJar = object : CookieJar {
            private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
            
            override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies.toMutableList()
            }
            
            override fun loadForRequest(url: okhttp3.HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        }
        
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMaikPetApi(retrofit: Retrofit): MaikPetApi {
        return retrofit.create(MaikPetApi::class.java)
    }
}
```

---

## 7. CONFIGURACIÓN DE APIS

### 7.1 Google Maps API

1. Ir a https://console.cloud.google.com/
2. Crear un proyecto nuevo
3. Ir a "APIs y servicios" → "Biblioteca"
4. Buscar y habilitar "Maps SDK for Android"
5. Ir a "APIs y servicios" → "Credenciales"
6. Crear una API Key
7. Copiar la API Key

### 7.2 Firebase Setup

1. Ir a https://console.firebase.google.com/
2. Crear un proyecto nuevo
3. Agregar una app Android:
   - Package name: `com.example.pet`
   - SHA-1 (opcional)
4. Descargar `google-services.json`
5. Colocar el archivo en `app/google-services.json`
6. Habilitar Cloud Messaging en Firebase

### 7.3 Configurar API Keys

En `gradle.properties`:
```properties
MAPS_API_KEY=TU_API_KEY_DE_GOOGLE_MAPS
```

En `app/build.gradle`:
```groovy
manifestPlaceholders = [MAPS_API_KEY: "TU_API_KEY"]
```

En `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

---

## 8. COMPILACIÓN Y EJECUCIÓN

### 8.1 Pasos para Compilar

1. Abrir el proyecto en Android Studio
2. Esperar a que se sincronicen las dependencias
3. Conectar un dispositivo Android o iniciar un emulador
4. Hacer clic en "Run" o usar:
   ```bash
   ./gradlew assembleDebug
   ```

### 8.2 Generar APK de Release

1. Crear un keystore (solo una vez):
   ```bash
   keytool -genkey -v -keystore your-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias your-alias
   ```

2. Configurar signing en `app/build.gradle`:
   ```groovy
   signingConfigs {
       release {
           storeFile file('your-keystore.jks')
           storePassword 'tu-password'
           keyAlias 'tu-alias'
           keyPassword 'tu-key-password'
       }
   }
   
   buildTypes {
       release {
           signingConfig signingConfigs.release
           minifyEnabled true
           proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
       }
   }
   ```

3. Compilar:
   ```bash
   ./gradlew assembleRelease
   ```

---

## 9. PUBLICACIÓN EN EL SERVIDOR

### 9.1 Subir archivos PHP

1. Conectar al servidor por FTP o File Manager del cPanel
2. Crear una carpeta `pet/` en `public_html/`
3. Subir todos los archivos PHP a esa carpeta

### 9.2 Archivos a subir

```
public_html/pet/
├── config.php        (con tus datos de BD)
├── register.php
├── login.php
├── logout.php
├── get_session.php
├── get_mascotas.php
├── get_mis_mascotas.php
├── add_mascota.php
├── update_mascota.php
└── delete_mascota.php
```

### 9.3 Cambiar URL en Android

En `NetworkModule.kt`, cambiar:
```kotlin
private const val BASE_URL = "https://tu-dominio.com/pet/"
```

### 9.4 Verificar funcionamiento

1. Abrir en el navegador: `https://tu-dominio.com/pet/get_session.php`
2. Debe devolver: `{"logged_in":false}`

---

## 10. SOLUCIÓN DE PROBLEMAS

### Error 500 en PHP
- Verificar que `config.php` tenga los datos correctos de la base de datos
- Verificar que las tablas existan
- Revisar los logs de error de PHP en el servidor

### Imágenes no se guardan
- Verificar que la columna `imagen` sea `MEDIUMTEXT`
- Ejecutar: `ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL;`

### Mapa no funciona
- Verificar que la API Key de Google Maps sea correcta
- Verificar que el dispositivo tenga internet
- Verificar que "Maps SDK for Android" esté habilitado en Google Cloud Console

### Sesiones no funcionan
- Verificar que PHP tenga soporte para sesiones
- Verificar que las cookies estén habilitadas

### Login no funciona
- Verificar que el email y password sean correctos
- Verificar que el hash de password sea compatible

### App no conecta al backend
- Verificar la URL en `NetworkModule.kt`
- Verificar que el servidor tenga SSL (https)
- Verificar `android:usesCleartextTraffic="true"` en el manifest (solo para desarrollo)

---

## NOTAS IMPORTANTES

1. **Imágenes**: Se guardan como Base64 en la base de datos. Una imagen de 300x300px ocupa aproximadamente 50-100KB.

2. **Auto-eliminación**: Las mascotas se eliminan automáticamente después de 30 días.

3. **Validación de ventas**: El backend valida que la descripción no contenga palabras relacionadas con venta o cruza.

4. **Sesiones**: Usan cookies para mantener la sesión del usuario.

5. **Permisos**: La app requiere ubicación, cámara e internet.

---

## HISTORIAL DE VERSIONES

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Abril 2026 | Versión inicial completa |

---

**Documento creado: Abril 2026**
**Autor: Desarrollo MaikPet**
