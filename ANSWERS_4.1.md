# Practice 4.1 — Kotlin Multiplatform (30 Tasks)

Code for the hands-on tasks is in the `practice41`, `feature/auth`, `feature/profile`, `umbrella` and `shared` modules, plus `samples/` and `swift-package/`.

# Part A — What is Kotlin Multiplatform

## Task 1
`Task`: Contrast KMP with Flutter and React Native: state what Flutter renders through, what React Native talks to native views across, and what shared Kotlin code becomes on iOS.

`answer`:
- Flutter renders its own widget tree through its own engine (Skia/Impeller). It does not use native platform views.
- React Native talks to native views across a JS bridge (or the newer JSI). The JavaScript code runs in a JS engine.
- Shared Kotlin code is compiled by Kotlin/Native (LLVM) into a real native ARM64 binary, packaged as an iOS framework. There is no bridge and no VM.

So KMP shares only the logic as native code, and the UI can stay 100% native.

## Task 2
`Task`: Map each target to the way Kotlin reaches it: Android, iOS, browser (two options), desktop, server. Name the compiler technology used for iOS.

`answer`:

| Target | How Kotlin reaches it |
|---|---|
| Android | Kotlin/JVM — compiled to JVM bytecode, then to Android (DEX) |
| iOS | Kotlin/Native — compiled to a native binary / framework |
| Browser (option 1) | Kotlin/JS — compiled to JavaScript |
| Browser (option 2) | Kotlin/Wasm — compiled to WebAssembly |
| Desktop | Kotlin/JVM — runs on the JVM (Windows, macOS, Linux) |
| Server | Kotlin/JVM — runs on the JVM (for example with Ktor) |

The compiler technology for iOS is **Kotlin/Native**, which uses the **LLVM** backend to produce real ARM64 machine code.

## Task 3
`Task`: Classify each item as "shared (commonMain)" or "kept native" and justify in one line: Ktor API client, biometric login, input validation, push notifications, SQLDelight storage, deep-link navigation.

`answer`:

| Item | Where | Why |
|---|---|---|
| Ktor API client | shared (commonMain) | Ktor is a multiplatform HTTP client, the same requests work on every platform. |
| Biometric login | kept native | Face ID / fingerprint need deep platform-hardware APIs. |
| Input validation | shared (commonMain) | Pure logic, and it must behave identically on all platforms. |
| Push notifications | kept native | They rely on platform delivery services (FCM, APNs). |
| SQLDelight storage | shared (commonMain) | SQLDelight generates multiplatform code, only the driver is platform-specific. |
| Deep-link navigation | kept native | Navigation is part of the UI layer and uses platform intents / URL handling. |

## Task 4
`Task`: Complete the expect/actual setup so each platform returns its own Platform object. Write the missing declarations for Android, iOS and desktop (JVM).

`answer`:

``practice41/src/commonMain/.../Platform.kt``:
```kt
package com.example

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
```

``practice41/src/androidMain/.../Platform.android.kt``:
```kt
package com.example

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
```

``practice41/src/iosMain/.../Platform.ios.kt``:
```kt
package com.example

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
```

``practice41/src/jvmMain/.../Platform.jvm.kt``:
```kt
package com.example

class DesktopPlatform : Platform {
    override val name: String = "Desktop (Java ${System.getProperty("java.version")})"
}

actual fun getPlatform(): Platform = DesktopPlatform()
```

Every declared target (android, jvm, iosArm64, iosSimulatorArm64) has an `actual`. The iOS actual is in `iosMain`, so it covers both iOS targets.

## Task 5
`Task`: Find and fix the error in `expect fun currentTimeMillis(): Long { return 0L }`.

`answer`:

The error: an `expect` declaration **cannot have a body**. `expect` only describes the signature. The implementation must be in `actual` declarations in the platform source sets.

Fixed:

``commonMain/Time.kt``:
```kt
package com.example

expect fun currentTimeMillis(): Long
```

``androidMain/Time.android.kt`` and ``jvmMain/Time.jvm.kt``:
```kt
package com.example

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
```

