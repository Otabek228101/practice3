plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        val jvmCompilations = compilations
        tasks.register("printJvmCompilations") {
            group = "practice"
            doLast {
                println("main compilation: ${jvmCompilations.getByName("main").name}")
                jvmCompilations.forEach { compilation ->
                    val sourceSets = compilation.allKotlinSourceSets.map { it.name }.sorted()
                    println("${compilation.name} -> $sourceSets")
                }
            }
        }
    }
    js(IR) {
        browser()
    }
    linuxX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val jvmAndJsMain by creating { }

        val jvmMain by getting
        val jsMain by getting

        jvmAndJsMain.dependsOn(commonMain.get())
        jvmMain.dependsOn(jvmAndJsMain)
        jsMain.dependsOn(jvmAndJsMain)

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.register("printSourceSets") {
    group = "practice"
    doLast {
        kotlin.sourceSets.sortedBy { it.name }.forEach { sourceSet ->
            val parents = sourceSet.dependsOn.joinToString { it.name }
            println("${sourceSet.name}  ->  dependsOn: [$parents]")
        }
    }
}
