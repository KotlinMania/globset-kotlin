// port-lint: source globset/src/glob.rs
package io.github.kotlinmania.globset

public typealias Err = Error
public typealias Target = Glob

/**
 * Describes a matching strategy for a particular pattern.
 *
 * This provides a way to more quickly determine whether a pattern matches
 * a particular file path in a way that scales with a large number of
 * patterns. For example, if many patterns are of the form `*.ext`, then it's
 * possible to test whether any of those patterns matches by looking up a
 * file path's extension in a hash table.
 */
internal sealed class MatchStrategy {
    /**
     * A pattern matches if and only if the entire file path matches this
     * literal string.
     */
    data class Literal(
        val literal: String,
    ) : MatchStrategy()

    /**
     * A pattern matches if and only if the file path's basename matches this
     * literal string.
     */
    data class BasenameLiteral(
        val literal: String,
    ) : MatchStrategy()

    /**
     * A pattern matches if and only if the file path's extension matches this
     * literal string.
     */
    data class Extension(
        val ext: String,
    ) : MatchStrategy()

    /**
     * A pattern matches if and only if this prefix literal is a prefix of the
     * candidate file path.
     */
    data class Prefix(
        val prefix: String,
    ) : MatchStrategy()

    /**
     * A pattern matches if and only if this prefix literal is a prefix of the
     * candidate file path.
     *
     * An exception: if `component` is true, then `suffix` must appear at the
     * beginning of a file path or immediately following a `/`.
     */
    data class Suffix(
        val suffix: String,
        val component: Boolean,
    ) : MatchStrategy()

    /**
     * A pattern matches only if the given extension matches the file path's
     * extension. Note that this is a necessary but NOT sufficient criterion.
     * Namely, if the extension matches, then a full regex search is still
     * required.
     */
    data class RequiredExtension(
        val ext: String,
    ) : MatchStrategy()

    /** A regex needs to be used for matching. */
    object Regex : MatchStrategy()

    companion object {
        /** Returns a matching strategy for the given pattern. */
        fun new(pat: Glob): MatchStrategy {
            val baseLit = pat.basenameLiteral()
            if (baseLit != null) {
                return BasenameLiteral(baseLit)
            }
            val lit = pat.literal()
            if (lit != null) {
                return Literal(lit)
            }
            val ext = pat.ext()
            if (ext != null) {
                return Extension(ext)
            }
            val prefix = pat.prefix()
            if (prefix != null) {
                return Prefix(prefix)
            }
            val suffixPair = pat.suffix()
            if (suffixPair != null) {
                return Suffix(suffixPair.first, suffixPair.second)
            }
            val reqExt = pat.requiredExt()
            if (reqExt != null) {
                return RequiredExtension(reqExt)
            }
            return Regex
        }
    }
}

/**
 * Glob represents a successfully parsed shell glob pattern.
 *
 * It cannot be used directly to match file paths, but it can be converted
 * to a regular expression string or a matcher.
 */
