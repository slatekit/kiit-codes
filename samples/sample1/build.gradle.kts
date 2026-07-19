plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "sample.SampleAppKt"
}

dependencies {
    implementation(project(":kiit-codes"))
}
