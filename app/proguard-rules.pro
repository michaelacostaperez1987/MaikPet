# Retrofit
-keepattributes Signature, Exceptions, *Annotation*
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class retrofit2.Call { *; }
-keep class retrofit2.Response { *; }
-keep class retrofit2.http.* { *; }

# Mantener interfaces de API para que no se ofusquen los tipos genéricos
-keep,allowobfuscation interface * extends retrofit2.Call
-keep,allowobfuscation class * implements retrofit2.Call

# MaikPetApi
-keep class com.macosta.maikpet.data.api.MaikPetApi { *; }
-keep class com.macosta.maikpet.data.api.* { *; }

# MaikPetApi
-keep class com.macosta.maikpet.data.api.MaikPetApi { *; }
-keep class com.macosta.maikpet.data.api.* { *; }

# Tipos genéricos - crítico para evitar ClassCastException
-keep,allowobfuscation class kotlin.coroutines.Continuation { *; }
-keep,allowobfuscation class kotlin.coroutines.SafeContinuation { *; }

# OkHttp + Okio + Logging
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class okhttp3.logging.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Data models
-keep class com.macosta.maikpet.data.model.** { <fields>; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Enums (Gson los necesita para serializar/deserializar)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# SSL / TrustManager - necesario para conexiones HTTPS
-keep class javax.net.ssl.** { *; }
-keep class com.macosta.maikpet.di.NetworkModule$* { *; }
-dontwarn javax.net.ssl.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
