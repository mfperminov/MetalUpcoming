object Versions {
    val kotlin = "1.3.72"
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
    val splitties = "3.0.0-alpha06"
    val lycheeVersion = "0.0.12"
}

object Deps {
    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val junit = "junit:junit:${Versions.junit}"
    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val testRunner = "androidx.test:runner:${Versions.testRunner}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    val kotlinAndroidExtensions =
        "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.kotlin}"
    val splittiesViewDSL = "com.louiscad.splitties:splitties-views-dsl:${Versions.splitties}"
    val splittiesRecyclerView =
        "com.louiscad.splitties:splitties-views-dsl-recyclerview:${Versions.splitties}"
    val lycheeExtendedPersistence =
        "net.aquadc.properties:extended-persistence:${Versions.lycheeVersion}"
    val lycheeProperties = "net.aquadc.properties:properties:${Versions.lycheeVersion}"
    val lycheePersistence = "net.aquadc.properties:persistence:${Versions.lycheeVersion}"
    val lycheeAndroidBindings = "net.aquadc.properties:android-bindings:${Versions.lycheeVersion}"
}