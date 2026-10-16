# KMP Practice 

Answers to questions are in [ANSWERS.md](ANSWERS.md).

## Modules

- `shared` - Kotlin Multiplatform module for Android + iOS
- `androidApp` - small Android app that uses `shared` and shows the results on screen
- `structure` - separate KMP module for the targets / source sets tasks (jvm, iosArm64, js)
- `hierarchy` - KMP module for questions 16, 17 and 21: jvm + js + linuxX64 (+ iosArm64, iosSimulatorArm64) with a custom `jvmAndJsMain` source set

## Where to find each hands-on question

| Question | Topic | Files |
|---|---|---|
| 5 | expect / actual `getPlatform()` | `shared/src/commonMain/kotlin/com/example/shared/Platform.kt`<br>`shared/src/androidMain/kotlin/com/example/shared/Platform.android.kt`<br>`shared/src/iosMain/kotlin/com/example/shared/Platform.ios.kt` |
| 8 | targets `jvm()` and `iosArm64()` | `gradle-snapshots/question08-structure-build.gradle.kts.txt`<br>`structure/src/commonMain`, `structure/src/jvmMain`, `structure/src/iosArm64Main` |
| 11 | expect / actual `currentTimestamp()` | `shared/src/commonMain/kotlin/com/example/shared/Timestamp.kt`<br>`shared/src/androidMain/kotlin/com/example/shared/Timestamp.android.kt`<br>`shared/src/iosMain/kotlin/com/example/shared/Timestamp.ios.kt` |
| 12 | adding the `js` target | `structure/build.gradle.kts` (same as `gradle-snapshots/question12-structure-build.gradle.kts.txt`)<br>`structure/src/jsMain` |
| 13 | `Greeting` + `GreetingTest` | `shared/src/commonMain/kotlin/com/example/shared/Greeting.kt`<br>`shared/src/commonTest/kotlin/com/example/shared/GreetingTest.kt` |
| 16 | custom `jvmAndJsMain` source set + 3 `dependsOn` calls | `hierarchy/build.gradle.kts`<br>`hierarchy/src/jvmAndJsMain/kotlin/com/example/JvmAndJs.kt`<br>`hierarchy/src/jvmMain`, `hierarchy/src/jsMain`, `hierarchy/src/linuxX64Main`<br>`gradle-snapshots/question16-hierarchy-build.gradle.kts.txt` |
| 17 | adding `iosArm64` + `iosSimulatorArm64` (no default hierarchy) | `hierarchy/build.gradle.kts`<br>`./gradlew :hierarchy:printSourceSets` |
| 21 | `compilations.getByName("main")` for `jvm` | `hierarchy/build.gradle.kts` (`jvm {}` block)<br>`./gradlew :hierarchy:printJvmCompilations` |

`gradle-snapshots` has the `structure/build.gradle.kts` file as it looked for question 8 (before) and question 12 (after adding `js`).

For `hierarchy`: `question16-hierarchy-build.gradle.kts.txt` is the file for question 16, and `question17-21-hierarchy-build.gradle.kts.txt` is the final version (iOS targets + `printJvmCompilations`).
