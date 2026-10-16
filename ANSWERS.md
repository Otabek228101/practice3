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

# Section 3 - KMP Advanced Project Structure 

## Question 14
`Context`: A developer claims that `dependsOn` is "basically the same thing" as putting a module in the `dependencies {}` block, just for source sets instead of libraries.

`Task`: Explain why this is wrong. List all four consequences the lecture says hold simultaneously when `A.dependsOn(B)` is declared, and state clearly which kind of relation (visibility/actualization, or packaging/build) `dependsOn` represents.

`answer`:

The dependencies {} block is a normal Gradle dependency. It brings in published libraries by coordinates ("group:artifact:version") or other projects by path, with scopes like implementation/api. This is a packaging/build relation.
dependsOn is declared outside dependencies {} on purpose. It can’t be used for a library or for another Gradle project’s source set. It only links source sets inside the same project into a hierarchy.
dependsOn relations are also preserved at compile time: the compiler checks them directly. They are not just a way of organizing the project.
When A.dependsOn(B) is declared, all four of these hold at the same time:

- A observes B’s API, including internal declarations.
- A can provide actual implementations for B’s expect declarations. 
- B compiles to every target A compiles to, plus its own targets. 
- A inherits all of B’s regular dependencies.

Example: iosArm64Main.dependsOn(iosMain) means iosArm64Main depends on iosMain for its common declarations.

## Question 15
`Context`: A student tries to write `commonTest.dependsOn(commonMain)` to "share" test code with production code, and separately tries to call `dependsOn` directly on a library's own commonMain.

`Task`: Explain why both of these attempts are mistakes, referencing the lecture's explanation of what `dependsOn` is reserved for, and what the `dependencies {}` block is for instead.

`answer`:
1. commonTest.dependsOn(commonMain)

Main and test code are kept separate on purpose. Every target automatically gets two compilations, main and test. This is the same Main/Test split that the source sets have.
The two also live in different hierarchies. *Test source sets “align on their own separate tree”*. For example, upgrading a library in iosTest never affects production code.
commonTest doesn’t need this link to see production code. The plugin already sets up the main/test relationship, so test code can see commonMain.
A manual dependsOn would mix the test and production hierarchies. It would also pass all four dependsOn consequences to test code, including inheriting dependencies and providing actuals for production expects. That breaks the separation the build relies on.
To reuse test helpers, put them in commonTest itself. Don’t try to “share” test code with production through dependsOn.

2. calling dependsOn on a library’s own commonMain

DependsOn “is declared outside dependencies {} on purpose - it can’t be used for a library or another Gradle project’s source set.” It only links source sets inside your own project.
A published library already ships its own source-set structure. For example, kotlinx-coroutines has its own commonMain, concurrentMain and jsMain. You don’t wire into that tree yourself.

## Question 16
`Context`: The lecture's flagship example: a project targets JVM, JS, and Linux (linuxX64), and the team wants to share code between just JVM and JS — a pairing Kotlin has no built-in source set for.

`Task`: In Android Studio, create (or reuse) a KMP module declaring `jvm()`, `js(IR) { browser() }`, and `linuxX64()` as targets. In the `sourceSets {}` block, declare `val jvmAndJsMain by creating { }`, then wire it in with the three `dependsOn` calls from the lecture: `jvmAndJsMain.dependsOn(commonMain.get())`, `jvmMain.dependsOn(jvmAndJsMain)`, and `jsMain.dependsOn(jvmAndJsMain)`. Move one shared function into `jvmAndJsMain` and confirm the project still syncs and builds.

`answer`:

I created a new KMP module `hierarchy` and added it to `settings.gradle.kts` with `include(":hierarchy")`. The module declares three targets: `jvm()`, `js(IR) { browser() }` and `linuxX64()`.

Kotlin has no built-in source set for only JVM + JS, so I created one by hand and connected it with the three `dependsOn` calls from the lecture.

