// port-lint: source src/lib.rs
package io.github.kotlinmania.globset

/**
 * Escape meta-characters within the given glob pattern.
 *
 * The escaping works by surrounding meta-characters with brackets. For
 * example, `*` becomes `[*]`.
 *
 * # Example
 *
 * ```
 * assertEquals("foo[*]bar", escape("foo*bar"))
 * assertEquals("foo[?]bar", escape("foo?bar"))
 * assertEquals("foo[[]bar", escape("foo[bar"))
 * assertEquals("foo[]]bar", escape("foo]bar"))
 * assertEquals("foo[{]bar", escape("foo{bar"))
 * assertEquals("foo[}]bar", escape("foo}bar"))
 * ```
 *
 * Parceled from `lib.rs`; see [Lib.kt] for the lib.rs translation ledger.
 */
fun escape(s: String): String {
    val escaped = StringBuilder(s.length)
    for (c in s) {
        when (c) {
            // note that ! does not need escaping because it is only special
            // inside brackets
            '?', '*', '[', ']', '{', '}' -> {
                escaped.append('[')
                escaped.append(c)
                escaped.append(']')
            }
            else -> {
                escaped.append(c)
            }
        }
    }
    return escaped.toString()
}
