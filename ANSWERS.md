# Section 1 — What Is Kotlin Multiplatform (Lecture 8)
## Question 1
`Context`: A teammate says: "KMP is basically like Flutter or React Native, just for Kotlin developers." You want to correct this without being dismissive.

`Task`: Explain, using the lecture's own contrasts, why Kotlin Multiplatform is neither a bridge nor a VM. Name specifically what Flutter renders through, what React Native talks to native views across, and what Kotlin/Native produces for iOS instead.

`answer:`
- Flutter renders its own widget tree through Skia/Impeller rather than using native platform views. 
- React Native talks to native views across a JS bridge (or the newer JSI).
- Kotlin/Native does not use an interpreter; instead, it uses LLVM to produce real ARM64 binaries for iOS.   
Because shared KMP code compiles straight to native code, the application can keep its UI layer completely native to the platform.

## Question 2
`Context`: Your Android and iOS teams are currently writing the same validation, networking, and business-rule code twice — once in Kotlin, once in Swift — and you're pitching KMP as the fix.

`Task`: Describe the "two codebases, one product" problem exactly as the lecture frames it, and state the three consequences of duplication the slide lists as the cost of keeping things this way.

`answer`:

The lecture frames the "two codebases, one product" problem like this: Android and iOS teams often rebuild the exact same logic, twice, in two languages. The Android team writes validation, networking, and business rules in Kotlin, and the iOS team writes the same validation, networking, and business rules again in Swift.   
The slide lists these three consequences as the cost of this duplication:
- Duplicated bugs.   
- Inconsistent behavior between platforms.   
- Double the QA effort for every business-logic change.   

## Question 3
`Context`: A product manager asks what percentage of the app they should expect to be able to share, and which kinds of features are realistic to share versus which should stay platform-specific.

`Task`: Using the lecture's "typically shared vs. usually kept native" table, list the typical shared percentage range and give two examples each of commonly shared logic and logic that usually stays native, with the reason given for each native example.

`answer`:
Most production teams typically share **60–80%** of their codebase.

Commonly Shared Logic:
- Networking & API clients (e.g., using Ktor as a shared HTTP client).
- Validation & business rules (to ensure this logic behaves identically across platforms).

Usually Kept Native:
- Camera, biometrics, and sensors: These are kept native because they require deep platform-hardware integration.
- Push notifications: These stay native because they rely on platform-specific delivery services.

## Question 4
``Context``: Before committing engineering time, your team wants to know which KMP targets are safe to ship to production today and which are still risky.

``Task``: From the platform stability table, name every target listed as Stable and the one target listed as Beta, and state what "Beta" implies about using it for a production feature.

`answer`:
Targets listed as Stable:
- Android   iOS (Arm64 + Simulator)   
- Desktop - Windows, macOS, Linux   
- Server / backend (e.g., with Ktor)   
- Web — Kotlin/JS   

Target listed as Beta:
- Web — Kotlin/Wasm

What "Beta" implies for a production feature:

Beta status indicates that the target is "still maturing" rather than fully "stable & battle-tested". Using a Beta target for production features carries higher engineering risk, as APIs, tooling, performance optimizations, or compiler implementations are still undergoing active development and may undergo breaking changes or exhibit limitations compared to stable platforms.

## Question 5
`Context`: The lecture's expect/actual example declares a platform accessor with a common declaration in commonMain/Platform.kt, and separate implementations in androidMain/Platform.android.kt and iosMain/Platform.ios.kt.

`Task`: In Android Studio, create a new Kotlin Multiplatform project (or module) targeting Android and iOS. In commonMain, declare `expect fun getPlatform(): Platform` alongside a simple `Platform` class (or interface) with a `name: String` property. Then provide `actual` implementations in androidMain and iosMain that return a platform-specific name (e.g. "Android" and "iOS"). Build the project and confirm both actuals resolve with no red underlines, then take a screenshot of the three files side by side.

`answer`:

This is what these files look like:

``commonMain/Platform.kt``:
```kt
package com.example.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
```

``androidMain/Platform.android.kt``:
```kt
package com.example.shared

class AndroidPlatform : Platform {
    override val name: String = "Android"
}

actual fun getPlatform(): Platform = AndroidPlatform()
```

``iosMain/Platform.ios.kt``:

```kotlin
package com.example.shared

class IOSPlatform : Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform = IOSPlatform()
```

The project was successfully compiled (Build finished UP-TO-DATE status). The absence of errors and red underlines in the IDE (Android Studio) confirms that the compiler successfully found actual implementations for all declared platforms (Android and iOS) and correctly linked them to the expect declaration.