``iosMain/Time.ios.kt``:
```kt
package com.example

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
```

If the function really should be the same on every platform, the other fix is to remove `expect` and keep a normal function with a body in `commonMain`.

## Task 6
`Task`: A project targets androidTarget(), jvm(), iosArm64() and iosSimulatorArm64(). An expect declaration in commonMain has actual implementations only in androidMain and jvmMain. State what fails and give two ways to fix it.

`answer`:

The compilations for **iosArm64** and **iosSimulatorArm64** fail. `commonMain` is compiled for every target, so every target needs an `actual`. The compiler reports that the `expect` declaration has no actual declaration for the native targets (and the IDE marks the `expect` red).

Two ways to fix it:
1. Add one `actual` in `iosMain`. Thanks to the default hierarchy, `iosMain` covers both `iosArm64` and `iosSimulatorArm64`, so one implementation is enough (this is what `practice41` does).
2. Add separate `actual` declarations in `iosArm64Main` and `iosSimulatorArm64Main`.

Another option: if iOS doesn't need this API, move the `expect` out of `commonMain` into a source set that is compiled only for android + jvm.

## Task 7
`Task`: List every source set the compiler merges when building for iosArm64. Explain why code placed in appleMain is visible to iOS targets but not to androidMain.

`answer`:

With the default hierarchy template, the compiler merges for `iosArm64`:
- `commonMain`
- `nativeMain`
- `appleMain`
- `iosMain`
- `iosArm64Main`

`appleMain` is labelled only with Apple targets (iOS, macOS, watchOS, tvOS). Kotlin collects every source set labelled for a given target and compiles them together. `iosArm64Main -> iosMain -> appleMain` is a `dependsOn` chain, so iOS sees `appleMain`. `androidMain` depends only on `commonMain`, and `appleMain` is never compiled for Android. Information only flows down the tree, from general to specific, so `androidMain` can't see `appleMain`. `appleMain` can also use Apple APIs (`platform.Foundation`) that don't exist on Android.

## Task 8
`Task`: Write a function in iosMain that returns UIDevice.currentDevice.systemVersion and explain why it cannot live in commonMain.

`answer`:

``practice41/src/iosMain/.../IosVersion.kt``:
```kt
package com.example

import platform.UIKit.UIDevice

fun iosSystemVersion(): String = UIDevice.currentDevice.systemVersion
```

It can't be in `commonMain` because `UIDevice` comes from Apple's UIKit (`platform.UIKit`), which exists only for iOS targets. `commonMain` is also compiled for Android and JVM, where UIKit doesn't exist, so the import would be unresolved. Kotlin blocks platform-only APIs in `commonMain`. If common code needs the version, use `expect fun osVersion(): String` in `commonMain` and make this function the iOS `actual`.

## Task 9
`Task`: Write the sourceSets block that adds ktor-client-core to commonMain, the OkHttp engine to androidMain and the Darwin engine to iosMain.

`answer`:

``practice41/build.gradle.kts``:
```kt
sourceSets {
    commonMain.dependencies {
        implementation("io.ktor:ktor-client-core:3.1.3")
    }
    androidMain.dependencies {
        implementation("io.ktor:ktor-client-okhttp:3.1.3")
    }
    iosMain.dependencies {
        implementation("io.ktor:ktor-client-darwin:3.1.3")
    }
}
```

The core API (`HttpClient`, requests) is shared, and each platform gets its own engine. `HttpClient()` without arguments picks the engine found on the platform. Usage is in `practice41/src/commonMain/.../ApiClient.kt`.

## Task 10
`Task`: State the difference between Kotlin Multiplatform and Compose Multiplatform. Describe the module layout for an app that shares logic with KMP but keeps native UI. State which of the two can be used without the other.

`answer`:

- **Kotlin Multiplatform** shares **business logic**: networking, storage, validation, state. The UI stays native.
- **Compose Multiplatform** is a declarative UI framework from JetBrains built on top of KMP. It additionally shares the **UI layer**.

