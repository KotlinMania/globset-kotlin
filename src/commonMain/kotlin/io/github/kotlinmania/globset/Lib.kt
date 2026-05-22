// port-lint: ignore
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
 *
 * Items still pending port (blocked on `src/glob.rs`, which is itself
 * blocked on `regex-automata-kotlin` / `aho-corasick-kotlin` / `bstr-kotlin`
 * which do not yet exist in this workspace):
 *   - `new_regex`             function
 *   - `new_regex_set`         function
 *   - `GlobSet`               struct
 *   - `GlobSetBuilder`        struct
 *   - `Candidate`             struct
 *   - `GlobSetMatchStrategy`  enum
 *   - `LiteralStrategy`       struct
 *   - `BasenameLiteralStrategy` struct
 *   - `ExtensionStrategy`     struct
 *   - `PrefixStrategy`        struct
 *   - `SuffixStrategy`        struct
 *   - `RequiredExtensionStrategy` struct
 *   - `RegexSetStrategy`      struct
 *   - `MultiStrategyBuilder`  struct
 *   - `RequiredExtensionStrategyBuilder` struct
 *   - remaining `mod tests`   (require Glob / GlobSet / GlobSetBuilder)
 */
