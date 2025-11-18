# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes
-keep class com.fitforge.app.data.** { *; }
-keep class com.fitforge.app.domain.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
