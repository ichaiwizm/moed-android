# Moed — règles ProGuard/R8 (release). isMinifyEnabled=false par défaut : ces règles
# ne s'appliquent que si le shrinking est activé ultérieurement. Conservées ici par sûreté.

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.moed.app.**$$serializer { *; }
-keepclassmembers class com.moed.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.moed.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Retrofit / OkHttp (module réseau opt-in) ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }

# --- KosherJava (moteur) : réflexion possible sur les enums de calendrier ---
-keep class com.kosherjava.zmanim.** { *; }