``hierarchy/build.gradle.kts``:
```kt
kotlin {
    jvm()
    js(IR) {
        browser()
    }
    linuxX64()

    sourceSets {
        val jvmAndJsMain by creating { }

        val jvmMain by getting
        val jsMain by getting

        jvmAndJsMain.dependsOn(commonMain.get())
        jvmMain.dependsOn(jvmAndJsMain)
        jsMain.dependsOn(jvmAndJsMain)
    }
}
```

Right after `by creating`, `jvmAndJsMain` is isolated: it has no `dependsOn` and no targets. The first call links it to `commonMain`. The other two calls make the existing `jvmMain` and `jsMain` depend on it, and that is what adds the JVM and JS targets to `jvmAndJsMain`. `linuxX64Main` is not connected to it, so it only sees `commonMain`.

``commonMain/Common.kt``:
```kt
package com.example

fun commonHello(): String = "Hello from commonMain"

internal fun commonSecret(): String = "internal from commonMain"
```

``jvmAndJsMain/JvmAndJs.kt``:
```kt
package com.example

fun sharedJvmAndJsGreeting(): String =
    "${commonHello()} + jvmAndJsMain (running on ${runtimeName()}, ${commonSecret()})"

expect fun runtimeName(): String
```

``jvmMain/Platform.jvm.kt``:
```kt
package com.example

actual fun runtimeName(): String = "JVM ${System.getProperty("java.version")}"

fun jvmGreeting(): String = "JVM says: ${sharedJvmAndJsGreeting()}"
```

``jsMain/Platform.js.kt``:
```kt
package com.example

actual fun runtimeName(): String = "JavaScript"

fun jsGreeting(): String = "JS says: ${sharedJvmAndJsGreeting()}"
```

``linuxX64Main/Platform.linux.kt``:
```kt
package com.example

fun linuxGreeting(): String = "Linux says: ${commonHello()}"
```

The shared function `sharedJvmAndJsGreeting()` lives in `jvmAndJsMain`. The code shows the `dependsOn` consequences in practice:
- `jvmAndJsMain` uses `commonHello()` and the `internal` function `commonSecret()` from `commonMain`, so it sees B's API including `internal`.
- `jvmAndJsMain` declares `expect fun runtimeName()`, and `jvmMain` and `jsMain` provide the `actual` implementations.
- `jvmMain` and `jsMain` both call the shared function. `linuxX64Main` can only use `commonMain` code, because `jvmAndJsMain` is not compiled for Linux.

The project syncs and `./gradlew :hierarchy:build` builds successfully. `./gradlew :hierarchy:jvmTest` passes, so the shared function from `jvmAndJsMain` works on the JVM. `./gradlew :hierarchy:printSourceSets` shows `jvmAndJsMain -> dependsOn: [commonMain]`, and `jvmMain` and `jsMain` list `jvmAndJsMain` as their parent.

## Question 17
`Context`: The lecture warns that writing any manual `dependsOn` call opts a project out of Kotlin's default hierarchy template, which otherwise creates useful intermediate source sets (like `appleMain`) for you automatically.

`Task`: Using the project you built in the previous question, add `iosArm64()` and `iosSimulatorArm64()` as two more targets. Before adding any manual `dependsOn` calls involving them, check the Project view for an auto-created `appleMain` (or `iosMain`) intermediate source set. Note whether it still appears after your earlier manual `dependsOn` wiring, and explain what you observe in terms of the default hierarchy template caveat from the lecture.

`answer`:

``hierarchy/build.gradle.kts``:
```kt
kotlin {
    jvm()
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
    }
}
```

After re-sync, Gradle created the leaf source sets `iosArm64Main` and `iosSimulatorArm64Main` (plus their `Test` pairs). But there is **no** `appleMain`, `iosMain` or `nativeMain`. `./gradlew :hierarchy:printSourceSets` shows that `iosArm64Main` and `iosSimulatorArm64Main` both depend directly on `commonMain`. Gradle also prints a warning that the Default Kotlin Hierarchy Template was not applied, because explicit `.dependsOn()` edges were configured.

