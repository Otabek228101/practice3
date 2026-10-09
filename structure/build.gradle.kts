plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    iosArm64()
    js {
        browser()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.register("printSourceSets") {
    group = "practice"
    doLast {
        kotlin.sourceSets.names.sorted().forEach {
            println(it)
        }
    }
}
