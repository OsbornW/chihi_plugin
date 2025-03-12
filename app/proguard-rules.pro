# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.sjkj.ad.** {
public <methods>;
}
#保持bean⽂件
-keep class com.sjkj.ad.api.** { *; }

  -keep class com.chihi.adplugin.AdPlugin { *; }
  -keep class com.chihi.adplugin.AdConfig { *; }
  -keep class com.chihi.adplugin.AdLoadCallbackBuilder { *; }
  -keep class com.chihi.adplugin.PluginManager { *; }

  -keep class kotlin.jvm.functions.Function0
  -keepclassmembers class kotlin.jvm.functions.Function0 { *; }
  -keep class kotlin.jvm.functions.Function1
  -keepclassmembers class kotlin.jvm.functions.Function1 { *; }

  # 保持 lifecycle 相关接口和类不被混淆
  -keep class androidx.lifecycle.** { *; }

  # 保持 Gson 相关类不被混淆
  -keep class com.google.gson.** { *; }










