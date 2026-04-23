# Fix: Conectividad en builds Release (Play Console)

## Problema
La app no conecta a Internet cuando se descarga desde Play Console (build release). En debug funciona bien.

## Causa Raíz
Dos problemas:

### 1. R8/ProGuard Rules incompletas (CRÍTICO)
`app/proguard-rules.pro` no tenía:
- `-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations` — necesario para que Retrofit pueda leer `@GET`, `@POST` etc. en runtime
- `-keepattributes InnerClasses`
- `-keep class okhttp3.logging.** { *; }` — el logging interceptor se eliminaba
- `-keepclassmembers enum *` — Gson necesita `values()` y `valueOf()` para enums

En debug (`minifyEnabled false`) funciona. En release R8 ofusca/elimina lo que no está protegido.

### 2. Network Security Config
`network_security_config.xml` solo tenía `domain-config` para HTTP pero sin `base-config` explícito, dejando comportamiento por defecto ambiguo.

## Archivos Modificados
- `app/proguard-rules.pro` — reglas completas para Retrofit + OkHttp + Gson + Hilt + enums
- `app/src/main/res/xml/network_security_config.xml` — se agregó `base-config` con `cleartextTrafficPermitted="false"` y trust-anchors

## Cómo Verificar
1. Build release local: `./gradlew assembleRelease`
2. Si no tenés keystore, usar debug build con minify: cambiar temporalmente `minifyEnabled true` en debug
3. Verificar con `adb install` el APK release firmado
