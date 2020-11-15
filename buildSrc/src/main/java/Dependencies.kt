object Versions {
    val kotlin = "1.3.72"
    val okhttp = "4.7.2"
    val junit = "4.12"
    val testRunner = "1.2.0"
    val espressoCore = "3.2.0"
    val leakCanary = "2.0-beta-3"
    val minSdk = 24
    val targetSdk = 30
    val compileSdk = 30
    val versionCode = 2
    val versionName = "1.0.1"
    val androidGradlePlugin = "4.2.0-alpha16"
    val splitties = "3.0.0-alpha06"
    val lycheeVersion = "0.0.12"
    val cardViewVersion = "1.0.0"
}

object Deps {
    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val junit = "junit:junit:${Versions.junit}"
    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val okhttpTesting = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
    val testRunner = "androidx.test:runner:${Versions.testRunner}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    val kotlinAndroidExtensions =
        "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.kotlin}"
    val splittiesViewDSL = "com.louiscad.splitties:splitties-views-dsl:${Versions.splitties}"
    val splittiesViewDSLMaterial =
        "com.louiscad.splitties:splitties-views-dsl-material:${Versions.splitties}"
    val cardView = "androidx.cardview:cardview:${Versions.cardViewVersion}"
    val splittiesRecyclerView =
        "com.louiscad.splitties:splitties-views-dsl-recyclerview:${Versions.splitties}"
    val lycheeProperties = "net.aquadc.properties:properties:${Versions.lycheeVersion}"
    val lycheeAndroidBindings = "net.aquadc.properties:android-bindings:${Versions.lycheeVersion}"
}