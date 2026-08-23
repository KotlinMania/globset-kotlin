// port-lint: source lib.rs
package io.github.kotlinmania.globset

/**
 * A GlobSet represents a group of globs that can be matched together in a
 * single pass.
 */
class GlobSet internal constructor(
    internal val len: Int,
    internal val strats: List<GlobSetMatchStrategy>,
) {
    companion object {
        /** Create a new empty GlobSet. */
        fun empty(): GlobSet = GlobSet(0, emptyList())

        /** Create a new builder for a GlobSet. */
        fun builder(): GlobSetBuilder = GlobSetBuilder.new()

        /** Default empty GlobSet. */
        fun default(): GlobSet = empty()

        /** Create a GlobSet from a collection of globs. */
        fun new(globs: Iterable<Glob>): GlobSet {
            val builder = GlobSetBuilder.new()
            for (g in globs) {
                builder.add(g)
            }
            return builder.build()
        }
    }

    /** Returns true if there are no globs in this set. */
    fun isEmpty(): Boolean = len == 0

    /** Returns the number of globs in this set. */
    fun len(): Int = len

    /** Returns the number of globs in this set. */
    val size: Int get() = len

    /** Returns true if and only if any glob in this set matches the given path. */
    fun isMatch(path: String): Boolean = isMatchCandidate(Candidate.new(path))

    /** Returns true if and only if any glob in this set matches the given path. */
    fun isMatch(path: ByteArray): Boolean = isMatchCandidate(Candidate.fromBytes(path))

    /** Returns true if and only if any glob in this set matches the candidate. */
    fun isMatchCandidate(candidate: Candidate): Boolean {
        if (len == 0) return false
        for (strat in strats) {
            if (strat.isMatch(candidate)) return true
        }
        return false
    }

    /** Returns true if and only if all globs in this set match the given path. */
    fun matchesAll(path: String): Boolean = matchesAllCandidate(Candidate.new(path))

    /** Returns true if and only if all globs in this set match the given path. */
    fun matchesAll(path: ByteArray): Boolean = matchesAllCandidate(Candidate.fromBytes(path))

    /** Returns true if and only if all globs in this set match the candidate. */
    fun matchesAllCandidate(candidate: Candidate): Boolean {
        if (len == 0) return true
        val matches = mutableListOf<Int>()
        matchesCandidateInto(candidate, matches)
        return matches.size == len
    }

    /** Returns the set of all information matching this pattern. */
    fun matches(path: String): List<Int> = matchesCandidate(Candidate.new(path))

    /** Returns the set of all information matching this pattern. */
    fun matches(path: ByteArray): List<Int> = matchesCandidate(Candidate.fromBytes(path))

    /** Returns the set of all information matching this pattern. */
    fun matchesCandidate(candidate: Candidate): List<Int> {
        val matches = mutableListOf<Int>()
        matchesCandidateInto(candidate, matches)
        return matches
    }

    /** Matches the given path and populates the given list. */
    fun matchesInto(path: String, matches: MutableList<Int>) {
        matchesCandidateInto(Candidate.new(path), matches)
    }

    /** Matches the given path and populates the given list. */
    fun matchesInto(path: ByteArray, matches: MutableList<Int>) {
        matchesCandidateInto(Candidate.fromBytes(path), matches)
    }

    /** Matches the given candidate and populates the given list. */
    fun matchesCandidateInto(candidate: Candidate, matches: MutableList<Int>) {
        matches.clear()
        if (len == 0) return
        for (strat in strats) {
            strat.matchesInto(candidate, matches)
        }
        matches.sort()
        if (matches.isNotEmpty()) {
            var j = 0
            for (i in 1 until matches.size) {
                if (matches[i] != matches[j]) {
                    j++
                    matches[j] = matches[i]
                }
            }
            while (matches.size > j + 1) {
                matches.removeAt(matches.size - 1)
            }
        }
    }
}

/**
 * A builder for a GlobSet.
 */
class GlobSetBuilder internal constructor() {
    internal val pats: MutableList<Glob> = mutableListOf()

    companion object {
        /** Create a new builder for a GlobSet. */
        fun new(): GlobSetBuilder = GlobSetBuilder()

        /** Default GlobSetBuilder. */
        fun default(): GlobSetBuilder = new()
    }

    /** Add a pattern to this builder. */
    fun add(pat: Glob): GlobSetBuilder {
        pats.add(pat)
        return this
    }

