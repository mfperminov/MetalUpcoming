object Versions {
    val kotlin = "1.3.72"
    val recyclerView = "1.1.0-beta04"
    val okhttp = "4.1.1"
    val junit = "4.12"
    val testRunner = "1.2.0"
    val espressoCore = "3.2.0"
    val leakCanary = "2.0-beta-3"
    val minSdk = 21
    val targetSdk = 29
    val compileSdk = 29
    val versionCode = 1
    val versionName = "1.0"
    val androidGradlePlugin = "4.1.0-alpha07"
}

object Deps {
    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    val junit = "junit:junit:${Versions.junit}"
    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val testRunner = "androidx.test:runner:${Versions.testRunner}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    val kotlinAndroidExtensions =
        "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.kotlin}"
}