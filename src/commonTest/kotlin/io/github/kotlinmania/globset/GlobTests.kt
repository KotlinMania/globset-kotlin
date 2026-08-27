// port-lint: tests globset/src/glob.rs
package io.github.kotlinmania.globset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GlobTests {
    private data class Options(
        val casei: Boolean? = null,
        val litsep: Boolean? = null,
        val bsesc: Boolean? = null,
        val ealtre: Boolean? = null,
        val unccls: Boolean? = null,
    )

    private val CASEI = Options(casei = true)
    private val SLASHLIT = Options(litsep = true)
    private val NOBSESC = Options(bsesc = false)
    private val BSESC = Options(bsesc = true)
    private val EALTRE = Options(bsesc = true, ealtre = true)
    private val UNCCLS = Options(unccls = true)

    private fun testMatches(pat: String, path: String, options: Options = Options()) {
        val builder = GlobBuilder.new(pat)
        options.casei?.let { builder.caseInsensitive(it) }
        options.litsep?.let { builder.literalSeparator(it) }
        options.bsesc?.let { builder.backslashEscape(it) }
        options.ealtre?.let { builder.emptyAlternates(it) }
        options.unccls?.let { builder.allowUnclosedClass(it) }
        val glob = builder.build()
        val matcher = glob.compileMatcher()
        val strategic = glob.compileStrategicMatcher()
        val set = GlobSetBuilder.new().add(glob).build()
        assertTrue(matcher.isMatch(path), "matcher failed for pat '$pat' and path '$path'")
        assertTrue(strategic.isMatch(path), "strategic failed for pat '$pat' and path '$path'")
        assertTrue(set.isMatch(path), "set failed for pat '$pat' and path '$path'")
    }

    private fun testNMatches(pat: String, path: String, options: Options = Options()) {
        val builder = GlobBuilder.new(pat)
        options.casei?.let { builder.caseInsensitive(it) }
        options.litsep?.let { builder.literalSeparator(it) }
        options.bsesc?.let { builder.backslashEscape(it) }
        options.ealtre?.let { builder.emptyAlternates(it) }
        options.unccls?.let { builder.allowUnclosedClass(it) }
        val glob = builder.build()
        val matcher = glob.compileMatcher()
        val strategic = glob.compileStrategicMatcher()
        val set = GlobSetBuilder.new().add(glob).build()
        assertFalse(matcher.isMatch(path), "matcher should not match pat '$pat' and path '$path'")
        assertFalse(strategic.isMatch(path), "strategic should not match pat '$pat' and path '$path'")
        assertFalse(set.isMatch(path), "set should not match pat '$pat' and path '$path'")
    }

    private fun testToRegex(pat: String, expectedRe: String, options: Options = Options()) {
        val builder = GlobBuilder.new(pat)
        options.casei?.let { builder.caseInsensitive(it) }
        options.litsep?.let { builder.literalSeparator(it) }
        options.bsesc?.let { builder.backslashEscape(it) }
        options.ealtre?.let { builder.emptyAlternates(it) }
        options.unccls?.let { builder.allowUnclosedClass(it) }
        val glob = builder.build()
        assertEquals("(?-u)$expectedRe", glob.regex())
    }

    private fun startsWith(needle: ByteArray, haystack: ByteArray): Boolean {
        if (needle.size > haystack.size) return false
        for (i in needle.indices) {
            if (needle[i] != haystack[i]) return false
        }
        return true
    }

    private fun endsWith(needle: ByteArray, haystack: ByteArray): Boolean {
        if (needle.size > haystack.size) return false
        val offset = haystack.size - needle.size
        for (i in needle.indices) {
            if (needle[i] != haystack[offset + i]) return false
        }
        return true
    }

    private fun s(string: String): String = string

    private fun toks(vararg tokens: Token): List<Token> = tokens.toList()

    private fun `class`(s: Char, e: Char): Token =
        Token.CharClass(negated = false, ranges = listOf(Pair(s, e)))

    private fun classTok(s: Char, e: Char): Token = `class`(s, e)

    private fun classn(s: Char, e: Char): Token =
        Token.CharClass(negated = true, ranges = listOf(Pair(s, e)))

    private fun classnTok(s: Char, e: Char): Token = classn(s, e)

    private fun rclass(vararg ranges: Pair<Char, Char>): Token =
        Token.CharClass(negated = false, ranges = ranges.toList())

    private fun rclassTok(vararg ranges: Pair<Char, Char>): Token = rclass(*ranges)

    private fun rclassn(vararg ranges: Pair<Char, Char>): Token =
        Token.CharClass(negated = true, ranges = ranges.toList())

    private fun rclassnTok(vararg ranges: Pair<Char, Char>): Token = rclassn(*ranges)

    // Syntax tests
    @Test
    fun literal1() {
        val glob = Glob.new("a")
        assertEquals(toks(Token.Literal('a')), glob.tokens.list)
    }

    @Test
    fun literal2() {
        val glob = Glob.new("ab")
        assertEquals(toks(Token.Literal('a'), Token.Literal('b')), glob.tokens.list)
    }

    @Test
    fun any1() {
        val glob = Glob.new("?")
        assertEquals(toks(Token.AnyChar), glob.tokens.list)
    }

    @Test
    fun any2() {
        val glob = Glob.new("a?b")
        assertEquals(toks(Token.Literal('a'), Token.AnyChar, Token.Literal('b')), glob.tokens.list)
    }

    @Test
    fun seq1() {
        val glob = Glob.new("*")
        assertEquals(toks(Token.ZeroOrMore), glob.tokens.list)
    }

    @Test
    fun seq2() {
        val glob = Glob.new("a*b")
        assertEquals(toks(Token.Literal('a'), Token.ZeroOrMore, Token.Literal('b')), glob.tokens.list)
    }

    @Test
    fun seq3() {
        val glob = Glob.new("*a*b*")
        assertEquals(
            toks(Token.ZeroOrMore, Token.Literal('a'), Token.ZeroOrMore, Token.Literal('b'), Token.ZeroOrMore),
            glob.tokens.list,
        )
    }

    @Test
    fun rseq1() {
        val glob = Glob.new("**")
        assertEquals(toks(Token.RecursivePrefix), glob.tokens.list)
    }

    @Test
    fun rseq2() {
        val glob = Glob.new("**/")
        assertEquals(toks(Token.RecursivePrefix), glob.tokens.list)
    }

    @Test
    fun rseq3() {
        val glob = Glob.new("/**")
        assertEquals(toks(Token.RecursiveSuffix), glob.tokens.list)
    }

    @Test
    fun rseq4() {
        val glob = Glob.new("/**/")
        assertEquals(toks(Token.RecursiveZeroOrMore), glob.tokens.list)
    }

    @Test
    fun rseq5() {
        val glob = Glob.new("a/**/b")
        assertEquals(toks(Token.Literal('a'), Token.RecursiveZeroOrMore, Token.Literal('b')), glob.tokens.list)
    }

    @Test
    fun cls1() = assertEquals(toks(classTok('a', 'a')), Glob.new("[a]").tokens.list)

    @Test
    fun cls2() = assertEquals(toks(classnTok('a', 'a')), Glob.new("[!a]").tokens.list)

    @Test
    fun cls3() = assertEquals(toks(classTok('a', 'z')), Glob.new("[a-z]").tokens.list)

    @Test
    fun cls4() = assertEquals(toks(classnTok('a', 'z')), Glob.new("[!a-z]").tokens.list)

    @Test
    fun cls5() = assertEquals(toks(classTok('-', '-')), Glob.new("[-]").tokens.list)

    @Test
    fun cls6() = assertEquals(toks(classTok(']', ']')), Glob.new("[]]").tokens.list)

    @Test
    fun cls7() = assertEquals(toks(classTok('*', '*')), Glob.new("[*]").tokens.list)

    @Test
    fun cls8() = assertEquals(toks(classnTok('!', '!')), Glob.new("[!!]").tokens.list)

    @Test
    fun cls9() = assertEquals(toks(rclassTok('a' to 'a', '-' to '-')), Glob.new("[a-]").tokens.list)

    @Test
    fun cls10() = assertEquals(toks(rclassTok('-' to '-', 'a' to 'z')), Glob.new("[-a-z]").tokens.list)

    @Test
    fun cls11() = assertEquals(toks(rclassTok('a' to 'z', '-' to '-')), Glob.new("[a-z-]").tokens.list)

    @Test
    fun cls12() = assertEquals(toks(rclassTok('-' to '-', 'a' to 'z', '-' to '-')), Glob.new("[-a-z-]").tokens.list)

    @Test
    fun cls13() = assertEquals(toks(classTok(']', 'z')), Glob.new("[]-z]").tokens.list)

    @Test
    fun cls14() = assertEquals(toks(classTok('-', 'z')), Glob.new("[--z]").tokens.list)

    @Test
    fun cls15() = assertEquals(toks(classTok(' ', '-')), Glob.new("[ --]").tokens.list)

    @Test
    fun cls16() = assertEquals(toks(rclassTok('0' to '9', 'a' to 'z')), Glob.new("[0-9a-z]").tokens.list)

    @Test
    fun cls17() = assertEquals(toks(rclassTok('a' to 'z', '0' to '9')), Glob.new("[a-z0-9]").tokens.list)

    @Test
    fun cls18() = assertEquals(toks(rclassnTok('0' to '9', 'a' to 'z')), Glob.new("[!0-9a-z]").tokens.list)

    @Test
    fun cls19() = assertEquals(toks(rclassnTok('a' to 'z', '0' to '9')), Glob.new("[!a-z0-9]").tokens.list)

    @Test
    fun cls20() = assertEquals(toks(classnTok('a', 'a')), Glob.new("[^a]").tokens.list)

    @Test
    fun cls21() = assertEquals(toks(classnTok('a', 'z')), Glob.new("[^a-z]").tokens.list)

    // Syntax error tests
    @Test
    fun errUnclosed1() {
        val e = assertFailsWith<Error> { Glob.new("[") }
        assertEquals(ErrorKind.UnclosedClass, e.kind())
    }

    @Test
    fun errUnclosed2() {
        val e = assertFailsWith<Error> { Glob.new("[]") }
        assertEquals(ErrorKind.UnclosedClass, e.kind())
    }

    @Test
    fun errUnclosed3() {
        val e = assertFailsWith<Error> { Glob.new("[!") }
        assertEquals(ErrorKind.UnclosedClass, e.kind())
    }

    @Test
    fun errUnclosed4() {
        val e = assertFailsWith<Error> { Glob.new("[!]") }
        assertEquals(ErrorKind.UnclosedClass, e.kind())
    }

    @Test
    fun errRange1() {
        val e = assertFailsWith<Error> { Glob.new("[z-a]") }
        assertEquals(ErrorKind.InvalidRange('z', 'a'), e.kind())
    }

    @Test
    fun errRange2() {
        val e = assertFailsWith<Error> { Glob.new("[z--]") }
        assertEquals(ErrorKind.InvalidRange('z', '-'), e.kind())
    }

    @Test
    fun errAlt1() {
        val e = assertFailsWith<Error> { Glob.new("{a,b") }
        assertEquals(ErrorKind.UnclosedAlternates, e.kind())
    }

    @Test
    fun errAlt2() {
        val e = assertFailsWith<Error> { Glob.new("{a,{b,c}") }
        assertEquals(ErrorKind.UnclosedAlternates, e.kind())
    }

    @Test
    fun errAlt3() {
        val e = assertFailsWith<Error> { Glob.new("a,b}") }
        assertEquals(ErrorKind.UnopenedAlternates, e.kind())
    }

    @Test
    fun errAlt4() {
        val e = assertFailsWith<Error> { Glob.new("{a,b}}") }
        assertEquals(ErrorKind.UnopenedAlternates, e.kind())
    }

    // Regex compilation tests
    @Test
    fun allowUnclosedClassSingle() = testToRegex("[", "^\\[$", UNCCLS)

    @Test
    fun allowUnclosedClassMany() = testToRegex("[abc", "^\\[abc$", UNCCLS)

    @Test
    fun allowUnclosedClassEmpty1() = testToRegex("[]", "^\\[\\]$", UNCCLS)

    @Test
    fun allowUnclosedClassEmpty2() = testToRegex("[][", "^\\[\\]\\[$", UNCCLS)

    @Test
    fun allowUnclosedClassNegatedUnclosed() = testToRegex("[!", "^\\[!$", UNCCLS)

    @Test
    fun allowUnclosedClassNegatedEmpty() = testToRegex("[!]", "^\\[!\\]$", UNCCLS)

    @Test
    fun allowUnclosedClassBrace1() = testToRegex("{[abc,xyz}", "^(?:\\[abc|xyz)$", UNCCLS)

    @Test
    fun allowUnclosedClassBrace2() = testToRegex("{[abc,[xyz}", "^(?:\\[abc|\\[xyz)$", UNCCLS)

    @Test
    fun allowUnclosedClassBrace3() = testToRegex("{[abc],[xyz}", "^(?:[abc]|\\[xyz)$", UNCCLS)

    @Test
    fun reEmpty() = testToRegex("", "^$")

    @Test
    fun reCasei() = testToRegex("a", "(?i)^a$", CASEI)

    @Test
    fun reSlash1() = testToRegex("?", "^[^/]$", SLASHLIT)

    @Test
    fun reSlash2() = testToRegex("*", "^[^/]*$", SLASHLIT)

    @Test
    fun re1() = testToRegex("a", "^a$")

    @Test
    fun re2() = testToRegex("?", "^.$")

    @Test
    fun re3() = testToRegex("*", "^.*$")

    @Test
    fun re4() = testToRegex("a?", "^a.$")

    @Test
    fun re5() = testToRegex("?a", "^.a$")

    @Test
    fun re6() = testToRegex("a*", "^a.*$")

    @Test
    fun re7() = testToRegex("*a", "^.*a$")

    @Test
    fun re8() = testToRegex("[*]", "^[\\*]$")

    @Test
    fun re9() = testToRegex("[+]", "^[\\+]$")

    @Test
    fun re10() = testToRegex("+", "^\\+$")

    @Test
    fun re11() = testToRegex("☃", "^\\xe2\\x98\\x83$")

    @Test
    fun re12() = testToRegex("**", "^.*$")

    @Test
    fun re13() = testToRegex("**/", "^.*$")

    @Test
    fun re14() = testToRegex("**/*", "^(?:/?|.*/).*$")

    @Test
    fun re15() = testToRegex("**/**", "^.*$")

    @Test
    fun re16() = testToRegex("**/**/*", "^(?:/?|.*/).*$")

    @Test
    fun re17() = testToRegex("**/**/**", "^.*$")

    @Test
    fun re18() = testToRegex("**/**/**/*", "^(?:/?|.*/).*$")

    @Test
    fun re19() = testToRegex("a/**", "^a/.*$")

    @Test
    fun re20() = testToRegex("a/**/**", "^a/.*$")

    @Test
    fun re21() = testToRegex("a/**/**/**", "^a/.*$")

    @Test
    fun re22() = testToRegex("a/**/b", "^a(?:/|/.*/)b$")

    @Test
    fun re23() = testToRegex("a/**/**/b", "^a(?:/|/.*/)b$")

    @Test
    fun re24() = testToRegex("a/**/**/**/b", "^a(?:/|/.*/)b$")

    @Test
    fun re25() = testToRegex("**/b", "^(?:/?|.*/)b$")

    @Test
    fun re26() = testToRegex("**/**/b", "^(?:/?|.*/)b$")

    @Test
    fun re27() = testToRegex("**/**/**/b", "^(?:/?|.*/)b$")

    @Test
    fun re28() = testToRegex("a**", "^a.*.*$")

    @Test
    fun re29() = testToRegex("**a", "^.*.*a$")

    @Test
    fun re30() = testToRegex("a**b", "^a.*.*b$")

    @Test
    fun re31() = testToRegex("***", "^.*.*.*$")

    @Test
    fun re32() = testToRegex("/a**", "^/a.*.*$")

    @Test
    fun re33() = testToRegex("/**a", "^/.*.*a$")

    @Test
    fun re34() = testToRegex("/a**b", "^/a.*.*b$")

    @Test
    fun re35() = testToRegex("{a,b}", "^(?:a|b)$")

    @Test
    fun re36() = testToRegex("{a,{b,c}}", "^(?:a|(?:b|c))$")

    @Test
    fun re37() = testToRegex("{{a,b},{c,d}}", "^(?:(?:a|b)|(?:c|d))$")

    // Match tests
    @Test fun match1() = testMatches("a", "a")

    @Test fun match2() = testMatches("a*b", "a_b")

    @Test fun match3() = testMatches("a*b*c", "abc")

    @Test fun match4() = testMatches("a*b*c", "a_b_c")

    @Test fun match5() = testMatches("a*b*c", "a___b___c")

    @Test fun match6() = testMatches("abc*abc*abc", "abcabcabcabcabcabcabc")

    @Test fun match7() = testMatches("a*a*a*a*a*a*a*a*a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")

    @Test fun match8() = testMatches("a*b[xyz]c*d", "abxcdbxcddd")

    @Test fun match9() = testMatches("*.rs", ".rs")

    @Test fun match10() = testMatches("☃", "☃")

    @Test fun matchrec1() = testMatches("some/**/needle.txt", "some/needle.txt")

    @Test fun matchrec2() = testMatches("some/**/needle.txt", "some/one/needle.txt")

    @Test fun matchrec3() = testMatches("some/**/needle.txt", "some/one/two/needle.txt")

    @Test fun matchrec4() = testMatches("some/**/needle.txt", "some/other/needle.txt")

    @Test fun matchrec5() = testMatches("**", "abcde")

    @Test fun matchrec6() = testMatches("**", "")

    @Test fun matchrec7() = testMatches("**", ".asdf")

    @Test fun matchrec8() = testMatches("**", "/x/.asdf")

    @Test fun matchrec9() = testMatches("some/**/**/needle.txt", "some/needle.txt")

    @Test fun matchrec10() = testMatches("some/**/**/needle.txt", "some/one/needle.txt")

    @Test fun matchrec11() = testMatches("some/**/**/needle.txt", "some/one/two/needle.txt")

    @Test fun matchrec12() = testMatches("some/**/**/needle.txt", "some/other/needle.txt")

    @Test fun matchrec13() = testMatches("**/test", "one/two/test")

    @Test fun matchrec14() = testMatches("**/test", "one/test")

    @Test fun matchrec15() = testMatches("**/test", "test")

    @Test fun matchrec16() = testMatches("/**/test", "/one/two/test")

    @Test fun matchrec17() = testMatches("/**/test", "/one/test")

    @Test fun matchrec18() = testMatches("/**/test", "/test")

    @Test fun matchrec19() = testMatches("**/.*", ".abc")

    @Test fun matchrec20() = testMatches("**/.*", "abc/.abc")

    @Test fun matchrec21() = testMatches("**/foo/bar", "foo/bar")

    @Test fun matchrec22() = testMatches(".*/**", ".abc/abc")

    @Test fun matchrec23() = testMatches("test/**", "test/")

    @Test fun matchrec24() = testMatches("test/**", "test/one")

    @Test fun matchrec25() = testMatches("test/**", "test/one/two")

    @Test fun matchrec26() = testMatches("some/*/needle.txt", "some/one/needle.txt")

    @Test fun matchrange1() = testMatches("a[0-9]b", "a0b")

    @Test fun matchrange2() = testMatches("a[0-9]b", "a9b")

    @Test fun matchrange3() = testMatches("a[!0-9]b", "a_b")

    @Test fun matchrange4() = testMatches("[a-z123]", "1")

    @Test fun matchrange5() = testMatches("[1a-z23]", "1")

    @Test fun matchrange6() = testMatches("[123a-z]", "1")

    @Test fun matchrange7() = testMatches("[abc-]", "-")

    @Test fun matchrange8() = testMatches("[-abc]", "-")

    @Test fun matchrange9() = testMatches("[-a-c]", "b")

    @Test fun matchrange10() = testMatches("[a-c-]", "b")

    @Test fun matchrange11() = testMatches("[-]", "-")

    @Test fun matchrange12() = testMatches("a[^0-9]b", "a_b")

    @Test fun matchpat1() = testMatches("*hello.txt", "hello.txt")

    @Test fun matchpat2() = testMatches("*hello.txt", "gareth_says_hello.txt")

    @Test fun matchpat3() = testMatches("*hello.txt", "some/path/to/hello.txt")

    @Test fun matchpat4() = testMatches("*hello.txt", "some\\path\\to\\hello.txt")

    @Test fun matchpat5() = testMatches("*hello.txt", "/an/absolute/path/to/hello.txt")

    @Test fun matchpat6() = testMatches("*some/path/to/hello.txt", "some/path/to/hello.txt")

    @Test fun matchpat7() = testMatches("*some/path/to/hello.txt", "a/bigger/some/path/to/hello.txt")

    @Test fun matchescape() = testMatches("_[[]_[]]_[?]_[*]_!_", "_[_]_?_*_!_")

    @Test fun matchcasei1() = testMatches("aBcDeFg", "aBcDeFg", CASEI)

    @Test fun matchcasei2() = testMatches("aBcDeFg", "abcdefg", CASEI)

    @Test fun matchcasei3() = testMatches("aBcDeFg", "ABCDEFG", CASEI)

    @Test fun matchcasei4() = testMatches("aBcDeFg", "AbCdEfG", CASEI)

    @Test fun matchalt1() = testMatches("a,b", "a,b")

    @Test fun matchalt2() = testMatches(",", ",")

    @Test fun matchalt3() = testMatches("{a,b}", "a")

    @Test fun matchalt4() = testMatches("{a,b}", "b")

    @Test fun matchalt5() = testMatches("{**/src/**,foo}", "abc/src/bar")

    @Test fun matchalt6() = testMatches("{**/src/**,foo}", "foo")

    @Test fun matchalt7() = testMatches("{[}],foo}", "}")

    @Test fun matchalt8() = testMatches("{foo}", "foo")

    @Test fun matchalt9() = testMatches("{}", "")

    @Test fun matchalt10() = testMatches("{,}", "")

    @Test fun matchalt11() = testMatches("{*.foo,*.bar,*.wat}", "test.foo")

    @Test fun matchalt12() = testMatches("{*.foo,*.bar,*.wat}", "test.bar")

    @Test fun matchalt13() = testMatches("{*.foo,*.bar,*.wat}", "test.wat")

    @Test fun matchalt14() = testMatches("foo{,.txt}", "foo.txt")

    @Test fun matchalt15() = testNMatches("foo{,.txt}", "foo")

    @Test fun matchalt16() = testMatches("foo{,.txt}", "foo", EALTRE)

    @Test fun matchalt17() = testMatches("{a,b{c,d}}", "bc")

    @Test fun matchalt18() = testMatches("{a,b{c,d}}", "bd")

    @Test fun matchalt19() = testMatches("{a,b{c,d}}", "a")

    @Test fun matchslash1() = testMatches("abc/def", "abc/def", SLASHLIT)

    @Test fun matchslash2() = testNMatches("abc?def", "abc/def", SLASHLIT)

    @Test fun matchslash3() = testNMatches("abc*def", "abc/def", SLASHLIT)

    @Test fun matchslash4() = testMatches("abc[/]def", "abc/def", SLASHLIT)

    @Test fun matchbackslash1() = testMatches("\\[", "[", BSESC)

    @Test fun matchbackslash2() = testMatches("\\?", "?", BSESC)

    @Test fun matchbackslash3() = testMatches("\\*", "*", BSESC)

    @Test fun matchbackslash4() = testMatches("\\[a-z]", "\\a", NOBSESC)

    @Test fun matchbackslash5() = testMatches("\\?", "\\a", NOBSESC)

    @Test fun matchbackslash6() = testMatches("\\*", "\\\\", NOBSESC)

    // Negative match tests
    @Test fun matchnot1() = testNMatches("a*b*c", "abcd")

    @Test fun matchnot2() = testNMatches("abc*abc*abc", "abcabcabcabcabcabcabca")

    @Test fun matchnot3() = testNMatches("some/**/needle.txt", "some/other/notthis.txt")

    @Test fun matchnot4() = testNMatches("some/**/**/needle.txt", "some/other/notthis.txt")

    @Test fun matchnot5() = testNMatches("/**/test", "test")

    @Test fun matchnot6() = testNMatches("/**/test", "/one/notthis")

    @Test fun matchnot7() = testNMatches("/**/test", "/notthis")

    @Test fun matchnot8() = testNMatches("**/.*", "ab.c")

    @Test fun matchnot9() = testNMatches("**/.*", "abc/ab.c")

    @Test fun matchnot10() = testNMatches(".*/**", "a.bc")

    @Test fun matchnot11() = testNMatches(".*/**", "abc/a.bc")

    @Test fun matchnot12() = testNMatches("a[0-9]b", "a_b")

    @Test fun matchnot13() = testNMatches("a[!0-9]b", "a0b")

    @Test fun matchnot14() = testNMatches("a[!0-9]b", "a9b")

    @Test fun matchnot15() = testNMatches("[!-]", "-")

    @Test fun matchnot16() = testNMatches("*hello.txt", "hello.txt-and-then-some")

    @Test fun matchnot17() = testNMatches("*hello.txt", "goodbye.txt")

    @Test fun matchnot18() = testNMatches("*some/path/to/hello.txt", "some/path/to/hello.txt-and-then-some")

    @Test fun matchnot19() = testNMatches("*some/path/to/hello.txt", "some/other/path/to/hello.txt")

    @Test fun matchnot20() = testNMatches("a", "foo/a")

    @Test fun matchnot21() = testNMatches("./foo", "foo")

    @Test fun matchnot22() = testNMatches("**/foo", "foofoo")

    @Test fun matchnot23() = testNMatches("**/foo/bar", "foofoo/bar")

    @Test fun matchnot24() = testNMatches("/*.c", "mozilla-sha1/sha1.c")

    @Test fun matchnot25() = testNMatches("*.c", "mozilla-sha1/sha1.c", SLASHLIT)

    @Test fun matchnot26() = testNMatches("**/m4/ltoptions.m4", "csharp/src/packages/repositories.config", SLASHLIT)

    @Test fun matchnot27() = testNMatches("a[^0-9]b", "a0b")

    @Test fun matchnot28() = testNMatches("a[^0-9]b", "a9b")

    @Test fun matchnot29() = testNMatches("[^-]", "-")

    @Test fun matchnot30() = testNMatches("some/*/needle.txt", "some/needle.txt")

    @Test fun matchrec31() = testNMatches("some/*/needle.txt", "some/one/two/needle.txt", SLASHLIT)

    @Test fun matchrec32() = testNMatches("some/*/needle.txt", "some/one/two/three/needle.txt", SLASHLIT)

    @Test fun matchrec33() = testNMatches(".*/**", ".abc")

    @Test fun matchrec34() = testNMatches("foo/**", "foo")

    // Extraction tests
    @Test fun extractLit1() = assertEquals("foo", GlobBuilder.new("foo").build().literal())

    @Test fun extractLit2() =
        assertNull(
            GlobBuilder
                .new("foo")
                .caseInsensitive(true)
                .build()
                .literal(),
        )

    @Test fun extractLit3() = assertEquals("/foo", GlobBuilder.new("/foo").build().literal())

    @Test fun extractLit4() = assertEquals("/foo/", GlobBuilder.new("/foo/").build().literal())

    @Test fun extractLit5() = assertEquals("/foo/bar", GlobBuilder.new("/foo/bar").build().literal())

    @Test fun extractLit6() = assertNull(GlobBuilder.new("*.foo").build().literal())

    @Test fun extractLit7() = assertEquals("foo/bar", GlobBuilder.new("foo/bar").build().literal())

    @Test fun extractLit8() = assertNull(GlobBuilder.new("**/foo/bar").build().literal())

    @Test
    fun extractBasetoks1() =
        assertEquals(
            toks(Token.Literal('f'), Token.Literal('o'), Token.Literal('o')),
            GlobBuilder.new("**/foo").build().basenameTokens(),
        )

    @Test
    fun extractBasetoks2() =
        assertNull(
            GlobBuilder
                .new("**/foo")
                .caseInsensitive(true)
                .build()
                .basenameTokens(),
        )

    @Test
    fun extractBasetoks3() =
        assertEquals(
            toks(Token.Literal('f'), Token.Literal('o'), Token.Literal('o')),
            GlobBuilder
                .new("**/foo")
                .literalSeparator(true)
                .build()
                .basenameTokens(),
        )

    @Test fun extractBasetoks4() =
        assertNull(
            GlobBuilder
                .new("*foo")
                .literalSeparator(true)
                .build()
                .basenameTokens(),
        )

    @Test fun extractBasetoks5() = assertNull(GlobBuilder.new("*foo").build().basenameTokens())

    @Test fun extractBasetoks6() = assertNull(GlobBuilder.new("**/fo*o").build().basenameTokens())

    @Test
    fun extractBasetoks7() =
        assertEquals(
            toks(Token.Literal('f'), Token.Literal('o'), Token.ZeroOrMore, Token.Literal('o')),
            GlobBuilder
                .new("**/fo*o")
                .literalSeparator(true)
                .build()
                .basenameTokens(),
        )

    @Test fun extractExt1() = assertEquals(".rs", GlobBuilder.new("**/*.rs").build().ext())

    @Test fun extractExt2() = assertNull(GlobBuilder.new("**/*.rs.bak").build().ext())

    @Test fun extractExt3() = assertEquals(".rs", GlobBuilder.new("*.rs").build().ext())

    @Test fun extractExt4() = assertNull(GlobBuilder.new("a*.rs").build().ext())

    @Test fun extractExt5() = assertNull(GlobBuilder.new("/*.c").build().ext())

    @Test fun extractExt6() =
        assertNull(
            GlobBuilder
                .new("*.c")
                .literalSeparator(true)
                .build()
                .ext(),
        )

    @Test fun extractExt7() = assertEquals(".c", GlobBuilder.new("*.c").build().ext())

    @Test fun extractReqExt1() = assertEquals(".rs", GlobBuilder.new("*.rs").build().requiredExt())

    @Test fun extractReqExt2() = assertEquals(".rs", GlobBuilder.new("/foo/bar/*.rs").build().requiredExt())

    @Test fun extractReqExt3() = assertEquals(".rs", GlobBuilder.new("/foo/bar/*.rs").build().requiredExt())

    @Test fun extractReqExt4() = assertEquals(".rs", GlobBuilder.new("/foo/bar/.rs").build().requiredExt())

    @Test fun extractReqExt5() = assertEquals(".rs", GlobBuilder.new(".rs").build().requiredExt())

    @Test fun extractReqExt6() = assertNull(GlobBuilder.new("./rs").build().requiredExt())

    @Test fun extractReqExt7() = assertNull(GlobBuilder.new("foo").build().requiredExt())

    @Test fun extractReqExt8() = assertNull(GlobBuilder.new(".foo/").build().requiredExt())

    @Test fun extractReqExt9() = assertNull(GlobBuilder.new("foo/").build().requiredExt())

    @Test fun extractPrefix1() = assertEquals("/foo", GlobBuilder.new("/foo").build().prefix())

    @Test fun extractPrefix2() = assertEquals("/foo/", GlobBuilder.new("/foo/*").build().prefix())

    @Test fun extractPrefix3() = assertNull(GlobBuilder.new("**/foo").build().prefix())

    @Test fun extractPrefix4() = assertEquals("foo/", GlobBuilder.new("foo/**").build().prefix())

    @Test fun extractSuffix1() = assertEquals(Pair("/foo/bar", true), GlobBuilder.new("**/foo/bar").build().suffix())

    @Test fun extractSuffix2() = assertEquals(Pair("/foo/bar", false), GlobBuilder.new("*/foo/bar").build().suffix())

    @Test fun extractSuffix3() =
        assertNull(
            GlobBuilder
                .new("*/foo/bar")
                .literalSeparator(true)
                .build()
                .suffix(),
        )

    @Test fun extractSuffix4() = assertEquals(Pair("foo/bar", false), GlobBuilder.new("foo/bar").build().suffix())

    @Test fun extractSuffix5() = assertEquals(Pair(".foo", false), GlobBuilder.new("*.foo").build().suffix())

    @Test fun extractSuffix6() =
        assertNull(
            GlobBuilder
                .new("*.foo")
                .literalSeparator(true)
                .build()
                .suffix(),
        )

    @Test fun extractSuffix7() = assertEquals(Pair("_test", false), GlobBuilder.new("**/*_test").build().suffix())

    @Test fun extractBaselit1() = assertEquals("foo", GlobBuilder.new("**/foo").build().basenameLiteral())

    @Test fun extractBaselit2() = assertNull(GlobBuilder.new("foo").build().basenameLiteral())

    @Test fun extractBaselit3() = assertNull(GlobBuilder.new("*foo").build().basenameLiteral())

    @Test fun extractBaselit4() = assertNull(GlobBuilder.new("*/foo").build().basenameLiteral())

    @Test
    fun testFromStrAndRefs() {
        val g = Glob.fromStr("src/**/*.kt")
        assertEquals("src/**/*.kt", g.glob())
        assertEquals("src/**/*.kt", g.asStr())
        assertEquals("src/**/*.kt", g.asRef())
        assertEquals(g, Glob.new("src/**/*.kt"))
    }
}