    /** Build the GlobSet. */
    fun build(): GlobSet {
        val literals = mutableMapOf<String, MutableList<Int>>()
        val basenames = mutableMapOf<String, MutableList<Int>>()
        val extensions = mutableMapOf<String, MutableList<Int>>()
        val prefixes = mutableListOf<Pair<String, Int>>()
        val suffixes = mutableListOf<Pair<String, Int>>()
        val reqExts = mutableMapOf<String, MutableList<Pair<Int, Regex>>>()
        val regexes = mutableListOf<Pair<Int, Regex>>()

        for ((i, pat) in pats.withIndex()) {
            when (val strat = MatchStrategy.new(pat)) {
                is MatchStrategy.Literal -> {
                    literals.getOrPut(strat.literal) { mutableListOf() }.add(i)
                }
                is MatchStrategy.BasenameLiteral -> {
                    basenames.getOrPut(strat.literal) { mutableListOf() }.add(i)
                }
                is MatchStrategy.Extension -> {
                    extensions.getOrPut(strat.ext) { mutableListOf() }.add(i)
                }
                is MatchStrategy.Prefix -> {
                    prefixes.add(Pair(strat.prefix, i))
                }
                is MatchStrategy.Suffix -> {
                    if (strat.component && strat.suffix.length > 1) {
                        literals.getOrPut(strat.suffix.substring(1)) { mutableListOf() }.add(i)
                    }
                    suffixes.add(Pair(strat.suffix, i))
                }
                is MatchStrategy.RequiredExtension -> {
                    reqExts.getOrPut(strat.ext) { mutableListOf() }.add(Pair(i, newRegex(pat.regex())))
                }
                is MatchStrategy.Regex -> {
                    regexes.add(Pair(i, newRegex(pat.regex())))
                }
            }
        }

        val strats = mutableListOf<GlobSetMatchStrategy>()
        if (literals.isNotEmpty()) strats.add(LiteralStrategy(literals))
        if (basenames.isNotEmpty()) strats.add(BasenameLiteralStrategy(basenames))
        if (extensions.isNotEmpty()) strats.add(ExtensionStrategy(extensions))
        if (prefixes.isNotEmpty()) strats.add(PrefixStrategy(prefixes))
        if (suffixes.isNotEmpty()) strats.add(SuffixStrategy(suffixes))
        if (reqExts.isNotEmpty()) strats.add(RequiredExtensionStrategy(reqExts))
        if (regexes.isNotEmpty()) strats.add(RegexSetStrategy(regexes))

        return GlobSet(pats.size, strats)
    }

    override fun toString(): String =
        buildString {
            append("GlobSetBuilder { pats: [")
            for ((i, p) in pats.withIndex()) {
                if (i > 0) append(", ")
                append("Glob(\"${p.glob()}\")")
            }
            append("] }")
        }
}

internal sealed interface GlobSetMatchStrategy {
    fun isMatch(candidate: Candidate): Boolean

    fun matchesInto(candidate: Candidate, matches: MutableList<Int>)
}

internal class LiteralStrategy(
    val map: Map<String, List<Int>>,
) : GlobSetMatchStrategy {
    override fun isMatch(candidate: Candidate): Boolean =
        map.containsKey(candidate.pathString)

    override fun matchesInto(candidate: Candidate, matches: MutableList<Int>) {
        map[candidate.pathString]?.let { matches.addAll(it) }
    }
}

internal class BasenameLiteralStrategy(
    val map: Map<String, List<Int>>,
) : GlobSetMatchStrategy {
    override fun isMatch(candidate: Candidate): Boolean =
        candidate.basename.isNotEmpty() && map.containsKey(candidate.basenameString)

    override fun matchesInto(candidate: Candidate, matches: MutableList<Int>) {
        if (candidate.basename.isNotEmpty()) {
            map[candidate.basenameString]?.let { matches.addAll(it) }
        }
    }
}

internal class ExtensionStrategy(
    val map: Map<String, List<Int>>,
) : GlobSetMatchStrategy {
    override fun isMatch(candidate: Candidate): Boolean =
        candidate.ext.isNotEmpty() && map.containsKey(candidate.extString)

    override fun matchesInto(candidate: Candidate, matches: MutableList<Int>) {
        if (candidate.ext.isNotEmpty()) {
            map[candidate.extString]?.let { matches.addAll(it) }
        }
    }
}

internal class PrefixStrategy(
    val prefixes: List<Pair<String, Int>>,
) : GlobSetMatchStrategy {
    override fun isMatch(candidate: Candidate): Boolean {
        val pathStr = candidate.pathString
        for ((prefix, _) in prefixes) {
            if (pathStr.startsWith(prefix)) return true
        }
        return false
    }

    override fun matchesInto(candidate: Candidate, matches: MutableList<Int>) {
        val pathStr = candidate.pathString
        for ((prefix, index) in prefixes) {
            if (pathStr.startsWith(prefix)) matches.add(index)
        }
    }
}

internal class SuffixStrategy(
    val suffixes: List<Pair<String, Int>>,
) : GlobSetMatchStrategy {
    override fun isMatch(candidate: Candidate): Boolean {
        val pathStr = candidate.pathString
        for ((suffix, _) in suffixes) {
            if (pathStr.endsWith(suffix)) return true
        }
        return false
    }

    override fun matchesInto(candidate: Candidate, matches: MutableList<Int>) {
        val pathStr = candidate.pathString
        for ((suffix, index) in suffixes) {
            if (pathStr.endsWith(suffix)) matches.add(index)
        }
    }
}

internal class RequiredExtensionStrategy(
    val map: Map<String, List<Pair<Int, Regex>>>,
) : GlobSetMatchStrategy {
    override fun isMatch(candidate: Candidate): Boolean {
        if (candidate.ext.isEmpty()) return false
        val list = map[candidate.extString] ?: return false
        val byteStr = candidate.toByteString()
        for ((_, re) in list) {
            if (re.containsMatchIn(byteStr)) return true
        }
        return false
    }

    override fun matchesInto(candidate: Candidate, matches: MutableList<Int>) {
        if (candidate.ext.isEmpty()) return
        val list = map[candidate.extString] ?: return
        val byteStr = candidate.toByteString()
        for ((index, re) in list) {
            if (re.containsMatchIn(byteStr)) matches.add(index)
        }
    }
}

internal class RegexSetStrategy(
    val regexes: List<Pair<Int, Regex>>,
) : GlobSetMatchStrategy {
    override fun isMatch(candidate: Candidate): Boolean {
        val byteStr = candidate.toByteString()
        for ((_, re) in regexes) {
            if (re.containsMatchIn(byteStr)) return true
        }
        return false
    }

    override fun matchesInto(candidate: Candidate, matches: MutableList<Int>) {
        val byteStr = candidate.toByteString()
        for ((index, re) in regexes) {
            if (re.containsMatchIn(byteStr)) matches.add(index)
        }
    }
}