This is exactly the caveat from the lecture. Normally the default hierarchy template builds the intermediate source sets for me (`nativeMain` -> `appleMain` -> `iosMain` -> `iosArm64Main` / `iosSimulatorArm64Main`). But my manual `dependsOn` calls from Question 16 opt the whole project out of that template. Kotlin does not mix my hand-written tree with the default one, so it does not infer the tree anymore. As the recap says: "New source sets need manual dependsOn; Kotlin won't infer the tree for you." If I want an `iosMain` shared by both iOS targets now, I have to create it with `by creating` and wire it with `dependsOn` myself, the same way as `jvmAndJsMain`.

## Question 18
`Context`: You declare `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")` once, inside `commonMain.dependencies {}`, in a project targeting android, iosArm64, and iosSimulatorArm64.

`Task`: Explain what "propagation" means here — do you also need to add this dependency separately to androidMain and iosMain? Then explain how a developer would scope the same dependency to iOS only, according to the lecture.

`answer`:

Propagation means that a dependency declared once on `commonMain` automatically reaches every source set that depends on `commonMain`, directly or transitively. This follows from the fourth `dependsOn` rule: A inherits all of B's regular dependencies.

So **no**, I don't need to add `kotlinx-coroutines-core` to `androidMain` or `iosMain` again. `androidMain`, `iosMain`, `iosArm64Main` and `iosSimulatorArm64Main` all sit below `commonMain` in the tree, so they all get coroutines 1.7.3 from that one declaration.

To scope the dependency to iOS only, I declare it on `iosMain` instead of `commonMain`:

```kt
sourceSets {
    iosMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    }
}
```

Now it propagates only to `iosArm64Main` and `iosSimulatorArm64Main`, and `commonMain` and `androidMain` don't see it. As the lecture says, the source set you pick is itself a scoping decision.

## Question 19
`Context`: The lecture shows a real IntelliJ screenshot where, inside a commonMain that compiles to android, iosArm64, and iosSimulatorArm64, `Dispatchers.Main` and `runBlocking` resolve fine, but `dispatcher.asExecutor()` is underlined red as unresolved.

`Task`: Explain the compatibility rule that decides this outcome, referencing the fact that a published KMP library (like kotlinx-coroutines-core) ships its own internal source sets (commonMain, concurrentMain, a JVM-only source set). State specifically which of the library's own source sets each of the three calls resolves from, and why exactly one of the three fails to compile.

`answer`:

A published KMP library is not one blob. It ships its own source-set structure. `kotlinx-coroutines-core:1.7.3` has its own `commonMain` (every target), `concurrentMain` (every target except JS), `jsMain` and a JVM-only source set. So my `commonMain -> library` dependency is only an intermediate step. Kotlin resolves it down to real source set -> source set edges.

The compatibility rule: **a library source set is visible to my source set only if it covers at least all of my source set's targets.**

My `commonMain` compiles to android + iosArm64 + iosSimulatorArm64:
- `Dispatchers.Main` comes from the library's `commonMain`. It covers every target, so it covers mine. It resolves.
- `runBlocking` comes from the library's `concurrentMain`. It covers every target except JS. I have no JS target, so it still covers all of mine. It resolves.
- `asExecutor()` comes from the library's JVM-only source set. It covers only JVM/Android, but not iosArm64 and iosSimulatorArm64. My `commonMain` must also compile for iOS, so this source set is not compatible and `asExecutor()` is red, unresolved.

To use `asExecutor()`, the call has to move to `androidMain` (or `jvmMain`), where the JVM-only source set does cover all targets.

## Question 20
`Context`: Your commonMain declares `kotlinx-coroutines-core:1.7.3`, but androidMain separately adds `androidx.navigation-compose:2.7.7`, which internally requires coroutines 1.8.0 or newer.

`Task`: Explain what Gradle does to resolve this version conflict, including what happens to commonMain's and iosMain's effective coroutines version as a result. Then explain why upgrading a dependency inside `iosTest` does NOT similarly affect the versions used in any `*Main` source set.

`answer`:

`commonMain` is compiled several times, once for every target. All those compilations must agree on **one** version of coroutines, otherwise the shared code could be compiled against different APIs on different platforms.