Module layout for shared logic + native UI:
```
project/
├── shared/      KMP module: commonMain, androidMain, iosMain (logic only)
├── androidApp/  Android app, Jetpack Compose UI, implementation(project(":shared"))
└── iosApp/      Xcode project, SwiftUI UI, uses the Shared.framework built from :shared
```

**KMP can be used without Compose Multiplatform** (exactly this layout). Compose Multiplatform can't be used without KMP, because it is built on KMP's targets, source sets and compilations.

## Task 11
`Task`: Name the stability level of: iOS, Kotlin/Wasm, expect/actual classes, Compose Multiplatform for web. Decide for each whether you would ship it in a production app today and justify.

`answer`:

| Item | Stability | Ship to production? |
|---|---|---|
| iOS target | Stable | Yes. Stable and battle-tested, used in many production apps. |
| Kotlin/Wasm | Beta | Only with care. Still maturing, APIs and tooling can change. OK for a non-critical feature, not for a core one. |
| expect/actual classes | Beta | Yes, but carefully. `expect`/`actual` functions and properties are stable. For classes, prefer an interface + `expect fun` factory, or accept possible changes. |
| Compose Multiplatform for web | Beta | Not for a core production product yet. Use it for internal tools or experiments. |

Beta means it works, but APIs and tooling may still have breaking changes, so the engineering risk is higher.

## Task 12
`Task`: Write a commonTest test for fun isValidEmail(text: String): Boolean using kotlin.test (one valid and one invalid case). Name the Gradle tasks that run it on the JVM and on the iOS simulator.

`answer`:

``practice41/src/commonMain/.../EmailValidation.kt``:
```kt
package com.example

private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun isValidEmail(text: String): Boolean = emailRegex.matches(text.trim())
```

``practice41/src/commonTest/.../EmailValidationTest.kt``:
```kt
package com.example

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidationTest {
    @Test
    fun validEmail() {
        assertTrue(isValidEmail("student@ttpu.uz"))
    }

    @Test
    fun invalidEmail() {
        assertFalse(isValidEmail("student-at-ttpu.uz"))
    }
}
```

`commonTest.dependencies { implementation(kotlin("test")) }` is in `practice41/build.gradle.kts`.

Gradle tasks:
- JVM: `./gradlew :practice41:jvmTest`
- iOS simulator: `./gradlew :practice41:iosSimulatorArm64Test` (needs macOS with Xcode)
- All targets at once: `./gradlew :practice41:allTests`

## Task 13
`Task`: List two limitations of KMP in 2026 given in the lecture (Swift interop, binary size, team skills). For each, propose one mitigation for a team starting a new project.

`answer`:

1. **Swift interop.** Kotlin is exposed to Swift through an Objective-C header. Sealed classes aren't exhaustive enums, generics are limited, and `suspend` functions and `Flow` are awkward to call from Swift.
   - Mitigation: design an iOS-friendly public API (simple classes, no complex generics), and use a tool like SKIE (or Swift export when it is stable) to get Swift enums and async/await.
2. **Team skills.** iOS developers have to learn Kotlin, Gradle and Android Studio, and debugging Kotlin from Xcode is harder.
   - Mitigation: start small (share only validation and models first), use remote distribution so iOS developers who don't change shared code only consume a framework, and pair Android and iOS developers on the shared module.

Binary size is the third limitation: the Kotlin/Native runtime adds a few MB to the iOS app. Mitigation: one static umbrella framework, export only the needed modules, and always measure the release build.

## Task 14
`Task`: Write a LoginValidator class in commonMain (email and password rules) and show how it is consumed from an Android ViewModel and from a SwiftUI view model.

`answer`:

