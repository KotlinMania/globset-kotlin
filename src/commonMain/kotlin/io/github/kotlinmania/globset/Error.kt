// port-lint: source src/lib.rs
package io.github.kotlinmania.globset

/**
 * Represents an error that can occur when parsing a glob pattern.
 *
 * Parceled from `lib.rs` together with [ErrorKind]; see `Lib.kt` for the
 * lib.rs translation ledger.
 */
data class Error(
    /** The original glob provided by the caller. */
    internal val globValue: String?,
    /** The kind of error. */
    internal val kindValue: ErrorKind,
) {
    /** Return the glob that caused this error, if one exists. */
    fun glob(): String? = globValue

    /** Return the kind of this error. */
    fun kind(): ErrorKind = kindValue

    override fun toString(): String = when (globValue) {
        null -> kindValue.toString()
        else -> "error parsing glob '$globValue': $kindValue"
    }
}

/**
 * The kind of error that can occur when parsing a glob pattern.
 *
 * The upstream Rust enum is marked `non_exhaustive`.
 */
sealed class ErrorKind {
    /**
     * **DEPRECATED**.
     *
     * This error used to occur for consistency with git's glob specification,
     * but the specification now accepts all uses of `**`. When `**` does not
     * appear adjacent to a path separator or at the beginning/end of a glob,
     * it is now treated as two consecutive `*` patterns. As such, this error
     * is no longer used.
     */
    object InvalidRecursive : ErrorKind()

    /** Occurs when a character class (e.g., `[abc]`) is not closed. */
    object UnclosedClass : ErrorKind()

    /**
     * Occurs when a range in a character (e.g., `[a-z]`) is invalid. For
     * example, if the range starts with a lexicographically larger character
     * than it ends with.
     */
    data class InvalidRange(val start: Char, val end: Char) : ErrorKind()

    /** Occurs when a `}` is found without a matching `{`. */
    object UnopenedAlternates : ErrorKind()

    /** Occurs when a `{` is found without a matching `}`. */
    object UnclosedAlternates : ErrorKind()

    /**
     * **DEPRECATED**.
     *
     * This error used to occur when an alternating group was nested inside
     * another alternating group, e.g., `{{a,b},{c,d}}`. However, this is now
     * supported and as such this error cannot occur.
     */
    object NestedAlternates : ErrorKind()

    /** Occurs when an unescaped `\` is found at the end of a glob. */
    object DanglingEscape : ErrorKind()

    /** An error associated with parsing or compiling a regex. */
    data class Regex(val message: String) : ErrorKind()

    internal fun description(): String = when (this) {
        InvalidRecursive -> "invalid use of **; must be one path component"
        UnclosedClass -> "unclosed character class; missing ']'"
        is InvalidRange -> "invalid character range"
        UnopenedAlternates ->
            "unopened alternate group; missing '{' " +
                "(maybe escape '}' with '[}]'?)"
        UnclosedAlternates ->
            "unclosed alternate group; missing '}' " +
                "(maybe escape '{' with '[{]'?)"
        NestedAlternates -> "nested alternate groups are not allowed"
        DanglingEscape -> "dangling '\\'"
        is Regex -> message
    }

    override fun toString(): String = when (this) {
        InvalidRecursive,
        UnclosedClass,
        UnopenedAlternates,
        UnclosedAlternates,
        NestedAlternates,
        DanglingEscape,
        is Regex -> description()
        is InvalidRange -> "invalid range; '$start' > '$end'"
    }
}
