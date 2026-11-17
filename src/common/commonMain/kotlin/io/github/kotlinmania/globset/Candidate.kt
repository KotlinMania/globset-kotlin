package io.github.kotlinmania.globset

/**
 * A candidate path for matching.
 *
 * All glob matching in this crate operates on `Candidate` values. Constructing
 * candidates has a very small cost associated with it, so callers may find it
 * beneficial to amortize that cost when matching a single path against multiple
 * globs or sets of globs.
 */
class Candidate internal constructor(
    internal val path: ByteArray,
    internal val basename: ByteArray,
    internal val ext: ByteArray,
) {
    companion object {
        /**
         * Create a new candidate for matching from the given path.
         */
        fun new(path: String): Candidate = fromBytes(path.encodeToByteArray())

        /**
         * Create a new candidate for matching from the given path as a sequence
         * of bytes.
         */
        fun fromBytes(path: ByteArray): Candidate {
            val normalized = normalizePath(path)
            val basename = fileName(normalized) ?: EMPTY_BYTES
            val ext = fileNameExt(basename) ?: EMPTY_BYTES
            return Candidate(normalized, basename, ext)
        }

        /**
         * Create a candidate from a borrowed or owned byte slice.
         */
        fun fromCow(path: ByteArray): Candidate = fromBytes(path)

        private val EMPTY_BYTES = ByteArray(0)
    }

    internal val pathString: String get() = path.decodeToString()
    internal val basenameString: String get() = basename.decodeToString()
    internal val extString: String get() = ext.decodeToString()

    internal fun pathPrefix(max: Int): ByteArray =
        if (path.size <= max) path else path.copyOfRange(0, max)

    internal fun pathSuffix(max: Int): ByteArray =
        if (path.size <= max) path else path.copyOfRange(path.size - max, path.size)

    internal fun toByteString(): String {
        val chars = CharArray(path.size)
        for (i in path.indices) {
            chars[i] = (path[i].toInt() and 0xFF).toChar()
        }
        return chars.concatToString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Candidate) return false
        return path.contentEquals(other.path) &&
            basename.contentEquals(other.basename) &&
            ext.contentEquals(other.ext)
    }

    override fun hashCode(): Int {
        var result = path.contentHashCode()
        result = 31 * result + basename.contentHashCode()
        result = 31 * result + ext.contentHashCode()
        return result
    }

    fun fmt(builder: StringBuilder) {
        builder.append(toString())
    }

    override fun toString(): String =
        buildString {
            append("Candidate(path=")
            append(path.decodeToString())
            append(", basename=")
            append(basename.decodeToString())
            append(", ext=")
            append(ext.decodeToString())
            append(')')
        }
}