``practice41/src/commonMain/.../LoginValidator.kt``:
```kt
package com.example

data class LoginValidationResult(
    val emailError: String?,
    val passwordError: String?
) {
    val isValid: Boolean
        get() = emailError == null && passwordError == null
}

class LoginValidator {
    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !isValidEmail(email) -> "Invalid email"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.length < 8 -> "Password must be at least 8 characters"
        password.none { it.isDigit() } -> "Password must contain a digit"
        password.none { it.isUpperCase() } -> "Password must contain an uppercase letter"
        else -> null
    }

    fun validate(email: String, password: String): LoginValidationResult =
        LoginValidationResult(
            emailError = validateEmail(email),
            passwordError = validatePassword(password)
        )
}
```

``samples/android/LoginViewModel.kt`` (Android ViewModel):
```kt
class LoginViewModel(
    private val validator: LoginValidator = LoginValidator()
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(email: String) = update(email, _state.value.password)

    fun onPasswordChange(password: String) = update(_state.value.email, password)

    private fun update(email: String, password: String) {
        val result = validator.validate(email, password)
        _state.value = LoginUiState(
            email = email,
            password = password,
            emailError = result.emailError,
            passwordError = result.passwordError,
            canSubmit = result.isValid
        )
    }
}
```

``samples/ios/LoginViewModel.swift`` (SwiftUI view model):
```swift
import SwiftUI
import Shared

final class LoginViewModel: ObservableObject {
    private let validator = LoginValidator()

    @Published var email = "" { didSet { validate() } }
    @Published var password = "" { didSet { validate() } }
    @Published private(set) var emailError: String?
    @Published private(set) var passwordError: String?
    @Published private(set) var canSubmit = false

    private func validate() {
        let result = validator.validate(email: email, password: password)
        emailError = result.emailError
        passwordError = result.passwordError
        canSubmit = result.isValid
    }
}
```

The rules are written once, in Kotlin. Android calls them from a Jetpack ViewModel. iOS imports the `Shared` framework and calls the same class from Swift, where Kotlin parameter names become Swift argument labels (`validate(email:password:)`). The tests are in `practice41/src/commonTest/.../LoginValidatorTest.kt`.

## Task 15
`Task`: An existing Android-only app needs an iOS version. Order these layers for migration into shared code and justify the first choice: data models, networking, local storage, business rules, UI.

`answer`:

1. **Data models**
2. **Business rules**
3. **Networking**
4. **Local storage**
5. **UI** (last, or keep it native: SwiftUI on iOS)

Why data models first: they are plain Kotlin classes with no platform dependencies, so moving them to `commonMain` is easy and low-risk. Every other layer (business rules, networking, storage) uses the models, so they have to be shared before anything else can be. Business rules come next because they are pure logic and give the biggest win (identical behaviour on both platforms). Networking and storage need multiplatform libraries (Ktor, SQLDelight), so they take more work. UI is last because KMP doesn't require sharing it.

# Part B — KMP Project Configuration

## Task 16
`Task`: Compare a single shared module with several shared modules: one advantage and one drawback each. State the signal that tells a team to split.

`answer`:

| | Advantage | Drawback |
|---|---|---|
| Single shared module | Simple design, low cognitive load, a great starting point. | Compilation time grows with the module, and you can't depend on only part of the shared code. |
| Several shared modules | Separation of concerns and better scalability as the app grows. | More complicated setup (umbrella framework) and more involved dependency management. |

Signal to split: the shared module has grown so big that **build times are slow**, features become tangled, or some consumer needs **only part** of the shared code. Then it is time for feature modules + an umbrella module.

## Task 17
`Task`: Write the settings.gradle.kts includes for a project with a :shared module, an Android app and a desktop app, and the dependency line each app module uses to consume :shared.

`answer`:

``settings.gradle.kts``:
```kt
rootProject.name = "MyApp"

include(":shared")
include(":androidApp")
include(":desktopApp")
```

``androidApp/build.gradle.kts``:
```kt
dependencies {
    implementation(project(":shared"))
}
```

``desktopApp/build.gradle.kts`` (a JVM app):
```kt
dependencies {
    implementation(project(":shared"))
}
```

For the desktop app, `:shared` must declare a `jvm()` (desktop) target. Gradle then picks the JVM variant for the desktop app and the Android variant for the Android app, from the same dependency line.

