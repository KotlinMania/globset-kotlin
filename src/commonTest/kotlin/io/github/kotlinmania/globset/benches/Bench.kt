// port-lint: source globset/benches/bench.rs
package io.github.kotlinmania.globset.benches

import io.github.kotlinmania.globset.Candidate
import io.github.kotlinmania.globset.Glob
import io.github.kotlinmania.globset.GlobMatcher
import io.github.kotlinmania.globset.GlobSet
import io.github.kotlinmania.globset.GlobSetBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Bench {
    private val ext = "some/a/bigger/path/to/the/crazy/needle.txt"
    private val extPat = "*.txt"

    private val short = "some/needle.txt"
    private val shortPat = "some/**/needle.txt"

    private val long = "some/a/bigger/path/to/the/crazy/needle.txt"
    private val longPat = "some/**/needle.txt"

    private fun newReglob(pat: String): GlobMatcher =
        Glob.new(pat).compileMatcher()

    private fun newReglobMany(pats: List<String>): GlobSet {
        val builder = GlobSetBuilder.new()
        for (pat in pats) {
            builder.add(Glob.new(pat))
        }
        return builder.build()
    }

    @Test
    fun extRegex() {
        val set = newReglob(extPat)
        val cand = Candidate.new(ext)
        assertTrue(set.isMatchCandidate(cand))
    }

    @Test
    fun shortRegex() {
        val set = newReglob(shortPat)
        val cand = Candidate.new(short)
        assertTrue(set.isMatchCandidate(cand))
    }

    @Test
    fun longRegex() {
        val set = newReglob(longPat)
        val cand = Candidate.new(long)
        assertTrue(set.isMatchCandidate(cand))
    }

    private val manyShortGlobs =
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

    private val manyShortSearch = "98m-blah.csv.idx"

    @Test
    fun manyShortRegexSet() {
        val set = newReglobMany(manyShortGlobs)
        assertEquals(2, set.matches(manyShortSearch).size)
    }
}
