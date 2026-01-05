plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    <% if (useFirebase) { %>
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    <% } %>
    alias(libs.plugins.easylauncher)
}

android {
    namespace = "<%= packageName %>"
    compileSdk = 35

    defaultConfig {
        applicationId = "<%= packageName %>"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += "<%= flavorDimension %>"
    productFlavors {
        <% flavors.forEach(function(flavor) { %>
        create("<%= flavor %>") {
            dimension = "<%= flavorDimension %>"
            <% if (flavor !== 'production') { %>
            applicationIdSuffix = ".<%= flavor %>"
            resValue("string", "app_name", "<%= appName %> (<%= flavor.charAt(0).toUpperCase() + flavor.slice(1) %>)")
            <% } else { %>
            resValue("string", "app_name", "<%= appName %>")
            <% } %>
        }
        <% }) %>
    }
}

easylauncher {
    buildTypes {
        create("debug") {
            enable(true)
            filters(
                com.project.starter.easylauncher.filter.ChromeLikeFilter(
                    label = "DEBUG",
                    ribbonColor = "#FF0000",
                    gravity = com.project.starter.easylauncher.filter.ChromeLikeFilter.Gravity.BOTTOM,
                    textSizeRatio = 0.13f
                )
            )
        }
    }
    productFlavors {
        <% flavors.forEach(function(flavor) { %>
        create("<%= flavor %>") {
            enable(true)
            filters(
                com.project.starter.easylauncher.filter.ColorRibbonFilter(
                    label = "<%= flavor %>",
                    ribbonColor = "#00a82a",
                    gravity = com.project.starter.easylauncher.filter.ColorRibbonFilter.Gravity.TOP,
                    textSizeRatio = 0.1f
                )
            )
        }
        <% }) %>
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    <% if (useFirebase) { %>
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    <% } %>

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