## Task 18
`Task`: Two feature modules that both depend on the same library are exposed as two separate iOS frameworks. Explain what ends up inside the app and describe the concrete failure with shared state.

`answer`:

Each framework bundles **all of its dependencies inside it**. So the app contains two frameworks, and each has its **own copy** of the common library (and its own copy of the Kotlin runtime). Kotlin also treats the classes in the two copies as different, incompatible types.

Concrete failure: in this repo, `feature/auth` has `object SessionStore { var token }`, and `feature/profile` reads it. If they were two separate frameworks, `AuthFramework` would write the token into its own `SessionStore`, and `ProfileFramework` would read **another** `SessionStore` where the token is still `null`. On iOS the user logs in, and the profile screen still shows "Guest". On Android everything works, because there is only one copy. So the bug only shows up on iOS.

## Task 19
`Task`: Write the build.gradle.kts of an umbrella module that depends on :feature:auth and :feature:profile and produces one static iOS framework named Shared exporting both.

`answer`:

``umbrella/build.gradle.kts``:
```kt
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
}

kotlin {
    val xcFramework = XCFramework("Shared")

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":feature:auth"))
            export(project(":feature:profile"))
            xcFramework.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":feature:auth"))
            api(project(":feature:profile"))
        }
    }
}
```

Important points:
- `baseName = "Shared"` and `isStatic = true` give one static framework named `Shared`.
- The features are added with `api(...)`, not `implementation(...)`, and also with `export(...)`. Only then are their public types visible from Swift.
- The `XCFramework("Shared")` part is for Task 25.

The feature modules are `feature/auth` and `feature/profile`, included as `:feature:auth` and `:feature:profile` in `settings.gradle.kts`.

## Task 20
`Task`: Swift cannot see the types from :feature:auth. Find and fix the cause.

`answer`:

The cause: `:feature:auth` is added with `implementation` and is **not exported** from the framework. Kotlin/Native puts only the module's own public API in the framework header. Dependencies are compiled in, but their types are hidden from Swift unless they are exported. `export()` also requires the dependency to be `api`.

Fixed:
```kt
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "Shared"
            export(project(":feature:auth"))
        }
    }
    sourceSets.commonMain.dependencies {
        api(project(":feature:auth"))
    }
}
```

Two changes: `implementation` -> `api`, and `export(project(":feature:auth"))` inside `binaries.framework {}`.

## Task 21
`Task`: Explain why a Kotlin dependency cannot be shared across two separately built iOS frameworks, and why one umbrella framework is the only way to guarantee a single copy.

`answer`:

Kotlin/Native produces a closed, final binary for each framework. During the build, everything the framework needs (its dependencies and the Kotlin runtime) is compiled in, and the binary is **trimmed to exactly what that one build needs**. There is no mechanism to link a Kotlin dependency dynamically between two separately built Kotlin frameworks. Each build has its own version of the dependency's code, and the two can't agree on one copy.

An umbrella framework compiles all shared modules **in one build** into one artifact. Every dependency is then included exactly once, with one consistent code structure and one runtime, and all modules share the same objects. Since there is no other way to link Kotlin code across frameworks, one umbrella framework is the only way to guarantee a single copy.

## Task 22
`Task`: For a monorepo, a two-repository layout (Android + shared | iOS) and a three-repository layout (Android | iOS | shared): state one advantage and one drawback of each, and how the shared module is versioned in each.

`answer`:

| Layout | Advantage | Drawback | Versioning of shared |
|---|---|---|---|
| Monorepo | Easy to set up with wizards, iOS developers see the shared code right next to their app. | iOS developers must set up Gradle and Android Studio, and it doesn't fit existing separate repositories. | Not versioned separately. Both apps use the shared code from source, in the same commit. |
| Two repositories (Android + shared \| iOS) | Xcode project stays separate, and iOS and Android can release separately. | Shared code feels "owned" by the Android team. | The shared module moves in lockstep with the Android app. Only the iOS framework is published with a version for the iOS repo. |
| Three repositories (Android \| iOS \| shared) | Each platform owns its repo, and there is version parity: both teams consume the same versioned artifact. | More publishing overhead, every change needs publish + version bump. | The shared code is versioned independently and published for **both** Android/JVM and iOS. |

