# LISTA DE ARCHIVOS - MAIKPET

## DOCUMENTACIÓN
- [x] `DOCUMENTACION_COMPLETA.md` - Manual completo de desarrollo
- [x] `GUIA_RAPIDA.md` - Guía rápida de configuración

## BACKEND PHP
- [x] `backend/php/config.php` - Configuración de base de datos y sesiones
- [x] `backend/php/register.php` - Registro de usuarios
- [x] `backend/php/login.php` - Inicio de sesión
- [x] `backend/php/logout.php` - Cerrar sesión
- [x] `backend/php/get_session.php` - Verificar sesión activa
- [x] `backend/php/get_mascotas.php` - Obtener todas las mascotas
- [x] `backend/php/get_mis_mascotas.php` - Obtener mascotas del usuario
- [x] `backend/php/add_mascota.php` - Agregar nueva mascota
- [x] `backend/php/update_mascota.php` - Actualizar mascota
- [x] `backend/php/delete_mascota.php` - Eliminar mascota

## BASE DE DATOS
- [x] `database/maikpet_database.sql` - Script SQL completo

## ANDROID APP (en el proyecto)
El código fuente completo está en:
- `app/src/main/java/com/example/pet/` - Código Kotlin
- `app/src/main/res/` - Recursos (layouts, strings, etc.)
- `app/build.gradle` - Configuración de build
- `app/google-services.json` - Configuración Firebase

---

## PARA REPLICAR LA APP NECESITAS:

### 1. Backend (carpeta `backend/php/`)
Subir estos 10 archivos PHP al servidor

### 2. Base de Datos
Ejecutar el script SQL en `database/maikpet_database.sql`

### 3. Android App
El proyecto completo está en `app/`:
- Modificar `NetworkModule.kt` con la URL del servidor
- Agregar API Key de Google Maps en `gradle.properties`
- Agregar `google-services.json` de Firebase

---

## CONFIGURACIÓN REQUERIDA

### Backend (config.php)
```php
define('DB_HOST', 'localhost');
define('DB_USER', 'tu_usuario');
define('DB_PASS', 'tu_password');
define('DB_NAME', 'maikpet_db');
```

### Android (NetworkModule.kt)
```kotlin
private const val BASE_URL = "https://tu-dominio.com/pet/"
```

### Android (gradle.properties)
```properties
MAPS_API_KEY=TU_API_KEY_DE_GOOGLE_MAPS
```

---

## ARCHIVOS OPCIONALES

### Imágenes
- `app/src/main/res/drawable/` - Iconos e imágenes
- `app/src/main/res/mipmap/` - Iconos de launcher

### Sonidos
- `app/src/main/res/raw/bark.mp3` - Sonido de ladrido
- `app/src/main/res/raw/meow.mp3` - Sonido de maullido
