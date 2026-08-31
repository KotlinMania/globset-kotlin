// port-lint: tests globset/src/lib.rs
package io.github.kotlinmania.globset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibTests {
    private fun bytesAsString(b: ByteArray): String = b.decodeToString()

    @Test
    fun setWorks() {
        val builder = GlobSetBuilder.new()
        builder.add(Glob.new("src/**/*.rs"))
        builder.add(Glob.new("*.c"))
        builder.add(Glob.new("src/lib.rs"))
        val set = builder.build()

        assertTrue(set.isMatch("foo.c"))
        assertTrue(set.isMatch("src/foo.c"))
        assertFalse(set.isMatch("foo.rs"))
        assertFalse(set.isMatch("tests/foo.rs"))
        assertTrue(set.isMatch("src/foo.rs"))
        assertTrue(set.isMatch("src/grep/src/main.rs"))

        val matches = set.matches("src/lib.rs")
        assertEquals(2, matches.size)
        assertEquals(0, matches[0])
        assertEquals(2, matches[1])
    }

    @Test
    fun emptySetWorks() {
        val set = GlobSetBuilder.new().build()
        assertFalse(set.isMatch(""))
        assertFalse(set.isMatch("a"))
        assertTrue(set.matchesAll("a"))
    }

    @Test
    fun defaultSetIsEmptyWorks() {
        val set: GlobSet = GlobSet.default()
        assertFalse(set.isMatch(""))
        assertFalse(set.isMatch("a"))
    }

    @Test
    fun escape() {
        assertEquals("foo", escape("foo"))
        assertEquals("foo[*]", escape("foo*"))
        assertEquals("[[][]]", escape("[]"))
        assertEquals("[*][?]", escape("*?"))
        assertEquals("src/[*][*]/[*].rs", escape("src/**/*.rs"))
        assertEquals("bar[[]ab[]]baz", escape("bar[ab]baz"))
        assertEquals("bar[[]!![]]!baz", escape("bar[!!]!baz"))
    }

    @Test
    fun setDoesNotRemember() {
        val builder = GlobSetBuilder.new()
        builder.add(Glob.new("*foo*"))
        builder.add(Glob.new("*bar*"))
        builder.add(Glob.new("*quux*"))
        val set = builder.build()

        val matches = set.matches("ZfooZquuxZ")
        assertEquals(2, matches.size)
        assertEquals(0, matches[0])
        assertEquals(2, matches[1])

        val matchesNada = set.matches("nada")
        assertEquals(0, matchesNada.size)
    }

    @Test
    fun debug() {
        val builder = GlobSetBuilder.new()
        builder.add(Glob.new("*foo*"))
        builder.add(Glob.new("*bar*"))
        builder.add(Glob.new("*quux*"))
        assertEquals(
            "GlobSetBuilder { pats: [Glob(\"*foo*\"), Glob(\"*bar*\"), Glob(\"*quux*\")] }",
            builder.toString(),
        )
    }

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