## Task 23
`Task`: Three repositories: the shared code must also be published for Android/JVM, not only iOS. Give the two publishing options from the lecture and the trade-off between them.

`answer`:

1. **Publish each feature module separately.**
2. **Publish only the umbrella module.**

Trade-off:
- Separate feature modules: the Android app can depend on only the features it needs, and each feature can have its own version. But there are more artifacts to publish, version and keep compatible.
- Only the umbrella: one artifact and one version, simple to publish, and the same thing that iOS consumes. But Android always gets every feature, even unused ones, and you can't update one feature on its own.

## Task 24
`Task`: Configure maven-publish in the shared module to publish to mavenLocal() with group uz.ttpu.shared and version 1.0.0. Write the dependency line the Android repository uses to consume it.

`answer`:

``shared/build.gradle.kts``:
```kt
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

group = "uz.ttpu.shared"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}
```

`publishLibraryVariants("release")` is needed, because the Android target is not published by default. Publish with `./gradlew :shared:publishToMavenLocal`, and it goes to `~/.m2/repository/uz/ttpu/shared/`.

The Android repository adds `mavenLocal()` to its repositories and consumes it with:
```kt
dependencies {
    implementation("uz.ttpu.shared:shared:1.0.0")
}
```

The artifact id is the module name (`shared`). Gradle module metadata picks the Android variant (`shared-android`) automatically.

## Task 25
`Task`: Configure Gradle to build an XCFramework named Shared for iosArm64 and iosSimulatorArm64, and write the .binaryTarget entry of a Swift package that consumes it from a URL. Explain why a checksum is required.

`answer`:

Gradle (in ``umbrella/build.gradle.kts``):
```kt
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

kotlin {
    val xcFramework = XCFramework("Shared")

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            xcFramework.add(this)
        }
    }
}
```

Build with `./gradlew :umbrella:assembleSharedReleaseXCFramework` (on macOS). Zip the result and upload it, for example to a GitHub release.

``swift-package/Package.swift``:
```swift
.binaryTarget(
    name: "Shared",
    url: "https://github.com/Otabek228101/practice3/releases/download/1.0.0/Shared.xcframework.zip",
    checksum: "PASTE_RESULT_OF_swift_package_compute-checksum_HERE"
)
```

The checksum comes from `swift package compute-checksum Shared.xcframework.zip`.

Why a checksum is required: SwiftPM downloads a prebuilt binary from a remote URL, so it has to make sure the file is exactly the one the package author published. The SHA-256 checksum protects against a corrupted download and against the file being replaced on the server (a supply-chain attack). It also makes builds reproducible: a version always means the same binary. SwiftPM refuses remote binary targets without a checksum.

## Task 26
`Task`: Compare local (source) and remote (artifact) distribution on four axes: iOS developer onboarding, iteration speed, versioning overhead, who can edit shared code.

`answer`:

| Axis | Local (source) | Remote (artifact) |
|---|---|---|
| iOS developer onboarding | Hard: everyone needs a full dev environment, iOS devs learn Gradle and Android Studio. | Easy: iOS devs never touch Kotlin or Gradle, they consume a framework like any other dependency. |
| Iteration speed | Fast: the iOS build compiles the framework itself, every change is visible on the next build. | Slow: every change means publish -> version -> rebuild. |
| Versioning overhead | None: no version numbers, no publish step. | Every change needs a new version and a publish. |
| Who can edit shared code | Both Android and iOS developers, it is a shared responsibility. | Mostly the Kotlin/Android side, iOS contributions taper off over time. |

## Task 27
`Task`: Describe the hybrid workflow step by step for an iOS developer who must change shared Kotlin code: default setup, switching to a local dependency, verifying the change, publishing, switching back. State the correct order of publish and app update.

`answer`:

1. **Default setup:** the iOS app consumes the published, versioned framework remotely, for example `Shared` 1.0.0 through Swift Package Manager.
2. **Switch to local:** to build a new feature, the developer points the iOS build at a local dependency on the framework (direct framework integration, or with CocoaPods by changing an environment variable), built from the local Kotlin sources.
3. **Verify:** they change the Kotlin code, rebuild the iOS app, and see the change on the next build. They debug it right away, with the same fast loop as pure local distribution.
4. **Publish:** when the feature is ready, they publish the shared module as a new version (for example 1.1.0).
5. **Switch back:** they update the iOS app to depend on the new remote version 1.1.0 and remove the local dependency.

Correct order: **publish the shared module first, then update the app** to the new remote version. If the app is updated first, it points to a version that doesn't exist yet, and the build breaks for everyone else.

## Task 28
`Task`: Two apps need shared libraries: App A uses logging and networking, App B uses networking and analytics. Draw the repository graph (text) and state why each app gets its own umbrella repository.

`answer`:

```
 logging-lib repo      networking-lib repo      analytics-lib repo
   (own version)          (own version)            (own version)
        │                   │        │                   │
        └────────┬──────────┘        └─────────┬─────────┘
                 ▼                             ▼
       App A umbrella repo            App B umbrella repo
      (logging + networking)       (networking + analytics)
                 │                             │
                 ▼                             ▼
     App A (Android + iOS)          App B (Android + iOS)
```

Every library has its own repository and version and is published for Android/JVM and iOS.

Why each app has its own umbrella: iOS needs **one** framework so each dependency exists only once. The two apps use **different subsets** of libraries, so one shared umbrella would force App A to ship analytics and App B to ship logging, which they don't use (bigger binary, extra dependencies). A separate umbrella for each app pulls in exactly the libraries that app needs, and still gives it one framework with a single copy of `networking`.

## Task 29
`Task`: Pick a configuration (modules, repositories, distribution) for each case and give one reason.

`answer`:

| Case | Modules | Repositories | Distribution | Reason |
|---|---|---|---|---|
| Solo developer, small app | Single shared module | Monorepo | Local | One person edits everything, so the simplest setup and the fastest loop, with no publishing. |
| Four engineers releasing Android and iOS together | Single module (split into features + umbrella later as it grows) | Monorepo | Local | Same release cycle, so no need for separate versions, and everyone can edit shared code with a fast loop. |
| Separate Android and iOS teams, independent release cycles | Several feature modules + umbrella | Three repositories (Android \| iOS \| shared) | Remote (with the hybrid workflow for people changing shared code) | Independent releases need independently versioned artifacts, and the iOS team can consume them without learning Gradle. |

## Task 30
`Task`: A single :shared module has grown to 40k lines with slow builds. Describe, step by step, how to split it into feature modules plus an umbrella module without changing the API the iOS app uses.

`answer`:

1. **Map the code.** Find feature boundaries (auth, profile, payments...) and the common core (models, networking, utils) that many features use.
2. **Create a `:core` module** and move the shared base code there first, because every feature will depend on it.
3. **Create feature modules** (`:feature:auth`, `:feature:profile`...), one at a time. Move the code, keeping the **same package names and class names**, so the Kotlin API doesn't change. Each feature depends on `:core` with `api(...)`.
4. **Turn the old `:shared` into the umbrella** (or create `:umbrella` with the same framework settings). It depends on all features with `api(project(...))`.
5. **Keep the same framework config:** `baseName = "Shared"`, the same `isStatic`, and `export(...)` for every feature and `:core` whose types Swift uses. Then the iOS app still does `import Shared` and sees the same types with the same names.
6. **Android** can now depend on individual feature modules (or on the umbrella).
7. **Verify after every step:** run `allTests`, build the iOS framework, and compare the generated `Shared.h` header with the old one. It must contain the same classes and functions, so the Swift code compiles without changes.
8. **Result:** Gradle compiles only changed modules and builds features in parallel, so builds get faster, while iOS still gets one `Shared` framework with a single copy of every dependency.