class Glob internal constructor(
    private val glob: String,
    private val re: String,
    private val opts: GlobOptions,
    internal val tokens: Tokens,
) {
    companion object {
        /** Builds a new pattern with default options. */
        fun new(glob: String): Glob = GlobBuilder.new(glob).build()

        /** Parse a glob pattern from string. */
        fun fromStr(glob: String): Glob = new(glob)
    }

    /** Returns a matcher for this pattern. */
    fun compileMatcher(): GlobMatcher {
        val regex = newRegex(re)
        return GlobMatcher(this, regex)
    }

    /**
     * Returns a strategic matcher.
     */
    internal fun compileStrategicMatcher(): GlobStrategic {
        val strategy = MatchStrategy.new(this)
        val regex = newRegex(re)
        return GlobStrategic(strategy, regex)
    }

    /** Returns the original glob pattern used to build this pattern. */
    fun glob(): String = glob

    /** Returns the string representation of this glob pattern. */
    fun asStr(): String = glob

    /** Returns the string reference of this glob pattern. */
    fun asRef(): String = glob

    /**
     * Returns the regular expression string for this glob.
     */
    fun regex(): String = re

    /**
     * Returns the pattern as a literal if and only if the pattern must match
     * an entire path exactly.
     *
     * The basic format of these patterns is `literal`.
     */
    internal fun literal(): String? {
        if (opts.caseInsensitive) {
            return null
        }
        val lit = StringBuilder()
        for (t in tokens) {
            when (t) {
                is Token.Literal -> lit.append(t.char)
                else -> return null
            }
        }
        return if (lit.isEmpty()) null else lit.toString()
    }

    /**
     * Returns an extension if this pattern matches a file path if and only
     * if the file path has the extension returned.
     */
    internal fun ext(): String? {
        if (opts.caseInsensitive) {
            return null
        }
        if (tokens.isEmpty()) return null
        val start =
            when (tokens[0]) {
                is Token.RecursivePrefix -> 1
                else -> 0
            }
        if (tokens.size <= start) return null
        when (tokens[start]) {
            is Token.ZeroOrMore -> {
                if (start == 0 && opts.literalSeparator) {
                    return null
                }
            }
            else -> return null
        }
        if (tokens.size <= start + 1) return null
        when (val dotTok = tokens[start + 1]) {
            is Token.Literal -> if (dotTok.char != '.') return null
            else -> return null
        }
        val lit = StringBuilder(".")
        for (i in start + 2 until tokens.size) {
            when (val t = tokens[i]) {
                is Token.Literal -> {
                    if (t.char == '.' || t.char == '/') return null
                    lit.append(t.char)
                }
                else -> return null
            }
        }
        return if (lit.isEmpty()) null else lit.toString()
    }

    /**
     * This is like `ext`, but returns an extension even if it isn't sufficient
     * to imply a match.
     */
    internal fun requiredExt(): String? {
        if (opts.caseInsensitive) {
            return null
        }
        val ext = StringBuilder()
        for (i in tokens.size - 1 downTo 0) {
            when (val t = tokens[i]) {
                is Token.Literal -> {
                    if (t.char == '/') return null
                    ext.append(t.char)
                    if (t.char == '.') break
                }
                else -> return null
            }
        }
        if (ext.isEmpty() || ext.last() != '.') {
            return null
        }
        return ext.reverse().toString()
    }

    /**
     * Returns a literal prefix of this pattern if the entire pattern matches
     * if the literal prefix matches.
     */
    internal fun prefix(): String? {
        if (opts.caseInsensitive) {
            return null
        }
        if (tokens.isEmpty()) return null
        val (end, needSep) =
            when (val last = tokens.last()) {
                is Token.ZeroOrMore -> {
                    if (opts.literalSeparator) {
                        return null
                    }
                    Pair(tokens.size - 1, false)
                }
                is Token.RecursiveSuffix -> Pair(tokens.size - 1, true)
                else -> Pair(tokens.size, false)
            }
        val lit = StringBuilder()
        for (i in 0 until end) {
            when (val t = tokens[i]) {
                is Token.Literal -> lit.append(t.char)
                else -> return null
            }
        }
        if (needSep) {
            lit.append('/')
        }
        return if (lit.isEmpty()) null else lit.toString()
    }

    /**
     * Returns a literal suffix of this pattern if the entire pattern matches
     * if the literal suffix matches.
     */
    internal fun suffix(): Pair<String, Boolean>? {
        if (opts.caseInsensitive) {
            return null
        }
        if (tokens.isEmpty()) return null
        val lit = StringBuilder()
        val (startPos, entire) =
            when (tokens[0]) {
                is Token.RecursivePrefix -> {
                    if (tokens.size > 1 && tokens[1] is Token.Literal) {
                        lit.append('/')
                        Pair(1, true)
                    } else {
                        Pair(1, false)
                    }
                }
                else -> Pair(0, false)
            }
        if (tokens.size <= startPos) return null
        val actualStart =
            when (tokens[startPos]) {
                is Token.ZeroOrMore -> {
                    if (opts.literalSeparator) {
                        return null
                    }
                    startPos + 1
                }
                else -> startPos
            }
        for (i in actualStart until tokens.size) {
            when (val t = tokens[i]) {
                is Token.Literal -> lit.append(t.char)
                else -> return null
            }
        }
        val res = lit.toString()
        return if (res.isEmpty() || res == "/") null else Pair(res, entire)
    }

    /**
     * If this pattern only needs to inspect the basename of a file path,
     * then the tokens corresponding to only the basename match are returned.
     */
    internal fun basenameTokens(): List<Token>? {
        if (opts.caseInsensitive) {
            return null
        }
        if (tokens.isEmpty()) return null
        val start =
            when (tokens[0]) {
                is Token.RecursivePrefix -> 1
                else -> return null
            }
        if (tokens.size <= start) return null
        for (i in start until tokens.size) {
            when (val t = tokens[i]) {
                is Token.Literal -> {
                    if (t.char == '/') return null
                }
                is Token.AnyChar, is Token.ZeroOrMore -> {
                    if (!opts.literalSeparator) return null
                }
                is Token.RecursivePrefix,
                is Token.RecursiveSuffix,
                is Token.RecursiveZeroOrMore,
                is Token.CharClass,
                is Token.Alternates,
                -> return null
            }
        }
        return tokens.subList(start, tokens.size)
    }

    /**
     * Returns the pattern as a literal if and only if the pattern exclusively
     * matches the basename of a file path *and* is a literal.
     */
    internal fun basenameLiteral(): String? {
        val toks = basenameTokens() ?: return null
        val lit = StringBuilder()
        for (t in toks) {
            when (t) {
                is Token.Literal -> lit.append(t.char)
                else -> return null
            }
        }
        return lit.toString()
    }

    fun eq(other: Glob): Boolean = this == other

    fun hash(): Int = hashCode()

    fun deref(): String = glob

    fun fmt(builder: StringBuilder) {
        builder.append("Glob(\"$glob\")")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Glob) return false
        return glob == other.glob && opts == other.opts
    }

    override fun hashCode(): Int {
        var result = glob.hashCode()
        result = 31 * result + opts.hashCode()
        return result
    }

    override fun toString(): String = glob
}

