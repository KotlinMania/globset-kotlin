package io.github.kotlinmania.globset

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer

/**
 * GlobSet represents a group of globs that can be matched together in a
 * single pass.
 */
class GlobSet internal constructor(
    internal val len: Int,
    internal val strats: List<GlobSetMatchStrategy>,
) {
    companion object : Deserialize<GlobSet> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<GlobSet> =
            deserialize(this, deserializer)

        /** Create an empty GlobSet. An empty set matches nothing. */
        fun empty(): GlobSet = GlobSet(0, emptyList())

        /**
         * Create a new [GlobSetBuilder]. A `GlobSetBuilder` can be used to add
         * new patterns. Once all patterns have been added, `build` should be
         * called to produce a `GlobSet`, which can then be used for matching.
         */
        fun builder(): GlobSetBuilder = GlobSetBuilder.new()

        /** Create a default empty GlobSet. */
        fun default(): GlobSet = empty()

        /**
         * Builds a new matcher from a collection of Glob patterns.
         * Once a matcher is built, no new patterns can be added to it.
         */
        fun new(globs: Iterable<Glob>): GlobSet {
            val builder = GlobSetBuilder.new()
            for (g in globs) {
                builder.add(g)
            }
            return builder.build()
        }
    }

    /** Returns true if this set is empty, and therefore matches nothing. */
    fun isEmpty(): Boolean = len == 0

    /** Returns the number of globs in this set. */
    fun len(): Int = len

    /** Returns the number of globs in this set. */
    val size: Int get() = len

    /** Returns true if any glob in this set matches the path given. */
    fun isMatch(path: String): Boolean = isMatchCandidate(Candidate.new(path))

    /** Returns true if any glob in this set matches the path given. */
    fun isMatch(path: ByteArray): Boolean = isMatchCandidate(Candidate.fromBytes(path))

    /**
     * Returns true if any glob in this set matches the path given.
     *
     * This takes a [Candidate] as input, which can be used to amortize the
     * cost of preparing a path for matching.
     */
    fun isMatchCandidate(candidate: Candidate): Boolean {
        if (len == 0) return false
        for (strat in strats) {
            if (strat.isMatch(candidate)) return true
        }
        return false
    }

    /**
     * Returns true if all globs in this set match the path given.
     *
     * This will return true if the set of globs is empty, as in that case all
     * `0` of the globs will match.
     */
    fun matchesAll(path: String): Boolean = matchesAllCandidate(Candidate.new(path))

    /**
     * Returns true if all globs in this set match the path given.
     *
     * This will return true if the set of globs is empty, as in that case all
     * `0` of the globs will match.
     */
    fun matchesAll(path: ByteArray): Boolean = matchesAllCandidate(Candidate.fromBytes(path))

    /**
     * Returns true if all globs in this set match the path given.
     *
     * This takes a [Candidate] as input, which can be used to amortize the cost
     * of preparing a path for matching.
     *
     * This will return true if the set of globs is empty, as in that case all
     * `0` of the globs will match.
     */
    fun matchesAllCandidate(candidate: Candidate): Boolean {
        if (len == 0) return true
        val matches = mutableListOf<Int>()
        matchesCandidateInto(candidate, matches)
        return matches.size == len
    }

    /**
     * Returns the sequence number of every glob pattern that matches the
     * given path.
     */
    fun matches(path: String): List<Int> = matchesCandidate(Candidate.new(path))

    /**
     * Returns the sequence number of every glob pattern that matches the
     * given path.
     */
    fun matches(path: ByteArray): List<Int> = matchesCandidate(Candidate.fromBytes(path))

    /**
     * Returns the sequence number of every glob pattern that matches the
     * given path.
     *
     * This takes a [Candidate] as input, which can be used to amortize the
     * cost of preparing a path for matching.
     */
    fun matchesCandidate(candidate: Candidate): List<Int> {
        val matches = mutableListOf<Int>()
        matchesCandidateInto(candidate, matches)
        return matches
    }

    /**
     * Adds the sequence number of every glob pattern that matches the given
     * path to the list given.
     *
     * [matches] is cleared before matching begins, and contains the set of
     * sequence numbers (in ascending order) after matching ends. If no globs
     * were matched, then [matches] will be empty.
     */
    fun matchesInto(path: String, matches: MutableList<Int>) {
        matchesCandidateInto(Candidate.new(path), matches)
    }

    /**
     * Adds the sequence number of every glob pattern that matches the given
     * path to the list given.
     *
     * [matches] is cleared before matching begins, and contains the set of
     * sequence numbers (in ascending order) after matching ends. If no globs
     * were matched, then [matches] will be empty.
     */
    fun matchesInto(path: ByteArray, matches: MutableList<Int>) {
        matchesCandidateInto(Candidate.fromBytes(path), matches)
    }

    /**
     * Adds the sequence number of every glob pattern that matches the given
     * candidate to the list given.
     *
     * [matches] is cleared before matching begins, and contains the set of
     * sequence numbers (in ascending order) after matching ends. If no globs
     * were matched, then [matches] will be empty.
     *
     * This takes a [Candidate] as input, which can be used to amortize the
     * cost of preparing a path for matching.
     */
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

    fun fmt(builder: StringBuilder) {
        builder.append("GlobSet { len: $len }")
    }

    override fun toString(): String = "GlobSet { len: $len }"
}

/**
 * GlobSetBuilder builds a group of patterns that can be used to
 * simultaneously match a file path.
 */
class GlobSetBuilder internal constructor() {
    internal val pats: MutableList<Glob> = mutableListOf()

