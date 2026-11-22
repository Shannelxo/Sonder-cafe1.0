# Rules for release builds (R8). Most libraries ship their own consumer rules;
# these cover the parts of this app that are accessed by reflection or generated code.

# kotlinx.serialization: keep generated serializers for the API DTOs.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class com.sondercafe.app.data.remote.dto.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sondercafe.app.data.remote.dto.**$$serializer { *; }

# Retrofit needs generic signatures to read suspend function return types.
-keepattributes Signature, Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep line numbers for readable crash reports, but hide original file names.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
