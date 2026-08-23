// port-lint: source lib.rs
package io.github.kotlinmania.globset

/**
 * The globset library provides cross platform single glob and glob set matching.
 *
 * Glob set matching is the process of matching one or more glob patterns against
 * a single candidate path simultaneously, and returning all of the globs that
 * matched.
 *
 * Module structure:
 * - Error and ErrorKind types are in Error.kt
 * - Escape function is in Escape.kt
 * - Candidate type is in Candidate.kt
 * - Glob and GlobBuilder types are in Glob.kt
 * - GlobSet and GlobSetBuilder types are in GlobSet.kt
 */
private const val MODULE_LEDGER = true
