// port-lint: source src/pathutil.rs
package io.github.kotlinmania.globset

private const val SLASH: Byte = '/'.code.toByte()
private const val DOT: Byte = '.'.code.toByte()
private const val BACKSLASH: Byte = '\\'.code.toByte()

/**
 * The final component of the path, if it is a normal file.
 *
 * If the path terminates in `..`, or consists solely of a root of prefix,
 * fileName will return `null`.
 */
internal fun fileName(path: ByteArray): ByteArray? {
    if (path.isEmpty()) return null
    val lastSlash = path.lastIndexOfByte(SLASH).let { if (it < 0) 0 else it + 1 }
    val got = path.copyOfRange(lastSlash, path.size)
    if (got.size == 2 && got[0] == DOT && got[1] == DOT) {
        return null
    }
    return got
}

/**
 * Return a file extension given a path's file name.
 *
 * Note that this does NOT match the semantics of the standard library's path
 * extension handling. Namely, the extension includes the `.` and matching is
 * otherwise more liberal. Specifically, the extension is:
 *
 * * `null`, if the file name given is empty;
 * * `null`, if there is no embedded `.`;
 * * Otherwise, the portion of the file name starting with the final `.`.
 *
 * e.g., A file name of `.rs` has an extension `.rs`.
 *
 * N.B. This is done to make certain glob match optimizations easier. Namely,
 * a pattern like `*.rs` is obviously trying to match files with a `rs`
 * extension, but it also matches files like `.rs`, which doesn't have an
 * extension according to the standard library's path extension handling.
 */
internal fun fileNameExt(name: ByteArray): ByteArray? {
    if (name.isEmpty()) return null
    val lastDotAt = name.lastIndexOfByte(DOT)
    if (lastDotAt < 0) return null
    return name.copyOfRange(lastDotAt, name.size)
}

/**
 * Normalizes a path to use `/` as a separator everywhere, even on platforms
 * that recognize other characters as separators.
 */
internal fun normalizePath(path: ByteArray): ByteArray {
    var copied: ByteArray? = null
    for (i in path.indices) {
        val b = path[i]
        if (b == SLASH || !isPathSeparatorByte(b)) continue
        if (copied == null) copied = path.copyOf()
        copied[i] = SLASH
    }
    return copied ?: path
}

private fun isPathSeparatorByte(b: Byte): Boolean = b == SLASH || b == BACKSLASH

private fun ByteArray.lastIndexOfByte(needle: Byte): Int {
    for (i in size - 1 downTo 0) {
        if (this[i] == needle) return i
    }
    return -1
}
