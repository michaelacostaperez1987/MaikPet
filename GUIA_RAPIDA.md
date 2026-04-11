# MAIKPET - GUÍA RÁPIDA DE CONFIGURACIÓN

## PASO 1: BASE DE DATOS

### 1.1 Crear Base de Datos
1. Ir a phpMyAdmin o panel de hosting
2. Crear una base de datos llamada `maikpet_db`
3. Importar el archivo: `database/maikpet_database.sql`

### 1.2 Configurar credenciales
Abrir `backend/php/config.php` y cambiar:
```php
define('DB_HOST', 'localhost');
define('DB_USER', 'tu_usuario_mysql');
define('DB_PASS', 'tu_password_mysql');
define('DB_NAME', 'maikpet_db');
```

---

## PASO 2: BACKEND PHP

### 2.1 Subir archivos al servidor
1. Subir todos los archivos de `backend/php/` a tu servidor
2. Preferiblemente en una carpeta llamada `pet/`
3. Ejemplo: `public_html/pet/`

### 2.2 Archivos a subir
- config.php
- register.php
- login.php
- logout.php
- get_session.php
- get_mascotas.php
- get_mis_mascotas.php
- add_mascota.php
- update_mascota.php
- delete_mascota.php

---

## PASO 3: ANDROID APP

### 3.1 Configurar URL del servidor
En `app/src/main/java/com/example/pet/di/NetworkModule.kt`:
```kotlin
private const val BASE_URL = "https://tu-dominio.com/pet/"
```

### 3.2 Configurar Google Maps API
1. Ir a https://console.cloud.google.com/
2. Crear proyecto
3. Habilitar "Maps SDK for Android"
4. Crear API Key
5. En `gradle.properties`:
```properties
MAPS_API_KEY=TU_API_KEY_AQUI
```

### 3.3 Configurar Firebase
1. Ir a https://console.firebase.google.com/
2. Crear proyecto
3. Agregar app Android con package: `com.example.pet`
4. Descargar `google-services.json`
5. Colocar en `app/google-services.json`

---

## PASO 4: COMPILAR

### 4.1 Debug APK
```bash
./gradlew assembleDebug
```

### 4.2 Release APK
1. Crear keystore
2. Configurar signing en build.gradle
3. Compilar:
```bash
./gradlew assembleRelease
```

---

## VERIFICACIÓN

### Probar Backend
Abrir en navegador:
- https://tu-dominio.com/pet/get_session.php
- Debe mostrar: `{"logged_in":false}`

### Probar Android
1. Instalar APK en dispositivo
2. Abrir la app
3. Verificar que carga el mapa
4. Probar registrar usuario
5. Probar agregar mascota

---

## SOLUCIÓN DE PROBLEMAS COMUNES

| Problema | Solución |
|----------|----------|
| Error 500 en PHP | Verificar credenciales en config.php |
| Imágenes no se guardan | Cambiar columna a MEDIUMTEXT |
| Mapa no funciona | Verificar API Key de Google Maps |
| Login no funciona | Verificar hash de password |
| App no conecta | Verificar URL en NetworkModule.kt |

---

## ESTRUCTURA DE ARCHIVOS

```
Pet2/
├── DOCUMENTACION_COMPLETA.md     ← Manual completo
├── GUIA_RAPIDA.md                ← Esta guía
├── backend/
│   └── php/
│       ├── config.php            ← Configuración BD
│       ├── register.php          ← Registro usuarios
│       ├── login.php             ← Login
│       ├── logout.php            ← Logout
│       ├── get_session.php       ← Verificar sesión
│       ├── get_mascotas.php      ← Obtener mascotas
│       ├── get_mis_mascotas.php  ← Mascotas del usuario
│       ├── add_mascota.php       ← Agregar mascota
│       ├── update_mascota.php    ← Actualizar mascota
│       └── delete_mascota.php    ← Eliminar mascota
├── database/
│   └── maikpet_database.sql     ← Script SQL
└── app/                          ← Código Android
```

---

## CONTACTOS DE SOPORTE

Para soporte técnico, contacta al desarrollador.
