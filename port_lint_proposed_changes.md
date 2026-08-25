# port-lint Proposed Changes

**Generated:** 2026-08-24
**Source:** tmp/globset
**Target:** src

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `commonMain/kotlin/io/github/kotlinmania/globset/Glob.kt` | `// port-lint: source glob.rs` | `// port-lint: source glob.rs` | `glob.rs` | `port-lint provenance header matched only after fallback normalization: 'glob.rs' vs expected 'glob.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/globset/GlobTests.kt` | `// port-lint: tests glob.rs` | `// port-lint: tests glob.rs` | `glob.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:glob.rs' vs expected 'glob.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/globset/Lib.kt` | `// port-lint: source lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'lib.rs' vs expected 'lib.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/globset/LibTests.kt` | `// port-lint: tests lib.rs` | `// port-lint: tests lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:lib.rs' vs expected 'lib.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/globset/SerdeImpl.kt` | `// port-lint: source serde_impl.rs` | `// port-lint: source serde_impl.rs` | `serde_impl.rs` | `port-lint provenance header matched only after fallback normalization: 'serde_impl.rs' vs expected 'serde_impl.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/globset/SerdeImplTests.kt` | `// port-lint: tests serde_impl.rs` | `// port-lint: tests serde_impl.rs` | `serde_impl.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:serde_impl.rs' vs expected 'serde_impl.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/globset/Fnv.kt` | `// port-lint: source fnv.rs` | `// port-lint: source fnv.rs` | `fnv.rs` | `port-lint provenance header matched only after fallback normalization: 'fnv.rs' vs expected 'fnv.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/globset/Pathutil.kt` | `// port-lint: source pathutil.rs` | `// port-lint: source pathutil.rs` | `pathutil.rs` | `port-lint provenance header matched only after fallback normalization: 'pathutil.rs' vs expected 'pathutil.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/globset/PathutilTests.kt` | `// port-lint: tests pathutil.rs` | `// port-lint: tests pathutil.rs` | `pathutil.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:pathutil.rs' vs expected 'pathutil.rs'` |
