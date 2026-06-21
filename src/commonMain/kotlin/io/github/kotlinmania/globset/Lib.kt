package io.github.kotlinmania.globset

/*
 * Translation ledger for `src/lib.rs`.
 *
 * Upstream `src/lib.rs` is a large crate-root file with real implementation,
 * not just re-exports. Per the workspace "lib.rs parceled like mod.rs" rule,
 * its items are parceled into focused Kotlin files; each parceled file
 * carries `// port-lint: source src/lib.rs` so ast_distance can track
 * provenance. This ledger names where each upstream item lives in Kotlin.
 *
 * Items parceled out of `src/lib.rs`:
 *   - `Error` struct          -> Error.kt
 *   - `ErrorKind` enum        -> Error.kt
 *   - `escape` function       -> Escape.kt
 *   - `mod tests::escape`     -> EscapeTests.kt
 *   - `Candidate` struct      -> Candidate.kt
 *   - `mod tests::Candidate`  -> CandidateTests.kt
 */

private const val MODULE_LEDGER = true