`navigation-compose:2.7.7` in `androidMain` needs coroutines 1.8.0+, so Gradle resolves the conflict by picking the higher version. It raises coroutines from 1.7.3 to **1.8.0** and aligns the whole `*Main` tree on it. So the effective version becomes 1.8.0 in `androidMain`, in `commonMain`, and also in `iosMain`, even though the iOS side never asked for 1.8.0. The version I wrote (1.7.3) is not the version that is actually used.

`*Test` source sets are aligned on their **own separate tree**. Version alignment happens inside the `*Main` tree and inside the `*Test` tree independently. So if I upgrade a dependency in `iosTest`, only the test source sets (and test compilations) get the new version. It never goes "up" into `iosMain`, `commonMain` or any other `*Main` source set, so test-only upgrades never touch production code.

## Question 21
`Context`: The lecture closes with the vocabulary shift from "source set" to "compilation," and shows the Gradle access pattern `kotlin { jvm { compilations.getByName("main") } }` for reaching a specific compilation.

`Task`: In your Android Studio project's build.gradle.kts, inside the `kotlin {}` block for your `jvm()` target, use `compilations.getByName("main")` to access the main compilation and print its name (e.g. via `println(this.compilations.getByName("main").name)` in a small Gradle task, or by inspecting it with the Gradle build in verbose/debug mode). Confirm you can also see a `test` compilation registered for the same target, and explain in one sentence how a compilation differs from a source set.

`answer`:

``hierarchy/build.gradle.kts``:
```kt
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
}
```

I ran `./gradlew :hierarchy:printJvmCompilations`. `compilations.getByName("main")` returned the main compilation and printed `main compilation: main`. The loop also shows a second compilation, `test`, registered for the same `jvm` target. This matches the lecture: every target automatically gets two compilations, `main` (production sources) and `test` (test sources). The `main` compilation contains `commonMain`, `jvmAndJsMain` and `jvmMain`, and the `test` compilation contains the test source sets (`commonTest`, `jvmTest`).

A source set is a group of source files with its own targets and dependencies, while a compilation is one actual compiler launch for one target that collects several source sets and compiles them together.

# Section 4 - Choosing a KMP Project Configuration 

## Question 22
`Context`: A small team is starting their first Kotlin Multiplatform project and isn't sure whether to create one shared module or split into several feature modules right away.

`Task`: State the lecture's pros and cons of a single shared module versus several shared modules, and explain, using the growing-app trade-off from the slides, at what point a team typically moves from one configuration to the other.

`answer`:

Single shared module:
- Pros: a simple design with just one module reduces cognitive load, no need to decide where functionality goes. Works great as a starting point.
- Cons: compilation time increases as the shared module grows. No separate features, and no way to depend on only part of the shared code.

Several shared modules (feature modules + umbrella module):
- Pros: separation of concerns for shared code. Better scalability as the app grows.
- Cons: more complicated setup, including umbrella framework configuration. More involved dependency management across modules.

For a small team starting their first KMP project, the lecture recommends starting with **one shared module**. The team moves to several modules when the app grows: when the shared module gets so big that compilation time becomes a problem, when features need to be separated, or when some code needs to depend on only part of the shared code. At that point they split the shared module into feature modules and add an umbrella module for iOS.

## Question 23
`Context`: A team has split their shared code into three feature modules and is now trying to generate iOS frameworks directly from each feature module separately, one framework per module.

`Task`: Explain, using the lecture's dependency-duplication reasoning, exactly why this approach breaks down on iOS, what an umbrella module is, and how it fixes the problem. Include the concrete failure mode the lecture describes (state not being shared through "the same" dependency).

`answer`:

Each KMP module compiled to its own framework bundles **all of its dependencies inside it**. Kotlin can't share a common dependency across separate builds, because each binary is trimmed to exactly what that one build needs. So if two feature modules both use the same dependency (for example, a shared `core` module or a library) and are exposed as two separate frameworks, that dependency is compiled into the iOS app **twice**, as two incompatible copies.

