# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/the-scrabi/bin/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate
-keep class org.mozilla.javascript.** { *; }

-keep class org.mozilla.classfile.ClassFileWriter
-keep class com.google.android.exoplayer2.** { *; }

-dontwarn org.mozilla.javascript.tools.**
-dontwarn android.arch.util.paging.CountedDataSource
-dontwarn android.arch.persistence.room.paging.LimitOffsetDataSource


# Rules for icepick. Copy paste from https://github.com/frankiesardo/icepick
-dontwarn icepick.**
-keep class icepick.** { *; }
-keep class **$$Icepick { *; }
-keepclasseswithmembernames class * {
    @icepick.* <fields>;
}
-keepnames class * { @icepick.State *;}

-dontwarn okio.**
-dontwarn javax.annotation.**

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-dontwarn android.support.**

-ignorewarning