/**
 * A matcher for a single pattern.
 */
class GlobMatcher internal constructor(
    internal val pat: Glob,
    internal val re: Regex,
) {
    /** Tests whether the given path matches this pattern or not. */
    fun isMatch(path: String): Boolean = isMatchCandidate(Candidate.new(path))

    /** Tests whether the given path matches this pattern or not. */
    fun isMatch(path: ByteArray): Boolean = isMatchCandidate(Candidate.fromBytes(path))

    /** Tests whether the given path matches this pattern or not. */
    fun isMatchCandidate(path: Candidate): Boolean = re.containsMatchIn(path.toByteString())

    /** Returns the `Glob` used to compile this matcher. */
    fun glob(): Glob = pat
}

/** A strategic matcher for a single pattern. */
internal class GlobStrategic(
    val strategy: MatchStrategy,
    val re: Regex,
) {
    fun isMatch(path: String): Boolean = isMatchCandidate(Candidate.new(path))

    fun isMatch(path: ByteArray): Boolean = isMatchCandidate(Candidate.fromBytes(path))

    fun isMatchCandidate(candidate: Candidate): Boolean {
        val bytePath = candidate.path
        return when (val strat = strategy) {
            is MatchStrategy.Literal -> strat.literal.encodeToByteArray().contentEquals(bytePath)
            is MatchStrategy.BasenameLiteral -> strat.literal.encodeToByteArray().contentEquals(candidate.basename)
            is MatchStrategy.Extension -> strat.ext.encodeToByteArray().contentEquals(candidate.ext)
            is MatchStrategy.Prefix -> startsWith(strat.prefix.encodeToByteArray(), bytePath)
            is MatchStrategy.Suffix -> {
                val suffixBytes = strat.suffix.encodeToByteArray()
                if (strat.component &&
                    suffixBytes.size > 1 &&
                    bytePath.contentEquals(suffixBytes.copyOfRange(1, suffixBytes.size))
                ) {
                    true
                } else {
                    endsWith(suffixBytes, bytePath)
                }
            }
            is MatchStrategy.RequiredExtension -> {
                candidate.ext.contentEquals(strat.ext.encodeToByteArray()) &&
                    re.containsMatchIn(candidate.toByteString())
            }
            is MatchStrategy.Regex -> re.containsMatchIn(candidate.toByteString())
        }
    }
}

