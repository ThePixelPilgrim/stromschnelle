import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Single source of truth: version.properties holds only versionName=x.y.z.
// versionCode is derived so it always increases as long as x.y.z increases.
val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}
val appVersionName: String = versionProps.getProperty("versionName")
    ?: error("versionName missing from version.properties")
val appVersionCode: Int = appVersionName.trim().split(".").let { parts ->
    require(parts.size == 3) { "versionName must be x.y.z, got '$appVersionName'" }
    val (major, minor, patch) = parts.map {
        it.toIntOrNull() ?: error("versionName component '$it' is not an integer")
    }
    require(minor in 0..99 && patch in 0..99) {
        "minor and patch must each be < 100 (got $appVersionName)"
    }
    major * 10000 + minor * 100 + patch
}

// Optional release signing: present only when app/keystore.properties exists
// (kept out of git). Without it, assembleRelease produces an unsigned APK.
val keystoreProps = Properties().apply {
    val f = rootProject.file("app/keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val hasReleaseSigning = keystoreProps.getProperty("storeFile") != null

android {
    namespace = "de.nereide.stromschnelle"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.nereide.stromschnelle"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")

                // v1 (JAR signing) is only needed below API 24; minSdk is 26.
                // v3 carries the proof-of-rotation lineage and is the ONLY
                // mechanism by which an Android signing key can ever be
                // rotated — without it, a compromised key means abandoning the
                // applicationId and every installed user.
                //
                // Safe to add to an already-released app: the signer identity is
                // unchanged, so v0.1.5 (v2-only) updates cleanly to a v2+v3 build.
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

// Room exports the schema JSON here so MigrationTestHelper can read it.
// The directory is committed — the migration test needs version 1's schema.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.glance.appwidget)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqlite.jdbc)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
