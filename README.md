# globset-kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fglobset--kotlin-blue.svg)](https://github.com/KotlinMania/globset-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/globset-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/globset-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/globset-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/globset-kotlin/actions)
[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)

**globset-kotlin** is a Kotlin Multiplatform library for matching file paths against Unix shell-style glob patterns. It provides efficient single glob and glob set matching across all Kotlin platforms.

## Features

- **Cross-platform**: Works on JVM, Android, iOS, macOS, Linux, Windows, JavaScript (browser & Node.js), and WebAssembly
- **Efficient glob sets**: Match a single path against multiple glob patterns simultaneously
- **Standard glob syntax**: Supports `*`, `?`, `[...]`, `{a,b}`, and `**` patterns
- **Case-insensitive matching**: Optional case-insensitive matching support
- **Literal matching**: Treat glob patterns as literal strings when needed
- **Path separator awareness**: Handles different path separators correctly across platforms

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.kotlinmania:globset-kotlin:0.1.2")
}
```

Or in your `build.gradle`:

```groovy
dependencies {
    implementation 'io.github.kotlinmania:globset-kotlin:0.1.2'
}
```

## Quick Start

### Single Glob Matching

```kotlin
import io.github.kotlinmania.globset.GlobBuilder

// Match a simple pattern
val glob = GlobBuilder("*.txt").build()
assert(glob.isMatch("file.txt"))
assert(!glob.isMatch("file.rs"))

// Case-insensitive matching
val caseInsensitive = GlobBuilder("*.TXT")
    .caseInsensitive(true)
    .build()
assert(caseInsensitive.isMatch("file.txt"))
```

### Glob Set Matching

Match multiple patterns efficiently:

```kotlin
import io.github.kotlinmania.globset.GlobSetBuilder

val set = GlobSetBuilder()
    .add("*.rs")
    .add("*.toml")
    .add("src/**/*.kt")
    .build()

// Check if any pattern matches
assert(set.isMatch("src/main.rs"))
assert(set.isMatch("Cargo.toml"))
assert(set.isMatch("src/lib/utils.kt"))
assert(!set.isMatch("README.md"))

// Get all matching patterns
val matches = set.matches("src/main.rs")
println("Matched patterns: $matches")
```

## Glob Syntax

globset-kotlin supports standard Unix shell glob patterns:

- **`?`** - Matches any single character except path separators
- **`*`** - Matches zero or more characters except path separators
- **`**`** - Matches zero or more directories (must be a complete path component)
- **`[abc]`** - Matches any character inside the brackets
- **`[!abc]`** or **`[^abc]`** - Matches any character *not* in the brackets
- **`[a-z]`** - Matches any character in the range
- **`{a,b,c}`** - Matches any of the comma-separated patterns

### Examples

```
*.txt        # Matches: file.txt, document.txt
src/*.kt     # Matches: src/Main.kt, src/Utils.kt
**/*.rs      # Matches: src/lib.rs, foo/bar/baz.rs
test_?.rs    # Matches: test_1.rs, test_a.rs
[abc]*.txt   # Matches: a_file.txt, book.txt
{foo,bar}/*  # Matches: foo/file.txt, bar/doc.rs
```

## Platform Support

globset-kotlin is a **Kotlin Multiplatform** library supporting:

- **JVM** (Java 21+)
- **Android** (API 24+)
- **iOS** (arm64, x64, simulatorArm64)
- **macOS** (arm64)
- **tvOS** (arm64, simulatorArm64)
- **watchOS** (arm32, arm64, deviceArm64, simulatorArm64)
- **Linux** (x64, arm64)
- **Windows** (mingw-x64)
- **JavaScript** (browser & Node.js)
- **WebAssembly** (wasm-js, wasm-wasi)
- **Android Native** (arm32, arm64, x86, x64)

## Building from Source

```bash
# Build for all platforms
./gradlew build

# Run tests
./gradlew test

# Build for a specific platform
./gradlew jvmTest
./gradlew macosArm64Test
./gradlew jsNodeTest
```

### Requirements

- JDK 21 or later
- Gradle 8.0+
- For iOS/macOS/tvOS/watchOS builds: macOS with Xcode
- For Windows builds: MinGW-w64

## Project Status

This is an **in-progress port** from the upstream Rust [`globset` crate](https://github.com/BurntSushi/ripgrep/tree/master/crates/globset) (part of ripgrep). The goal is feature parity with the upstream while providing a native Kotlin Multiplatform API.

Currently implemented:
- ✅ Core glob parsing and matching
- ✅ Glob sets with simultaneous pattern matching
- ✅ Case-insensitive matching
- ✅ Path separator normalization
- 🚧 Full regex-based glob compilation (in progress)
- 🚧 Complete test coverage (in progress)

See [PORT_REPORT.md](PORT_REPORT.md) for detailed porting status.

## Documentation

### API Documentation

API documentation is available on [Maven Central](https://central.sonatype.com/artifact/io.github.kotlinmania/globset-kotlin).

### Additional Resources

- [Porting Guidelines](AGENTS.md) - For contributors working on the port
- [Port Status](PORT_REPORT.md) - Detailed status of what's been ported
- [Swift Export Rollout](SWIFT_EXPORT_ROLLOUT.md) - iOS/macOS integration details

## Contributing

Contributions are welcome! This project follows a systematic porting approach:

1. Each Kotlin file carries a `// port-lint: source <path>` header linking to its upstream Rust source
2. We maintain structural parity with upstream for easier updates
3. Tests are ported alongside implementation

See [AGENTS.md](AGENTS.md) for detailed contribution guidelines.

## License

This project is released into the **public domain** under the [Unlicense](https://unlicense.org/).

See [LICENSE](LICENSE) for the full license text.

### Copyright & Acknowledgments

Original Rust implementation: Copyright (c) 2016-2024 Andrew Gallant and contributors
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project

This is a faithful port of the [`globset` crate](https://github.com/BurntSushi/ripgrep/tree/master/crates/globset) from [ripgrep](https://github.com/BurntSushi/ripgrep). All design credit belongs to Andrew Gallant (BurntSushi) and the ripgrep contributors. This Kotlin implementation preserves their excellent API design while bringing it to the Kotlin Multiplatform ecosystem.

## Related Projects

If you're working with file system operations in Kotlin, you might also be interested in:

- [ignore-kotlin](https://github.com/KotlinMania/ignore-kotlin) - `.gitignore`-style file filtering
- [regex-kotlin](https://github.com/KotlinMania/regex-kotlin) - Rust-style regex library for Kotlin

---

**Made with ❤️ by Sydney Renee and The Solace Project**
