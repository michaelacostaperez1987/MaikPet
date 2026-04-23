# Corrección de Error "No autorizado" en Dar en Adopción

## Problema
El endpoint `add_mascota.php` devuelve "No autorizado" porque la autenticación basada en sesiones PHP no se mantiene entre solicitudes. El frontend envía headers personalizados (`X-Session-Id`, `X-Auth-Token`, `X-User-Id`) pero el backend no los utiliza.

## Solución Implementada
Se modificaron los archivos del backend para:
1. **Enviar el ID de sesión PHP al frontend** después de login/registro.
2. **Aceptar el header `X-Session-Id`** para recuperar la sesión existente.
3. **Iniciar sesión automáticamente después del registro**.

## Archivos Modificados

### 1. `backend/php/config.php`
- Agregada lógica para leer el header `X-Session-Id` y establecer `session_id()` antes de iniciar la sesión.
- Compatible con servidores que no tienen `getallheaders()`.

### 2. `backend/php/login.php`
- Ahora devuelve `sessionId` (el ID de sesión PHP) y un `token` aleatorio en la respuesta JSON.
- Ejemplo de respuesta:
  ```json
  {
    "success": true,
    "usuario": { ... },
    "sessionId": "abc123...",
    "token": "a1b2c3..."
  }
  ```

### 3. `backend/php/register.php`
- Ahora inicia sesión automáticamente después del registro.
- Devuelve los mismos campos que login (`sessionId`, `token`, `usuario`).

### 4. `backend/php/add_mascota.php`
- Se removió información de debug que se había agregado temporalmente.

## Cómo Funciona Ahora
1. El usuario inicia sesión o se registra.
2. El backend devuelve `sessionId` y `token`.
3. El frontend guarda estos valores en `SharedPreferences`.
4. En cada solicitud, el interceptor en `NetworkModule.kt` añade los headers:
   - `X-Session-Id`: con el ID de sesión.
   - `X-Auth-Token`: con el token (no se valida aún, pero se envía).
   - `X-User-Id`: con el ID del usuario.
5. El backend recibe `X-Session-Id`, establece la sesión correspondiente y verifica que el usuario esté logueado.

## Requisitos de Despliegue
1. **Subir los archivos modificados al servidor** (reemplazar en `lmcosturas.com/pet/`):
   - `config.php`
   - `login.php`
   - `register.php`
   - `add_mascota.php` (opcional, solo si se desea quitar el debug)

2. **No se requieren cambios en el frontend** (ya tiene la lógica para guardar y enviar los headers).

3. **Verificar que la sesión PHP esté configurada correctamente** en el servidor (almacenamiento de sesiones por defecto en archivos).

## Pruebas
1. Iniciar sesión en la app.
2. Intentar publicar una mascota en adopción.
3. Si persiste el error, revisar los logs del servidor (error_log) o agregar temporalmente debug en `add_mascota.php`.

## Notas de Seguridad
- El token generado es aleatorio pero no se valida actualmente. Se puede extender la lógica para almacenar tokens en la base de datos y validarlos.
- La sesión PHP sigue siendo el mecanismo principal de autenticación; el header `X-Session-Id` solo ayuda a recuperarla.

## Archivos Afectados (Backend)
- `backend/php/config.php`
- `backend/php/login.php`
- `backend/php/register.php`
- `backend/php/add_mascota.php`
- (Todos los demás endpoints protegidos que incluyen `config.php` se benefician automáticamente)

## Soporte
Si el error persiste, contactar al desarrollador para revisar:
- Configuración de CORS en el servidor.
- Almacenamiento de sesiones PHP (puede estar configurado en memoria y perderse entre reinicios).
- Headers enviados por el frontend (usar el interceptor de logging de OkHttp).