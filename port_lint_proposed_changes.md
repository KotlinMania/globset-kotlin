# port-lint Proposed Changes

**Generated:** 2026-08-26
**Source:** tmp/globset/src/lib.rs
**Target:** src/commonMain/kotlin/io/github/kotlinmania/globset/Lib.kt

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/globset/Lib.kt` | `// port-lint: source globset/src/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'globset/src/lib.rs' vs expected 'lib.rs'` |