The concrete failure: state passed through "the same" dependency from two different modules is not actually connected. For example, if feature 1 saves a value into a shared singleton and feature 2 reads it, on iOS they talk to two different copies of that singleton, so feature 2 never sees the value. This leads to bugs that only show up on iOS, because on Android all modules use one copy.

An umbrella module is a module that depends on all the feature modules and is compiled into **one** iOS framework. The Android app can still depend on any or all feature modules directly, but the iOS app depends only on the umbrella framework. Because everything is compiled in one build, every dependency exists exactly once, with one consistent code structure, and the duplicate-state problem disappears. One framework is the only way to guarantee one copy.

## Question 24
`Context`: Your organization is deciding between a monorepo, a two-repository split (Android+shared | iOS), and a three-repository split (Android | iOS | shared).

`Task`: For each of the three repository layouts, state one advantage and one disadvantage from the lecture, and explain the specific versioning consequence that changes between the two-repository and three-repository setups (in particular, what has to happen to the shared modules in the three-repository case that doesn't in the two-repository case).

`answer`:

Monorepo (everything in one repository):
- Advantage: easy to set up with project wizards, and iOS developers can work with the shared code without hunting across repositories.
- Disadvantage: iOS developers need to set up unfamiliar tools (Gradle, Android Studio/IntelliJ), and it often doesn't fit apps whose code already lives in separate repositories.

Two repositories (Android + shared | iOS):
- Advantage: the Xcode project stays separate, so Android and iOS can be versioned separately.
- Disadvantage: the shared module's version is tied to the Android app (they move in lockstep), so the shared code feels "owned" by the Android team.

Three repositories (Android | iOS | shared):
- Advantage: each platform owns its repository, shared code is versioned independently, and both teams use the same versioned artifact, so there is version parity.
- Disadvantage: more publishing overhead. The shared modules must be published and versioned for every platform.

Versioning consequence: in the two-repository setup, the Android app uses the shared module directly from source in the same repository, so only the iOS framework needs to be published. In the three-repository setup, the shared code lives in its own repository, so the shared modules **must be published for Android/JVM too, not just for iOS**, either each feature module separately or just the umbrella module. Android then consumes the shared code as a versioned dependency, the same way iOS does.

## Question 25
`Context`: A 200-engineer company ships five different apps that all want to reuse the same logging, networking, and analytics logic, but no single app should be forced to pull in libraries it doesn't use.

`Task`: Using the "many repositories" configuration and the local-vs-remote distribution slides, design a configuration for this company: how many repositories for the shared libraries, how each app should consume only the libraries it needs, and whether local, remote, or the hybrid workflow best fits an organization this size. Justify each choice using the trade-offs given in the lecture.

`answer`:

Repositories for shared libraries: **three**, one each for logging, networking and analytics. This is the "many repositories" configuration: each library has its own repository and its own version, and is published independently for Android/JVM as well as iOS. Teams can update one library without releasing the others.

How each app consumes only what it needs: every app gets its **own umbrella repository** that pulls in only the libraries that app uses. For example, App A's umbrella includes logging + networking, and App B's umbrella includes all three. The Android part of each app can depend on the published libraries directly, and the iOS part depends on its app's umbrella framework. The umbrella is still required for iOS, so that each shared dependency exists only once in the app and state is not duplicated. Five apps means five umbrella repositories (plus the Android and iOS app code of each app).

Distribution: the **hybrid workflow**, with remote distribution as the default.
- Pure local distribution doesn't fit 200 engineers: everyone needs a full dev environment, iOS developers have to learn Gradle and Android Studio, and it gets harder to manage as more code is shared and the team grows.
- Remote distribution fits the size: libraries are published as versioned artifacts, and iOS developers who don't work on shared code never have to touch Kotlin, Gradle or Android Studio. But its cons are a slow loop (publish, version, rebuild) and hard debugging of Kotlin from iOS.
- The hybrid workflow fixes those cons: by default apps use the published remote versions. A developer actively changing a shared library switches to a local dependency, iterates and debugs with a fast loop, then publishes the new version and switches the app back to remote. In practice: Swift Package Manager for remote and direct framework integration for local, or CocoaPods with an environment variable to switch.