/**
 * A builder for a pattern.
 */
class GlobBuilder internal constructor(
    private val glob: String,
    private val opts: GlobOptions = GlobOptions.default(),
) {
    constructor(glob: String) : this(glob, GlobOptions.default())

    companion object {
        /** Create a new builder for the pattern given. */
        fun new(glob: String): GlobBuilder = GlobBuilder(glob)
    }

    /** Toggle whether the pattern matches case insensitively or not. */
    fun caseInsensitive(yes: Boolean): GlobBuilder {
        opts.caseInsensitive = yes
        return this
    }

    /** Toggle whether a literal `/` is required to match a path separator. */
    fun literalSeparator(yes: Boolean): GlobBuilder {
        opts.literalSeparator = yes
        return this
    }

    /**
     * When enabled, a back slash (`\`) may be used to escape
     * special characters in a glob pattern.
     */
    fun backslashEscape(yes: Boolean): GlobBuilder {
        opts.backslashEscape = yes
        return this
    }

    /** Toggle whether an empty pattern in a list of alternates is accepted. */
    fun emptyAlternates(yes: Boolean): GlobBuilder {
        opts.emptyAlternates = yes
        return this
    }

    /** Toggle whether unclosed character classes are allowed. */
    fun allowUnclosedClass(yes: Boolean): GlobBuilder {
        opts.allowUnclosedClass = yes
        return this
    }

    /** Parses and builds the pattern. */
    fun build(): Glob {
        val p = Parser(glob, opts)
        p.parse()
        if (p.branches.isEmpty()) {
            throw IllegalStateException("bug in parser")
        } else if (p.branches.size > 1) {
            throw Error(glob, ErrorKind.UnclosedAlternates)
        } else {
            val tokens = p.branches.removeLast()
            val re = tokens.toRegexWith(opts)
            return Glob(glob, re, opts.copy(), tokens)
        }
    }

    fun fmt(builder: StringBuilder) {
        builder.append("GlobBuilder(\"$glob\")")
    }
}

/**
 * Options for a glob pattern.
 */
internal data class GlobOptions(
    /** Whether matching is case-insensitive. */
    var caseInsensitive: Boolean = false,
    /**
     * Whether to require a literal separator to correspond to a separator in a file
     * path. For example, when enabled, `*` will not recognize `/`.
     */
    var literalSeparator: Boolean = false,
    /**
     * Whether or not to use `\` to escape special characters.
     * For example, when enabled, `\*` will recognize a literal `*`.
     */
    var backslashEscape: Boolean = true,
    /**
     * Whether or not an empty case in an alternate will be removed.
     */
    var emptyAlternates: Boolean = false,
    /**
     * Whether or not an unclosed character class is allowed. When an unclosed
     * character class is found, the opening `[` is treated as a literal `[`.
     * When this isn't enabled, an opening `[` without a corresponding `]` is
     * treated as an error.
     */
    var allowUnclosedClass: Boolean = false,
) {
    companion object {
        fun default(): GlobOptions = GlobOptions()
    }
}

