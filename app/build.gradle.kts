plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mandelamoney"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }


    defaultConfig {
        applicationId = "com.example.mandelamoney"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DB_USERNAME", "\"user\"")
        buildConfigField("String", "DB_PASSWORD", "\"J!Hs7#BJv&tCmyhA6h^xd3AXtpnEWUe5\"")
        buildConfigField("String", "DB_URL", "\"jdbc:mysql://jacksonserver.ddns.net:3306/MandelaMoneyDB?useSSL=true&requireSSL=true&verifyServerCertificate=false&noAccessToProcedureBodies=true\"")
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
}

dependencies {

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
}