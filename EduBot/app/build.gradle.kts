import java.util.Properties
plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
// Supabase credentials are read from local.properties (never hardcoded)
val props = Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }
android {
    namespace = "com.edubot.app"; compileSdk = 34
    defaultConfig {
        applicationId = "com.edubot.app"; minSdk = 24; targetSdk = 34; versionCode = 1; versionName = "1.0"
        buildConfigField("String", "SUPABASE_URL", "\"${props.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${props.getProperty("SUPABASE_KEY", "")}\"")
    }
    buildFeatures { compose = true; buildConfig = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
