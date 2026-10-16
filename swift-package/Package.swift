// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "Shared",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(name: "Shared", targets: ["Shared"])
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            url: "https://github.com/Otabek228101/practice3/releases/download/1.0.0/Shared.xcframework.zip",
            checksum: "PASTE_RESULT_OF_swift_package_compute-checksum_HERE"
        )
    ]
)
