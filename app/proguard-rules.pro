# Add project specific ProGuard rules here.

# Keep Glance widget classes
-keep class androidx.glance.** { *; }
-keep class com.example.salarywidget.widget.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
