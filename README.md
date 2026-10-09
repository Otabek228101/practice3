# KMP Practice 

Answers to all questions (1–13) are in [ANSWERS.md](ANSWERS.md).

## Modules

- `shared` - Kotlin Multiplatform module for Android + iOS
- `androidApp` - small Android app that uses `shared` and shows the results on screen
- `structure` - separate KMP module for the targets / source sets tasks (jvm, iosArm64, js)

## Where to find each hands-on question

| Question | Topic | Files |
|---|---|---|
| 5 | expect / actual `getPlatform()` | `shared/src/commonMain/kotlin/com/example/shared/Platform.kt`<br>`shared/src/androidMain/kotlin/com/example/shared/Platform.android.kt`<br>`shared/src/iosMain/kotlin/com/example/shared/Platform.ios.kt` |
| 8 | targets `jvm()` and `iosArm64()` | `gradle-snapshots/question08-structure-build.gradle.kts.txt`<br>`structure/src/commonMain`, `structure/src/jvmMain`, `structure/src/iosArm64Main` |
| 11 | expect / actual `currentTimestamp()` | `shared/src/commonMain/kotlin/com/example/shared/Timestamp.kt`<br>`shared/src/androidMain/kotlin/com/example/shared/Timestamp.android.kt`<br>`shared/src/iosMain/kotlin/com/example/shared/Timestamp.ios.kt` |
| 12 | adding the `js` target | `structure/build.gradle.kts` (same as `gradle-snapshots/question12-structure-build.gradle.kts.txt`)<br>`structure/src/jsMain` |
| 13 | `Greeting` + `GreetingTest` | `shared/src/commonMain/kotlin/com/example/shared/Greeting.kt`<br>`shared/src/commonTest/kotlin/com/example/shared/GreetingTest.kt` |

`gradle-snapshots` has the `structure/build.gradle.kts` file as it looked for question 8 (before) and question 12 (after adding `js`).