    companion object {
        /**
         * Create a new `GlobSetBuilder`. A `GlobSetBuilder` can be used to add new
         * patterns. Once all patterns have been added, `build` should be called
         * to produce a [GlobSet], which can then be used for matching.
         */
        fun new(): GlobSetBuilder = GlobSetBuilder()

        /** Default GlobSetBuilder. */
        fun default(): GlobSetBuilder = new()
    }

    /** Add a new pattern to this set. */
    fun add(pat: Glob): GlobSetBuilder {
        pats.add(pat)
        return this
    }

    fun fmt(builder: StringBuilder) {
        builder.append(toString())
    }

    /** Build the GlobSet. */
    fun build(): GlobSet {
        val literals = mutableMapOf<String, MutableList<Int>>()
        val basenames = mutableMapOf<String, MutableList<Int>>()
        val extensions = mutableMapOf<String, MutableList<Int>>()
        val prefixes = MultiStrategyBuilder()
        val suffixes = MultiStrategyBuilder()
        val reqExts = RequiredExtensionStrategyBuilder()
        val regexes = MultiStrategyBuilder()

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
                    prefixes.add(i, strat.prefix)
                }
                is MatchStrategy.Suffix -> {
                    if (strat.component && strat.suffix.length > 1) {
                        literals.getOrPut(strat.suffix.substring(1)) { mutableListOf() }.add(i)
                    }
                    suffixes.add(i, strat.suffix)
                }
                is MatchStrategy.RequiredExtension -> {
                    reqExts.add(i, strat.ext, pat.regex())
                }
                is MatchStrategy.Regex -> {
                    regexes.add(i, pat.regex())
                }
            }
        }

        val strats = mutableListOf<GlobSetMatchStrategy>()
        if (literals.isNotEmpty()) strats.add(LiteralStrategy(literals))
        if (basenames.isNotEmpty()) strats.add(BasenameLiteralStrategy(basenames))
        if (extensions.isNotEmpty()) strats.add(ExtensionStrategy(extensions))
        if (!prefixes.isEmpty()) strats.add(prefixes.prefix())
        if (!suffixes.isEmpty()) strats.add(suffixes.suffix())
        if (!reqExts.isEmpty()) strats.add(reqExts.build())
        if (!regexes.isEmpty()) strats.add(regexes.regexSet())

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

internal class MultiStrategyBuilder {
    internal val literals = mutableListOf<String>()
    internal val map = mutableListOf<Int>()
    internal var longest = 0

    fun add(globalIndex: Int, literal: String) {
        if (literal.length > longest) {
            longest = literal.length
        }
        map.add(globalIndex)
        literals.add(literal)
    }

    fun prefix(): PrefixStrategy {
        val pairs = literals.zip(map).map { Pair(it.first, it.second) }
        return PrefixStrategy(pairs)
    }

    fun suffix(): SuffixStrategy {
        val pairs = literals.zip(map).map { Pair(it.first, it.second) }
        return SuffixStrategy(pairs)
    }

    fun regexSet(): RegexSetStrategy {
        val pairs = mutableListOf<Pair<Int, Regex>>()
        for (i in literals.indices) {
            pairs.add(Pair(map[i], newRegex(literals[i])))
        }
        return RegexSetStrategy(pairs)
    }

    fun isEmpty(): Boolean = literals.isEmpty()
}

internal class RequiredExtensionStrategyBuilder {
    internal val map = mutableMapOf<String, MutableList<Pair<Int, String>>>()

    fun add(globalIndex: Int, ext: String, regex: String) {
        map.getOrPut(ext) { mutableListOf() }.add(Pair(globalIndex, regex))
    }

    fun build(): RequiredExtensionStrategy {
        val exts = mutableMapOf<String, List<Pair<Int, Regex>>>()
        for ((ext, regexes) in map) {
            val list = mutableListOf<Pair<Int, Regex>>()
            for ((globalIndex, regex) in regexes) {
                list.add(Pair(globalIndex, newRegex(regex)))
            }
            exts[ext] = list
        }
        return RequiredExtensionStrategy(exts)
    }

    fun isEmpty(): Boolean = map.isEmpty()
}

internal fun newRegex(pat: String): Regex {
    var cleaned = pat
    var options = setOf<RegexOption>()
    if (cleaned.startsWith("(?-u)")) {
        cleaned = cleaned.removePrefix("(?-u)")
    }
    if (cleaned.startsWith("(?i)")) {
        cleaned = cleaned.removePrefix("(?i)")
        options = setOf(RegexOption.IGNORE_CASE)
    }
    cleaned = sanitizeRegexForJs(cleaned)
    return Regex(cleaned, options)
}

private fun sanitizeRegexForJs(pattern: String): String {
    val sb = StringBuilder(pattern.length)
    var inClass = false
    var i = 0
    while (i < pattern.length) {
        val c = pattern[i]
        if (c == '\\' && i + 1 < pattern.length) {
            val next = pattern[i + 1]
            if (!inClass && (next == '-' || next == '#' || next == '&' || next == '~')) {
                sb.append(next)
                i += 2
                continue
            }
            sb.append('\\')
            sb.append(next)
            i += 2
            continue
        }
        if (c == '[') {
            inClass = true
        } else if (c == ']') {
            inClass = false
        }
        sb.append(c)
        i++
    }
    return sb.toString()
}

internal fun newRegexSet(patterns: Iterable<String>): List<Regex> =
    patterns.map { newRegex(it) }

/**
 * Escape meta-characters within the given glob pattern.
 *
 * The escaping works by surrounding meta-characters with brackets. For
 * example, `*` becomes `[*]`.
 */
fun escape(s: String): String {
    val escaped = StringBuilder(s.length)
    for (c in s) {
        when (c) {
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
