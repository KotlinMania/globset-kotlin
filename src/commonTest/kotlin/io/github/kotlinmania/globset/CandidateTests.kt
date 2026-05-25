// port-lint: tests src/lib.rs
package io.github.kotlinmania.globset

import kotlin.test.Test
import kotlin.test.assertEquals

class CandidateTests {
    private fun bytesAsString(b: ByteArray): String = b.decodeToString()

    @Test
    fun newFromSimplePath() {
        val c = Candidate.new("foo/bar.txt")
        assertEquals("foo/bar.txt", bytesAsString(c.path))
        assertEquals("bar.txt", bytesAsString(c.basename))
        assertEquals(".txt", bytesAsString(c.ext))
    }

    @Test
    fun fromBytesPreservesNormalizedPath() {
        val c = Candidate.fromBytes("a\\b\\c.rs".encodeToByteArray())
        assertEquals("a/b/c.rs", bytesAsString(c.path))
        assertEquals("c.rs", bytesAsString(c.basename))
        assertEquals(".rs", bytesAsString(c.ext))
    }

    @Test
    fun newWithNoExtensionLeavesEmptyExt() {
        val c = Candidate.new("README")
        assertEquals("README", bytesAsString(c.path))
        assertEquals("README", bytesAsString(c.basename))
        assertEquals("", bytesAsString(c.ext))
    }

    @Test
    fun dotfileTreatsLeadingDotAsExtension() {
        val c = Candidate.new(".gitignore")
        assertEquals(".gitignore", bytesAsString(c.path))
        assertEquals(".gitignore", bytesAsString(c.basename))
        assertEquals(".gitignore", bytesAsString(c.ext))
    }

    @Test
    fun trailingDotDotProducesEmptyBasename() {
        val c = Candidate.new("foo/..")
        assertEquals("foo/..", bytesAsString(c.path))
        assertEquals("", bytesAsString(c.basename))
        assertEquals("", bytesAsString(c.ext))
    }

    @Test
    fun emptyPath() {
        val c = Candidate.new("")
        assertEquals("", bytesAsString(c.path))
        assertEquals("", bytesAsString(c.basename))
        assertEquals("", bytesAsString(c.ext))
    }

    @Test
    fun pathPrefixTrimsToMax() {
        val c = Candidate.new("abcdefgh")
        assertEquals("abcd", bytesAsString(c.pathPrefix(4)))
        assertEquals("abcdefgh", bytesAsString(c.pathPrefix(100)))
    }

    @Test
    fun pathSuffixTrimsToMax() {
        val c = Candidate.new("abcdefgh")
        assertEquals("efgh", bytesAsString(c.pathSuffix(4)))
        assertEquals("abcdefgh", bytesAsString(c.pathSuffix(100)))
    }
}
