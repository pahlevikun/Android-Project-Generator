<% if (useFirebase) { %>import com.google.firebase.appdistribution.gradle.firebaseAppDistribution<% } %>
import <%= packageName %>.plugin.properties.AppProperties
import <%= packageName %>.plugin.extension.ArtifactNameManipulator
import com.project.starter.easylauncher.filter.ChromeLikeFilter
import com.project.starter.easylauncher.filter.ColorRibbonFilter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    <% if (useFirebase) { %>
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.appdistribution)
    <% } %>
    alias(libs.plugins.easylauncher)
}

val appProperties = AppProperties.getInstance(project)
val artifactNameManipulator = ArtifactNameManipulator(project)

android {
    namespace = appProperties.applicationId
    compileSdk = appProperties.compileSdk

    defaultConfig {
        applicationId = appProperties.applicationId
        minSdk = appProperties.minSdk
        targetSdk = appProperties.targetSdk
        versionCode = appProperties.appVersionCode
        versionName = appProperties.appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    flavorDimensions += appProperties.DIMENSION
    productFlavors {
        <% flavors.forEach(function(flavor) { %>
        create("<%= flavor %>") {
            dimension = appProperties.DIMENSION
            <% if (flavor !== 'production') { %>
            applicationIdSuffix = ".<%= flavor %>"
            resValue("string", "app_name", appProperties.appName + " (<%= flavor.charAt(0).toUpperCase() + flavor.slice(1) %>)")
            <% } else { %>
            resValue("string", "app_name", appProperties.appName)
            <% } %>
        }
        <% }) %>
    }
}

easylauncher {
    variants {
        <% (disableRibbonFlavors || []).forEach(function(flavor) { %>
        <% (disableRibbonVariants || []).forEach(function(variant) { %>
        create("<%= flavor %><%= variant.charAt(0).toUpperCase() + variant.slice(1) %>") {
            enable(false)
        }
        <% }) %>
        <% }) %>
    }
    buildTypes {
        create("debug") {
            enable(true)
            filters(
                ChromeLikeFilter(
                    label = "DEBUG",
                    ribbonColor = "#FF0000",
                    gravity = ChromeLikeFilter.Gravity.BOTTOM,
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
                ColorRibbonFilter(
                    label = "<%= flavor %>",
                    ribbonColor = "#00a82a",
                    gravity = ColorRibbonFilter.Gravity.TOP,
                    textSizeRatio = 0.1f
                )
            )
        }
        <% }) %>
    }
}

android {
    buildTypes {
        release {
            <% if (useFirebase) { %>
            firebaseAppDistribution {
                artifactType = "AAB"
                groups = appProperties.firebaseTesterGroups
                serviceCredentialsFile = appProperties.firebaseServiceCredentials
                releaseNotesFile = rootProject.file("app_distribution_notes.txt").absolutePath
            }
            <% } %>
            isDebuggable = appProperties.isReleaseDebuggable
        }
        debug {
            <% if (useFirebase) { %>
            firebaseAppDistribution {
                artifactType = "APK"
                groups = appProperties.firebaseTesterGroups
                serviceCredentialsFile = appProperties.firebaseServiceCredentials
                releaseNotesFile = rootProject.file("app_distribution_notes.txt").absolutePath
            }
            <% } %>
        }
    }
}

android {
    applicationVariants.configureEach {
        val variantName = this.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        tasks.register("buildArtifact${variantName}") {
            dependsOn("clean")
            dependsOn("assemble${variantName}")
            dependsOn("bundle${variantName}")
        }
        tasks.register("distribute${variantName}") {
            dependsOn("appDistributionUpload${variantName}")
        }
        artifactNameManipulator.apply(this)
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
