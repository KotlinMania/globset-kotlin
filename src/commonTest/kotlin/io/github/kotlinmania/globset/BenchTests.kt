// port-lint: tests globset/benches/bench.rs
package io.github.kotlinmania.globset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BenchTests {
    companion object {
        private const val EXT = "some/a/bigger/path/to/the/crazy/needle.txt"
        private const val EXT_PAT = "*.txt"

        private const val SHORT = "some/needle.txt"
        private const val SHORT_PAT = "some/**/needle.txt"

        private const val LONG = "some/a/bigger/path/to/the/crazy/needle.txt"
        private const val LONG_PAT = "some/**/needle.txt"

        private val MANY_SHORT_GLOBS =
            listOf(
                ".*.swp",
                "tags",
                "target",
                "*.lock",
                "tmp",
                "*.csv",
                "*.fst",
                "*-got",
                "*.csv.idx",
                "words",
                "98m*",
                "dict",
                "test",
                "months",
            )

        private const val MANY_SHORT_SEARCH = "98m-blah.csv.idx"

        private fun newGlob(pat: String): GlobMatcher =
            Glob.new(pat).compileMatcher()

        private fun newReglob(pat: String): GlobMatcher =
            Glob.new(pat).compileMatcher()

        private fun newReglobMany(pats: List<String>): GlobSet {
            val builder = GlobSetBuilder.new()
            for (pat in pats) {
                builder.add(Glob.new(pat))
            }
            return builder.build()
        }
    }

    @Test
    fun extGlob() {
        val pat = newGlob(EXT_PAT)
        assertTrue(pat.isMatch(EXT))
    }

    @Test
    fun extRegex() {
        val set = newReglob(EXT_PAT)
        val cand = Candidate.new(EXT)
        assertTrue(set.isMatchCandidate(cand))
    }

    @Test
    fun shortGlob() {
        val pat = newGlob(SHORT_PAT)
        assertTrue(pat.isMatch(SHORT))
    }

    @Test
    fun shortRegex() {
        val set = newReglob(SHORT_PAT)
        val cand = Candidate.new(SHORT)
        assertTrue(set.isMatchCandidate(cand))
    }

    @Test
    fun longGlob() {
        val pat = newGlob(LONG_PAT)
        assertTrue(pat.isMatch(LONG))
    }

    @Test
    fun longRegex() {
        val set = newReglob(LONG_PAT)
        val cand = Candidate.new(LONG)
        assertTrue(set.isMatchCandidate(cand))
    }

    @Test
    fun manyShortGlob() {
        val pats = MANY_SHORT_GLOBS.map { newGlob(it) }
        var count = 0
        for (pat in pats) {
            if (pat.isMatch(MANY_SHORT_SEARCH)) {
                count++
            }
        }
        assertEquals(2, count)
    }

    @Test
    fun manyShortRegexSet() {
        val set = newReglobMany(MANY_SHORT_GLOBS)
        val matches = set.matches(MANY_SHORT_SEARCH)
        assertEquals(2, matches.size)
    }
}
