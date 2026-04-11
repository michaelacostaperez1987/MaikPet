# 🐾 MaikPet - App de Adopción de Mascotas

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="100" />
  <h3>Aplicación Android para adopción gratuita de mascotas</h3>
</div>

---

## 📱 Funcionalidades

- 🗺️ **Mapa Interactivo** - Encuentra mascotas cerca de ti
- 📋 **Lista de Adopciones** - Ver todas las mascotas disponibles
- ➕ **Publicar Mascotas** - Agrega tus mascotas para adopción
- ✏️ **Editar/Eliminar** - Gestiona tus publicaciones
- 👤 **Login/Registro** - Sistema de usuarios
- 💬 **WhatsApp** - Contacta directamente con los adoptantes
- 🔔 **Notificaciones Push** - Recibe alertas de nuevas mascotas
- 📍 **Autocompletado** - Direcciones con sugerencias
- 🚫 **Validación** - Contra ventas y cruzas
- 🏠 **Home** - Pantalla informativa

---

## 📁 Estructura del Proyecto

```
Pet2/
├── DOCUMENTACION_COMPLETA.md     # Manual completo
├── GUIA_RAPIDA.md               # Guía rápida
├── LISTA_ARCHIVOS.md            # Lista de archivos
├── backend/
│   └── php/                      # Archivos PHP del backend
│       ├── config.php
│       ├── register.php
│       ├── login.php
│       ├── logout.php
│       ├── get_session.php
│       ├── get_mascotas.php
│       ├── get_mis_mascotas.php
│       ├── add_mascota.php
│       ├── update_mascota.php
│       └── delete_mascota.php
├── database/
│   └── maikpet_database.sql     # Script SQL
└── app/                          # Código Android
    ├── src/main/java/com/example/pet/
    │   ├── MainActivity.kt
    │   ├── MaikPetApplication.kt
    │   ├── data/
    │   ├── di/
    │   ├── firebase/
    │   ├── ui/
    │   ├── util/
    │   ├── viewmodel/
    │   └── worker/
    └── src/main/res/
```

---

## 🚀 Tecnologías

| Componente | Tecnología |
|------------|------------|
| Frontend | Kotlin + Jetpack Compose |
| Backend | PHP 7+ |
| Base de Datos | MySQL 5.7+ |
| Maps | Google Maps API |
| Push | Firebase Cloud Messaging |
| DI | Hilt |
| Networking | Retrofit + OkHttp |

---

## ⚙️ Configuración

### 1. Backend PHP
Subir archivos de `backend/php/` al servidor

### 2. Base de Datos
Ejecutar `database/maikpet_database.sql`

### 3. Android
1. Configurar URL del servidor en `NetworkModule.kt`
2. Agregar Google Maps API Key
3. Agregar Firebase google-services.json

Ver `GUIA_RAPIDA.md` para más detalles

---

## 📄 Licencia

Este proyecto es privado y para uso exclusivo de MaikPet.

---

## 👨‍💻 Desarrollador

**Michael** - Desarrollador de MaikPet

---

<div align="center">
  <p>Hecho con ❤️ para los animales</p>
  <p>🐾 Adopta, no compres 🐾</p>
</div>
