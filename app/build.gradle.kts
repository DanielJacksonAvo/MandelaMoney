plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mandelamoney"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "device"

    productFlavors {
        create("phone") {
            applicationIdSuffix = ".phone"
            minSdk = 31
            targetSdk = 35
            versionCode = 1
            versionName = "1.0"
        }
        create("tablet") {
            applicationIdSuffix = ".tablet"
            minSdk = 31
            targetSdk = 35
            versionCode = 1
            versionName = "1.0"
        }
    }

    defaultConfig {
        applicationId = "com.example.mandelamoney"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DB_USERNAME", "\"user\"")
        buildConfigField("String", "DB_PASSWORD", "\"J!Hs7#BJv&tCmyhA6h^xd3AXtpnEWUe5\"")
        buildConfigField("String", "DB_URL", "\"jdbc:mysql://jacksonserver.ddns.net:3306/MandelaMoneyDB?useSSL=true&requireSSL=true&verifyServerCertificate=false&noAccessToProcedureBodies=true\"")
        buildConfigField("String", "EMAIL_USERNAME", "\"mandelamoney.info@gmail.com\"")
        buildConfigField("String","EMAIL_PASSWORD","\"nehg zpqu bbeb pshb\"")
    }

    packaging{
        resources{
            excludes.add("META-INF/NOTICE.md")
            excludes.add("META-INF/LICENSE.md")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
        // Configure the source set for the 'tablet' product flavor
        getByName("tablet") {
            // Updated path to the standard location for flavor-specific manifests
            manifest.srcFile("src/tablet/AndroidManifest.xml")
        }
    }
}

dependencies {

    implementation(libs.android.mail)
    implementation (libs.android.activation)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.blurview)
    implementation(files("libs/mysql-connector-java-5.1.49.jar"))
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.camera.core)
    implementation (libs.camera.camera2)
    implementation (libs.camera.lifecycle)
    implementation (libs.camera.view)
    implementation(libs.zxing.android.embedded)
}
