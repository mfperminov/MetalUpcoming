object Versions {
    val kotlin = "1.6.0"
    val okhttp = "4.9.0"
    val junit = "4.12"
    val testRunner = "1.2.0"
    val espressoCore = "3.2.0"
    val leakCanary = "2.5"
    val minSdk = 21
    val targetSdk = 30
    val compileSdk = 30
    val versionCode = 4
    val versionName = "1.0.3"
    val androidGradlePlugin = "7.4.0-alpha02"
    val splitties = "3.0.0-beta01"
    val lycheeVersion = "0.0.17"
    val cardViewVersion = "1.0.0"
    val kotlinMppCollectionsVersion = "1.0-alpha05"
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
    val kotlinParcelize = "kotlin-parcelize"
    val splittiesViewDSL = "com.louiscad.splitties:splitties-views-dsl:${Versions.splitties}"
    val splittiesViewDSLMaterial =
        "com.louiscad.splitties:splitties-views-dsl-material:${Versions.splitties}"
    val cardView = "androidx.cardview:cardview:${Versions.cardViewVersion}"
    val splittiesRecyclerView =
        "com.louiscad.splitties:splitties-views-dsl-recyclerview:${Versions.splitties}"
    val lycheeProperties = "su.lychee:properties:${Versions.lycheeVersion}"
    val lycheeAndroidBindings = "su.lychee:android-bindings:${Versions.lycheeVersion}"
}