Now, when getPlatform() is called from the shared code, the program will automatically execute the appropriate platform‑specific code depending on the device.

## Question 6
`Context`: A colleague wants to also share the UI layer, not just the business logic, and is confused about whether that requires a completely different tool than Kotlin Multiplatform.

`Task`: Explain the relationship between Kotlin Multiplatform and Compose Multiplatform: what each one shares, which platforms Compose Multiplatform is stable on versus still in Beta, and why you can use KMP without Compose Multiplatform but never the reverse.

`answer`:
1. What each of the tools shares
- Kotlin Multiplatform (KMP): Shares business logic - the network layer, data storage, validation, and state management. At the same time, the interface (UI) remains 100% native for each platform (for example, Jetpack Compose on Android and SwiftUI on iOS).
- Compose Multiplatform (CMP): This is a declarative UI framework from JetBrains, built on the basis of KMP, and it allows you to additionally share the **user interface (UI) layer.

2. Stability of Compose Multiplatform across platforms

- Stable (Stable): Android, iOS, and Desktop (Windows, macOS, Linux).
- Beta (In beta version): Web (based on Kotlin/Wasm).

3. Why KMP can be used without CMP, but not vice versa
- KMP without CMP: KMP is the base compiler and architectural foundation. You can use it only for shared business logic code, leaving the UI native.
- CMP is impossible without KMP: Compose Multiplatform is built directly *on top of* the KMP infrastructure. CMP uses KMP compilation mechanisms, targets, and source sets to render the interface on different platforms, so it cannot work in isolation from KMP

# Section 2 — KMP Project Structure Fundamentals (Lecture 9)

## Question 7
`Context`: A student writes `import java.io.File` inside a function placed in commonMain and is confused why the IDE flags it as unresolved, even though the same import compiles fine in a plain JVM project.

`Task`: Explain what "common code" means in the lecture's definition, and explain — using the java.io.File example — why code that only exists on some targets cannot be referenced from commonMain.

`answer`:

"common code" (which lives in commonMain) is code shared across platforms that acts as a single source, which the compiler then compiles differently per target to produce platform-specific binaries.   Because this code must be able to compile for every target in the project, Kotlin actively blocks platform-only APIs in commonMain.
Using the java.io.File example: this class is a part of the JDK. Because commonMain is set up to also compile to native and JS targets where the JDK simply does not exist, the Kotlin compiler refuses to compile the reference at all. The IDE flags it as unresolved to ensure you don't write code that would break when compiled for non-JVM platforms

## Question 8
`Context`: The lecture describes targets as "labels" attached to source files through the Gradle DSL, using `kotlin { jvm(); iosArm64() }` as the minimal example.

`Task`: In Android Studio, open (or create) a Kotlin Multiplatform module's build.gradle.kts and declare exactly the two targets from the lecture's example: `jvm()` and `iosArm64()`. Sync the project, then use the Project view to confirm that Gradle has created a commonMain source set plus one source set per declared target. Record the exact source set folder names you see.

`answer`:

When you declare the jvm() and iosArm64() targets in the build.gradle.kts file and sync the project, the Kotlin Gradle plugin automatically generates the corresponding source sets.   If you look in the Project view under the src/ directory, the exact source set folder names you will see are:commonMain (The root source set for code shared across all targets).   jvmMain (The source set created for the jvm() target).   iosArm64Main (The source set created for the iosArm64() target).   (Note: Gradle will also generate the matching test directories for each of these: commonTest, jvmTest, and iosArm64Test)

## Question 9
`Context`: Two developers disagree about what makes something a "source set" in Kotlin Multiplatform versus just a regular folder of Kotlin files.

`Task`: List the four defining properties of a source set given in the lecture, and explain in your own words what distinguishes commonMain (a root source set) from androidMain (a platform/leaf source set).

`answer`:
The source set is defined by four key properties:
- It has a unique name within the project.
- It contains the source files, which are usually located in a folder with the same name.
- It clearly defines which platforms (targets) its code will be compiled for.
- It sets its own dependencies and compiler settings.

The difference between `commonMain` and the platform-based `androidMain`:
- `commonMain` (root set): This is a universal set that is compiled for all declared targets in the project. Since this code must work everywhere, it is completely isolated from platform APIs — you cannot use classes or libraries that exist only in one system (for example, JDK).
- `androidMain` (platform/target set): This set is compiled only for one specific target platform. Thanks to this limitation, it “unlocks” direct access to the platform API, allowing you to write code that uses Android‑specific tools that are not available at the general code level.

## Question 10
`Context`: While compiling for iosArm64, a build merges code from three different source sets into a single compiled program, and a junior developer asks how Kotlin decides which source sets to include.

