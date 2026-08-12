plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass = "sample.SampleApp"
}

dependencies {
    implementation(project(":kiit-codes"))
}