internal class Tokens(
    val list: MutableList<Token> = mutableListOf(),
) : MutableList<Token> by list {
    companion object {
        fun default(): Tokens = Tokens()
    }

    fun deref(): List<Token> = list

    fun derefMut(): MutableList<Token> = list

    fun toRegexWith(options: GlobOptions): String {
        val re = StringBuilder()
        re.append("(?-u)")
        if (options.caseInsensitive) {
            re.append("(?i)")
        }
        re.append('^')
        if (size == 1 && this[0] is Token.RecursivePrefix) {
            re.append(".*$")
            return re.toString()
        }
        tokensToRegex(options, this, re)
        re.append('$')
        return re.toString()
    }

    internal fun tokensToRegex(
        options: GlobOptions,
        tokens: List<Token>,
        re: StringBuilder,
    ) {
        for (tok in tokens) {
            when (tok) {
                is Token.Literal -> re.append(charToEscapedLiteral(tok.char))
                is Token.AnyChar -> {
                    if (options.literalSeparator) {
                        re.append("[^/]")
                    } else {
                        re.append('.')
                    }
                }
                is Token.ZeroOrMore -> {
                    if (options.literalSeparator) {
                        re.append("[^/]*")
                    } else {
                        re.append(".*")
                    }
                }
                is Token.RecursivePrefix -> re.append("(?:/?|.*/)")
                is Token.RecursiveSuffix -> re.append("/.*")
                is Token.RecursiveZeroOrMore -> re.append("(?:/|/.*/)")
                is Token.CharClass -> {
                    re.append('[')
                    if (tok.negated) {
                        re.append('^')
                    }
                    for (r in tok.ranges) {
                        if (r.first == r.second) {
                            re.append(charToEscapedLiteral(r.first))
                        } else {
                            re.append(charToEscapedLiteral(r.first))
                            re.append('-')
                            re.append(charToEscapedLiteral(r.second))
                        }
                    }
                    re.append(']')
                }
                is Token.Alternates -> {
                    val parts = mutableListOf<String>()
                    for (pat in tok.patterns) {
                        val altre = StringBuilder()
                        tokensToRegex(options, pat, altre)
                        if (altre.isNotEmpty() || options.emptyAlternates) {
                            parts.add(altre.toString())
                        }
                    }
                    if (parts.isNotEmpty()) {
                        re.append("(?:")
                        re.append(parts.joinToString("|"))
                        re.append(')')
                    }
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tokens) return false
        return list == other.list
    }

    override fun hashCode(): Int = list.hashCode()

    override fun toString(): String = list.toString()
}

internal sealed class Token {
    data class Literal(
        val char: Char,
    ) : Token()

    object AnyChar : Token()

    object ZeroOrMore : Token()

    object RecursivePrefix : Token()

    object RecursiveSuffix : Token()

    object RecursiveZeroOrMore : Token()

    data class CharClass(
        val negated: Boolean,
        val ranges: List<Pair<Char, Char>>,
    ) : Token()

    data class Alternates(
        val patterns: List<Tokens>,
    ) : Token()
}

private val REGEX_META_CHARS =
    setOf(
        '\\',
        '.',
        '+',
        '*',
        '?',
        '(',
        ')',
        '|',
        '[',
        ']',
        '{',
        '}',
        '^',
        '$',
        '#',
        '&',
        '-',
        '~',
    )

private fun charToEscapedLiteral(c: Char): String {
    if (c in REGEX_META_CHARS) {
        return "\\$c"
    }
    if (c.code <= 0x7F) {
        return c.toString()
    }
    val bytes = c.toString().encodeToByteArray()
    return bytesToEscapedLiteral(bytes)
}

private fun bytesToEscapedLiteral(bs: ByteArray): String {
    val sb = StringBuilder(bs.size * 4)
    for (b in bs) {
        val u = b.toInt() and 0xFF
        if (u <= 0x7F) {
            val ch = u.toChar()
            if (ch in REGEX_META_CHARS) {
                sb.append('\\')
            }
            sb.append(ch)
        } else {
            val hex = u.toString(16)
            sb.append("\\x")
            if (hex.length < 2) sb.append('0')
            sb.append(hex)
        }
    }
    return sb.toString()
}

private class Parser(
    private val glob: String,
    private val opts: GlobOptions,
) {
    val alternatesStack: MutableList<Int> = mutableListOf()
    val branches: MutableList<Tokens> = mutableListOf(Tokens.default())
    private var index = 0
    private var prev: Char? = null
    private var cur: Char? = null
    private var foundUnclosedClass = false

    fun error(kind: ErrorKind): Error = Error(glob, kind)

    fun parse() {
        while (true) {
            val c = bump() ?: break
            when (c) {
                '?' -> pushToken(Token.AnyChar)
                '*' -> parseStar()
                '[' -> {
                    if (!foundUnclosedClass) {
                        parseClass()
                    } else {
                        pushToken(Token.Literal(c))
                    }
                }
                '{' -> pushAlternate()
                '}' -> popAlternate()
                ',' -> parseComma()
                '\\' -> parseBackslash()
                else -> pushToken(Token.Literal(c))
            }
        }
    }

    private fun pushAlternate() {
        alternatesStack.add(branches.size)
        branches.add(Tokens.default())
    }

    private fun popAlternate() {
        if (alternatesStack.isEmpty()) {
            throw error(ErrorKind.UnopenedAlternates)
        }
        val start = alternatesStack.removeLast()
        val drained = mutableListOf<Tokens>()
        while (branches.size > start) {
            drained.add(branches.removeAt(start))
        }
        pushToken(Token.Alternates(drained))
    }

    private fun pushToken(tok: Token) {
        if (branches.isNotEmpty()) {
            branches.last().add(tok)
            return
        }
        throw error(ErrorKind.UnopenedAlternates)
    }

    private fun popToken(): Token {
        if (branches.isNotEmpty() && branches.last().isNotEmpty()) {
            return branches.last().removeLast()
        }
        throw error(ErrorKind.UnopenedAlternates)
    }

    private fun haveTokens(): Boolean {
        if (branches.isEmpty()) {
            throw error(ErrorKind.UnopenedAlternates)
        }
        return branches.last().isNotEmpty()
    }

    private fun parseComma() {
        if (alternatesStack.isEmpty()) {
            pushToken(Token.Literal(','))
        } else {
            branches.add(Tokens.default())
        }
    }

    private fun parseBackslash() {
        if (opts.backslashEscape) {
            val c = bump() ?: throw error(ErrorKind.DanglingEscape)
            pushToken(Token.Literal(c))
        } else if (isPathSeparator('\\')) {
            pushToken(Token.Literal('/'))
        } else {
            pushToken(Token.Literal('\\'))
        }
    }

    private fun parseStar() {
        val prevChar = prev
        if (peek() != '*') {
            pushToken(Token.ZeroOrMore)
            return
        }
        bump() // consume second '*'
        if (!haveTokens()) {
            val pk = peek()
            if (pk == null || isPathSeparator(pk)) {
                pushToken(Token.RecursivePrefix)
                if (pk != null && isPathSeparator(pk)) {
                    bump()
                }
            } else {
                pushToken(Token.ZeroOrMore)
                pushToken(Token.ZeroOrMore)
            }
            return
        }

        if (prevChar == null || !isPathSeparator(prevChar)) {
            if (branches.size <= 1 || (prevChar != ',' && prevChar != '{')) {
                pushToken(Token.ZeroOrMore)
                pushToken(Token.ZeroOrMore)
                return
            }
        }
        val pk = peek()
        val isSuffix =
            when {
                pk == null -> {
                    true
                }
                (pk == ',' || pk == '}') && branches.size >= 2 -> {
                    true
                }
                isPathSeparator(pk) -> {
                    bump()
                    false
                }
                else -> {
                    pushToken(Token.ZeroOrMore)
                    pushToken(Token.ZeroOrMore)
                    return
                }
            }
        when (popToken()) {
            is Token.RecursivePrefix -> pushToken(Token.RecursivePrefix)
            is Token.RecursiveSuffix -> pushToken(Token.RecursiveSuffix)
            else -> {
                if (isSuffix) {
                    pushToken(Token.RecursiveSuffix)
                } else {
                    pushToken(Token.RecursiveZeroOrMore)
                }
            }
        }
    }

    private fun parseClass() {
        val savedIndex = index
        val savedPrev = prev
        val savedCur = cur

        val ranges = mutableListOf<Pair<Char, Char>>()
        val negated =
            if (peek() == '!' || peek() == '^') {
                bump()
                true
            } else {
                false
            }
        var first = true
        var inRange = false
        while (true) {
            val c = bump()
            if (c == null) {
                if (opts.allowUnclosedClass) {
                    index = savedIndex
                    cur = savedCur
                    prev = savedPrev
                    foundUnclosedClass = true
                    pushToken(Token.Literal('['))
                    return
                } else {
                    throw error(ErrorKind.UnclosedClass)
                }
            }
            when (c) {
                ']' -> {
                    if (first) {
                        ranges.add(Pair(']', ']'))
                    } else {
                        break
                    }
                }
                '-' -> {
                    if (first) {
                        ranges.add(Pair('-', '-'))
                    } else if (inRange) {
                        addToLastRange(ranges, '-')
                        inRange = false
                    } else {
                        inRange = true
                    }
                }
                else -> {
                    if (inRange) {
                        addToLastRange(ranges, c)
                    } else {
                        ranges.add(Pair(c, c))
                    }
                    inRange = false
                }
            }
            first = false
        }
        if (inRange) {
            ranges.add(Pair('-', '-'))
        }
        pushToken(Token.CharClass(negated, ranges))
    }

    private fun addToLastRange(ranges: MutableList<Pair<Char, Char>>, add: Char) {
        val lastIdx = ranges.size - 1
        val r = ranges[lastIdx]
        if (add < r.first) {
            throw Error(glob, ErrorKind.InvalidRange(r.first, add))
        }
        ranges[lastIdx] = Pair(r.first, add)
    }

    private fun bump(): Char? {
        prev = cur
        if (index < glob.length) {
            cur = glob[index]
            index++
        } else {
            cur = null
        }
        return cur
    }

    private fun peek(): Char? = if (index < glob.length) glob[index] else null
}

private fun isPathSeparator(c: Char): Boolean = c == '/' || c == '\\'

internal fun startsWith(needle: ByteArray, haystack: ByteArray): Boolean {
    if (needle.size > haystack.size) return false
    for (i in needle.indices) {
        if (haystack[i] != needle[i]) return false
    }
    return true
}

internal fun endsWith(needle: ByteArray, haystack: ByteArray): Boolean {
    if (needle.size > haystack.size) return false
    val offset = haystack.size - needle.size
    for (i in needle.indices) {
        if (haystack[offset + i] != needle[i]) return false
    }
    return true
}

internal fun s(string: String): String = string

internal fun `class`(s: Char, e: Char): Token =
    Token.CharClass(negated = false, ranges = listOf(Pair(s, e)))

internal fun classTok(s: Char, e: Char): Token = `class`(s, e)

internal fun classn(s: Char, e: Char): Token =
    Token.CharClass(negated = true, ranges = listOf(Pair(s, e)))

internal fun classnTok(s: Char, e: Char): Token = classn(s, e)

internal fun rclass(ranges: List<Pair<Char, Char>>): Token =
    Token.CharClass(negated = false, ranges = ranges)

internal fun rclass(vararg ranges: Pair<Char, Char>): Token =
    Token.CharClass(negated = false, ranges = ranges.toList())

internal fun rclassTok(ranges: List<Pair<Char, Char>>): Token = rclass(ranges)

internal fun rclassn(ranges: List<Pair<Char, Char>>): Token =
    Token.CharClass(negated = true, ranges = ranges)

internal fun rclassn(vararg ranges: Pair<Char, Char>): Token =
    Token.CharClass(negated = true, ranges = ranges.toList())

internal fun rclassnTok(ranges: List<Pair<Char, Char>>): Token = rclassn(ranges)