`Task`: State the compilation "merge rule" from the lecture (which source sets get gathered together for a given target), and list the four golden rules that follow from it — including which direction information is allowed to flow between commonMain and a platform source set.

`answer`:

The merging rule during compilation is as follows: **Kotlin collects each source set marked (labelled) for a given target and then compiles them together**. For example, when compiling for a specific Apple target (such as `iosArm64`), the compiler combines `commonMain` + an intermediate set (for example, `appleMain`) + the platform set (`iosArm64Main`) into a single program.

The four golden rules that follow from this principle are:

1. Declare a target before Kotlin will compile anything to it.
2. Code shared by everything belongs in `commonMain`.
3. Code for one target belongs in that target's own source set.
4. Platform source sets can see `commonMain`, but `commonMain` can never see back into them. Information flows only one way: from general (`commonMain`) down to specific (platform source sets), never the reverse.

## Question 11
`Context`: You want to practice the expect/actual mechanism on a slightly different example than the platform accessor already covered in Lecture 8.

`Task`: In the same Android Studio KMP project, add to commonMain an `expect fun currentTimestamp(): Long`. Provide an `actual` implementation in androidMain using `System.currentTimeMillis()`, and an `actual` implementation in iosMain using an iOS-appropriate API (e.g. `platform.Foundation.NSDate().timeIntervalSince1970`). Build the project for both targets and confirm there are no unresolved `actual` errors.

`answer`:

``commonMain/Timestamp.kt``:
```kt
package com.example.shared

expect fun currentTimestamp(): Long
```

``androidMain/Timestamp.android.kt``:
```kt
package com.example.shared

actual fun currentTimestamp(): Long {
    return System.currentTimeMillis()
}
```

``iosMain/Timestamp.ios.kt``:
```kt
package com.example.shared

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimestamp(): Long {
    val seconds = NSDate().timeIntervalSince1970
    return (seconds * 1000).toLong()
}
```

`timeIntervalSince1970` returns seconds as a `Double`, and `System.currentTimeMillis()` returns milliseconds, so on iOS I multiply by 1000 and convert to `Long`. This way both platforms return the same value.

The project built for both targets (`./gradlew :shared:assembleDebug` and `./gradlew :shared:compileKotlinIosArm64`) with no "has no actual declaration" errors, so the compiler found an `actual` for every platform.

## Question 12
`Context`: The lecture adds a third target, `js()`, to the existing `jvm(); iosArm64()` example to show that Kotlin creates a matching source set automatically for any target you declare.

`Task`: In your build.gradle.kts, add `js(IR) { browser() }` (or the JS target syntax your Kotlin version supports) alongside your existing `jvm()` and `iosArm64()` targets. Re-sync in Android Studio and verify in the Project view that a new `jsMain` (and `jsTest`) source set now appears alongside the existing ones, with no manual wiring required.

`answer`:

``build.gradle.kts``:
```kt
kotlin {
    jvm()
    iosArm64()
    js {
        browser()
    }
}
```

In my Kotlin version `js { browser() }` works without `IR`, because IR is now the only JS compiler.

After re-sync, new `jsMain` and `jsTest` source sets appeared next to `commonMain`, `jvmMain` and `iosArm64Main`. I did not wire anything manually. Declaring the target attached a third label to the common code, and Kotlin created the matching source sets by itself.

## Question 13
`Context`: The lecture's testing example pairs a `Greeting` class in commonMain with a `GreetingTest` in commonTest, using `kotlin.test.Test` and `assertEquals` to check its output.

`Task`: In commonMain, add a simple `Greeting` class with a function `greet(): String` that returns a fixed string. In commonTest, write a `GreetingTest` class with a `@Test`-annotated function that calls `assertEquals` to check the expected output, following the lecture's Main/Test pairing convention. Run the test from Android Studio and confirm it passes.

`answer`:

``commonMain/Greeting.kt``:
```kt
package com.example.shared

class Greeting {
    fun greet(): String {
        return "Hello, Kotlin Multiplatform!"
    }
}
```

``commonTest/GreetingTest.kt``:
```kt
package com.example.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingTest {

    @Test
    fun testGreeting() {
        assertEquals("Hello, Kotlin Multiplatform!", Greeting().greet())
    }
}
```

For `kotlin.test` to work, I added `implementation(kotlin("test"))` to `commonTest.dependencies` in `build.gradle.kts`.

I ran the test with the green arrow next to `GreetingTest`, and it passed. `Greeting` is in `commonMain` and `GreetingTest` is in the paired `commonTest`, so this follows the Main/Test pairing from the lecture. Because `commonTest` compiles to every declared target, the same test can run on every platform.
