# MaikPet - Documentación Técnica

## Descripción
Aplicación Android para adopción de mascotas en Uruguay. Permite publicar y adoptar mascotas de forma gratuita.

## Tecnologías
- **Frontend**: Android (Kotlin + Jetpack Compose)
- **Backend**: PHP + MySQL
- **Notificaciones**: Firebase Cloud Messaging (FCM)
- **Arquitectura**: MVVM + Clean Architecture

## Funcionalidades Implementadas

### 1. Autenticación
- **Login tradicional**: Email + contraseña
- **Registro**: Nombre, email, contraseña, teléfono, dirección, edad (18+)
- **Sesión persistente**: Token guardado localmente (7 días)
- **Logout**: Cierra sesión y limpia datos locales

### 2. Mapa de Mascotas
- Ver todas las mascotas en adopción en un mapa
- Filtro por tipo (Perro/Gato)
- geolocalización automática
- Vista de detalles al tocar marcador

### 3. Publicar Mascota (Dar en Adopción)
- Formulario con: nombre, tipo, edad, vacunas, dirección, descripción
- Foto desde cámara o galería
- Geocodificación de dirección (OpenStreetMap)
- Validación: no permite ventas/cruzas/permutas
- Requiere estar logueado

### 4. Mis Mascotas
- Ver mascotas publicadas por el usuario
- Editar información
- Eliminar publicación

### 5. Perfil de Usuario
- Editar nombre, dirección, teléfono
- Guardado local (sin backend)

### 6. Legal
- Términos y Condiciones
- Política de Privacidad
- Aceptación obligatoria para registro

### 7. Notificaciones
- Notificaciones push cuando se publica nueva mascota
- FCM integrado

## Estructura del Proyecto

```
MaikPet/
├── app/
│   └── src/main/
│       ├── java/com/macosta/maikpet/
│       │   ├── data/
│       │   │   ├── api/         # Retrofit API
│       │   │   ├── model/       # Data classes
│       │   │   └── repository/  # Repository pattern
│       │   ├── di/              # Hilt modules
│       │   ├── firebase/        # FCM service
│       │   ├── ui/
│       │   │   ├── screens/     # Compose screens
│       │   │   ├── components/  # Reusable components
│       │   │   └── theme/       # Theme colors
│       │   └── viewmodel/       # ViewModels
│       └── res/
├── backend/php/                 # PHP backend
└── C:\Sitio\                   # Archivos para hosting
```

## Autenticación (Flujo)

### Login
1. App envía `POST /login.php` con email y password
2. Servidor verifica credenciales
3. Devuelve: `sessionId`, `token`, `usuario`
4. App guarda token en SharedPreferences

### Requests autenticadas
- App envía headers: `X-Auth-Token`, `X-User-Id`
- PHP verifica en `isLoggedIn()` y guarda en sesión

### Logout
1. App llama `POST /logout.php`
2. Limpia token y user_id de SharedPreferences
3. Servidor destruye sesión PHP

## API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | login.php | Iniciar sesión |
| POST | register.php | Registrarse |
| POST | logout.php | Cerrar sesión |
| GET | get_session.php | Verificar sesión |
| GET | get_mascotas.php | Listar todas las mascotas |
| GET | get_mis_mascotas.php | Mis mascotas |
| POST | add_mascota.php | Publicar mascota |
| POST | update_mascota.php | Actualizar mascota |
| DELETE | delete_mascota.php | Eliminar mascota |
| POST | update_perfil.php | Actualizar perfil |
| POST | save_device_token.php | Guardar token FCM |

## Configuración

### Base de datos (MySQL)
- Host: 192.185.112.105
- User: lmcostur_macostas
- DB: lmcostur_test

### API URL
- `https://lmcosturas.com/pet/`

### Firebase
- Project ID: maikpet-676bf
- Service account en: `maikpet-676bf-firebase-adminsdk-fbsvc-0eec82c200.json`

## Dependencias Android

```gradle
// Core
kotlin + Jetpack Compose
Hilt (DI)
Retrofit + OkHttp (Networking)
Coil (Images)
DataStore (Preferences)

// Firebase
firebase-messaging-ktx
firebase-auth-ktx

// Google
play-services-auth (Sign-In)
play-services-ads (AdMob)
```

## Notas

- No se permite venta, cruza ni permuta de mascotas
- Solo adopciones gratuitas
- Validación de edad mínima 18 años para registro
- Aceptación obligatoria de TyC y privacidad

---

**Última actualización**: 22/04/2026
**Versión**: 1.0.0 (commit d914200)