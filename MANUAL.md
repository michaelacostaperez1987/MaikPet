# Manual MaikPet - App de Adopción de Mascotas

## Índice
1. [Descripción General](#descripción-general)
2. [Requisitos Previos](#requisitos-previos)
3. [Estructura del Proyecto](#estructura-del-proyecto)
4. [Backend PHP](#backend-php)
5. [Base de Datos](#base-de-datos)
6. [Android App](#android-app)
7. [Configuración de APIs](#configuración-de-apis)
8. [Compilar y Ejecutar](#compilar-y-ejecutar)
9. [Subir al Servidor](#subir-al-servidor)
10. [Solución de Problemas](#solución-de-problemas)

---

## Descripción General

**MaikPet** es una aplicación Android para adopción gratuita de mascotas. Permite:
- Ver mascotas disponibles en un mapa
- Ver lista de adopciones
- Publicar mascotas para adopción
- Editar/eliminar publicaciones propias
- Login/registro de usuarios
- Contactar por WhatsApp
- Notificaciones push

**Stack tecnológico:**
- Frontend: Android (Kotlin + Jetpack Compose)
- Backend: PHP 7+
- Base de datos: MySQL 5.7+
- Maps: Google Maps API
- Imágenes: Base64 en MySQL (MEDIUMTEXT)

---

## Requisitos Previos

### Software necesario

1. **Android Studio** (Hedgehog o superior)
   - Descarga: https://developer.android.com/studio
   
2. **PHP** (para desarrollo local, opcional)
   - XAMPP o similar
   
3. **Git** (opcional pero recomendado)

### Cuentas necesarias

1. **Google Cloud Console**
   - API Key para Google Maps
   - Firebase Project (para push notifications)
   
2. **Hosting con PHP + MySQL**
   - Recomendado: Hosting con cPanel
   - Necesario: PHP 7+, MySQL 5.7+

---

## Estructura del Proyecto

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
│   │   │   └── viewmodel/MainViewModel.kt
│   │   ├── res/
│   │   │   ├── raw/ (sonidos)
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Backend PHP

### Archivos necesarios

Crea una carpeta `pet/` en tu servidor web. Todos los archivos van ahí.

#### 1. config.php (Conexión a BD)

```php
<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// ========== CONFIGURACIÓN DE TU BASE DE DATOS ==========
define('DB_HOST', '192.185.112.105');
define('DB_USER', 'lmcostur_macostas');
define('DB_PASS', 'r2rE6h3r9W');
define('DB_NAME', 'lmcostur_test');
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

#### 2. register.php (Registro de usuarios)

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

#### 3. login.php (Inicio de sesión)

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

#### 4. get_mascotas.php (Obtener todas las mascotas)

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

#### 5. get_mis_mascotas.php (Mascotas del usuario logueado)

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

#### 6. add_mascota.php (Agregar mascota)

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

#### 7. update_mascota.php (Actualizar mascota)

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

#### 8. delete_mascota.php (Eliminar mascota)

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

if ($_SERVER['REQUEST_METHOD'] !== 'DELETE') {
    echo json_encode(['success' => false, 'error' => 'Solo DELETE']);
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

#### 9. logout.php

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

#### 10. get_session.php

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

## Base de Datos

### Script SQL completo

```sql
-- Crear base de datos
CREATE DATABASE IF NOT EXISTS lmcostur_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lmcostur_test;

-- Tabla de usuarios
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

-- Tabla de mascotas
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

-- Usuario de prueba
INSERT INTO usuarios (nombre, email, password) VALUES 
('Michael', 'michael@michael.com', '$2y$10$abcdefghijklmnopqrstuvwxyz1234567890');
```

### Importante: Cambiar columna imagen

```sql
ALTER TABLE mascotas MODIFY COLUMN imagen MEDIUMTEXT DEFAULT NULL;
```

---

## Android App

### 1. Crear proyecto en Android Studio

1. File → New → New Project
2.选择 "Empty Activity"
3. Nombre: `Pet`
4. Package: `com.example.pet`
5. Minimum SDK: API 26 (Android 8.0)
6. Language: Kotlin

### 2. Configurar build.gradle.kts (project level)

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

### 3. Configurar settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pet"
include(":app")
```

### 4. Configurar app/build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    kotlin("kapt")
}

android {
    namespace = "com.example.pet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pet"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Coil for images
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}
```

### 5. AndroidManifest.xml

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
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />

    <application
        android:name=".MaikPetApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
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
            android:label="@string/app_name"
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

### 6. Modelos de datos (Models.kt)

```kotlin
package com.example.pet.data.model

import com.google.gson.annotations.SerializedName

data class Mascota(
    val id: Int,
    val nombre: String,
    val tipo: String,
    @SerializedName("edad_meses")
    val edadMeses: Int,
    val vacunas: String,
    val descripcion: String?,
    val direccion: String,
    val lat: Double?,
    val lng: Double?,
    val imagen: String?,
    val dueno: Usuario?
)

data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val telefono: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val email: String,
    val password: String
)

data class MascotaRequest(
    val id: Int? = null,
    val nombre: String,
    val tipo: String,
    @SerializedName("edad_meses")
    val edadMeses: Int,
    val vacunas: String,
    val direccion: String,
    val descripcion: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val imagen: String? = null
)

data class DeleteRequest(
    val id: Int
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null,
    val user: T? = null
)

data class SessionResponse(
    val logged_in: Boolean,
    val user: Usuario? = null
)
```

### 7. API Interface (MaikPetApi.kt)

```kotlin
package com.example.pet.data.api

import com.example.pet.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MaikPetApi {
    
    @GET("get_mascotas.php")
    suspend fun getMascotas(): Response<List<Mascota>>
    
    @GET("get_mis_mascotas.php")
    suspend fun getMisMascotas(): Response<List<Mascota>>
    
    @POST("add_mascota.php")
    suspend fun addMascota(@Body mascota: MascotaRequest): Response<ApiResponse<Mascota>>
    
    @HTTP(method = "DELETE", path = "delete_mascota.php", hasBody = true)
    suspend fun deleteMascota(@Body request: DeleteRequest): Response<ApiResponse<Nothing>>
    
    @POST("update_mascota.php")
    suspend fun updateMascota(@Body request: MascotaRequest): Response<ApiResponse<Nothing>>
    
    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<Usuario>>
    
    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Usuario>>
    
    @POST("logout.php")
    suspend fun logout(): Response<ApiResponse<Nothing>>
    
    @GET("get_session.php")
    suspend fun getSession(): Response<SessionResponse>
}
```

### 8. Repository (MaikPetRepository.kt)

```kotlin
package com.example.pet.data.repository

import com.example.pet.data.api.MaikPetApi
import com.example.pet.data.model.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

@Singleton
class MaikPetRepository @Inject constructor(
    private val api: MaikPetApi
) {
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()
    
    private val _mascotas = MutableStateFlow<List<Mascota>>(emptyList())
    val mascotas: StateFlow<List<Mascota>> = _mascotas.asStateFlow()
    
    private val _misMascotas = MutableStateFlow<List<Mascota>>(emptyList())
    val misMascotas: StateFlow<List<Mascota>> = _misMascotas.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun checkSession(): Result<Usuario?> {
        return try {
            val response = api.getSession()
            if (response.isSuccessful && response.body()?.logged_in == true) {
                val user = response.body()?.user
                _currentUser.value = user
                Result.Success(user)
            } else {
                _currentUser.value = null
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error")
        }
    }
    
    suspend fun getMascotas(): Result<List<Mascota>> {
        return try {
            _isLoading.value = true
            val response = api.getMascotas()
            if (response.isSuccessful) {
                val mascotas = response.body() ?: emptyList()
                _mascotas.value = mascotas
                Result.Success(mascotas)
            } else {
                Result.Error("Error al cargar mascotas")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getMisMascotas(): Result<List<Mascota>> {
        return try {
            val response = api.getMisMascotas()
            if (response.isSuccessful) {
                val mascotas = response.body() ?: emptyList()
                _misMascotas.value = mascotas
                Result.Success(mascotas)
            } else {
                Result.Error("Error al cargar")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error")
        }
    }
    
    suspend fun addMascota(mascota: MascotaRequest): Result<Boolean> {
        return try {
            _isLoading.value = true
            val response = api.addMascota(mascota)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    getMisMascotas()
                    getMascotas()
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al guardar")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateMascota(id: Int, request: MascotaRequest): Result<Boolean> {
        return try {
            _isLoading.value = true
            val requestWithId = MascotaRequest(
                id = id,
                nombre = request.nombre,
                tipo = request.tipo,
                edadMeses = request.edadMeses,
                vacunas = request.vacunas,
                direccion = request.direccion,
                descripcion = request.descripcion,
                lat = request.lat,
                lng = request.lng,
                imagen = request.imagen
            )
            val response = api.updateMascota(requestWithId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    getMisMascotas()
                    getMascotas()
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al actualizar")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteMascota(id: Int): Result<Boolean> {
        return try {
            _isLoading.value = true
            val response = api.deleteMascota(DeleteRequest(id))
            if (response.isSuccessful && response.body()?.success == true) {
                getMisMascotas()
                getMascotas()
                Result.Success(true)
            } else {
                Result.Error(response.body()?.error ?: "Error al eliminar")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            _isLoading.value = true
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.user != null) {
                    _currentUser.value = body.user
                    Result.Success(body.user)
                } else {
                    Result.Error(body?.error ?: "Error")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun register(nombre: String, direccion: String, telefono: String, email: String, password: String): Result<Usuario> {
        return try {
            val response = api.register(RegisterRequest(nombre, direccion, telefono, email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.user != null) {
                    _currentUser.value = body.user
                    Result.Success(body.user)
                } else {
                    Result.Error(body?.error ?: "Error")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error")
        }
    }
    
    suspend fun logout(): Result<Boolean> {
        return try {
            api.logout()
            _currentUser.value = null
            _misMascotas.value = emptyList()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Success(true)
        }
    }
}
```

### 9. Network Module (NetworkModule.kt)

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
    
    private const val BASE_URL = "https://lmcosturas.com/pet/"
    
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

### 10. Geocoding Repository

```kotlin
package com.example.pet.data.repository

import android.location.Geocoder
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class Location(val latitude: Double, val longitude: Double)

@Singleton
class GeocodingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun geocodeAddress(address: String): Location? {
        return try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(address, 1)
            if (!results.isNullOrEmpty()) {
                val location = results[0]
                Location(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
```

### 11. Application Class

```kotlin
package com.example.pet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MaikPetApplication : Application()
```

---

## Configuración de APIs

### Google Maps API Key

1. Ve a https://console.cloud.google.com/
2. Crea un proyecto nuevo
3. Habilita "Maps SDK for Android"
4. Crea una API Key
5. Agrega la key en `gradle.properties`:

```properties
MAPS_API_KEY=AIzaSyBBoKc50__YlKGaRBkP3Z7Xgek2q0bA7IA
```

### Firebase Setup

1. Ve a https://console.firebase.google.com/
2. Crea un proyecto
3. Agrega una app Android con package `com.example.pet`
4. Descarga `google-services.json`
5. Colócalo en `app/google-services.json`
6. Habilita Cloud Messaging en Firebase

---

## Compilar y Ejecutar

### 1. Configurar variables de entorno

Crear `gradle.properties`:

```properties
MAPS_API_KEY=AIzaSyBBoKc50__YlKGaRBkP3Z7Xgek2q0bA7IA
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

### 2. Build Debug APK

```bash
./gradlew assembleDebug
```

### 3. APK generado

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Subir al Servidor

### Archivos PHP a subir

Todos los archivos de la carpeta `pet/` van al servidor en la misma estructura:

```
public_html/pet/
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

### Cambiar BASE_URL en Android

En `NetworkModule.kt`, cambia:

```kotlin
private const val BASE_URL = "https://TU_DOMINIO.com/pet/"
```

---

## Solución de Problemas

### Error 500 en PHP
- Verificar conexión a base de datos en `config.php`
- Verificar que las tablas existan
- Revisar logs de error de PHP

### Imágenes no se guardan
- Verificar que la columna `imagen` sea `MEDIUMTEXT`
- Verificar permisos de la base de datos

### Mapa no funciona
- Verificar API Key de Google Maps
- Verificar que el dispositivo tenga internet

### Sesiones no funcionan
- Verificar que PHP tenga soporte para sesiones
- Verificar configuración de cookies

### Login no funciona
- Verificar que el email y password sean correctos
- Revisar que el hash de password coincida

---

## Notas Importantes

1. **Imágenes**: Se guardan como Base64 en la base de datos. Una imagen de 300x300px ocupa aproximadamente 50-100KB.

2. **Auto-eliminación**: Las mascotas se eliminan automáticamente después de 30 días.

3. **Validación de ventas**: El backend valida que la descripción no contenga palabras relacionadas con venta o cruza.

4. **Sesiones**: Usan cookies para mantener la sesión del usuario.

5. **Permisos**: La app requiere ubicación, cámara e internet.

---

## Contacto

Para soporte técnico o dudas sobre la implementación, contactar al desarrollador.
