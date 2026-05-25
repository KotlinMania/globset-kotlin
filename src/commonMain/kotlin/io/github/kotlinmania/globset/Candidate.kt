// port-lint: source src/lib.rs
package io.github.kotlinmania.globset

/**
 * A candidate path for matching.
 *
 * All glob matching in this crate operates on `Candidate` values. Constructing
 * candidates has a very small cost associated with it, so callers may find it
 * beneficial to amortize that cost when matching a single path against multiple
 * globs or sets of globs.
 */
public class Candidate internal constructor(
    internal val path: ByteArray,
    internal val basename: ByteArray,
    internal val ext: ByteArray,
) {
    public companion object {
        /**
         * Create a new candidate for matching from the given path.
         */
        public fun new(path: String): Candidate = fromBytes(path.encodeToByteArray())

        /**
         * Create a new candidate for matching from the given path as a sequence
         * of bytes.
         *
         * Generally speaking, this routine expects the bytes to be
         * _conventionally_ UTF-8. It is legal for the byte sequence to contain
         * invalid UTF-8. However, if the bytes are in some other encoding that
         * isn't ASCII compatible (for example, UTF-16), then the results of
         * matching are unspecified.
         */
        public fun fromBytes(path: ByteArray): Candidate {
            val normalized = normalizePath(path)
            val basename = fileName(normalized) ?: EMPTY
            val ext = fileNameExt(basename) ?: EMPTY
            return Candidate(normalized, basename, ext)
        }

        private val EMPTY = ByteArray(0)
    }

    internal fun pathPrefix(max: Int): ByteArray =
        if (path.size <= max) path else path.copyOfRange(0, max)

    internal fun pathSuffix(max: Int): ByteArray =
        if (path.size <= max) path else path.copyOfRange(path.size - max, path.size)

    override fun toString(): String = buildString {
        append("Candidate(path=")
        append(path.decodeToString())
        append(", basename=")
        append(basename.decodeToString())
        append(", ext=")
        append(ext.decodeToString())
        append(')')
    }
}
