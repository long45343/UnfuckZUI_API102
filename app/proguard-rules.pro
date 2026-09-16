-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

-keep public class * implements xyz.long4543.unfuckzzui.core.FeatureHandler {
    public <init>();
}

-dontwarn android.app.**
-dontwarn android.os.**
-dontwarn com.android.internal.**
