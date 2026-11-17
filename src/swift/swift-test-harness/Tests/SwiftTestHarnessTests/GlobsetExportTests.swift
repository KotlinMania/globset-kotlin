import Testing
import Globset

@Suite
struct GlobsetExportTests {
    @Test
    func singleGlobAndBuilderMatchPaths() {
        let glob = Glob.Companion.shared.new(glob: "*.rs")
        #expect(glob.glob() == "*.rs")
        #expect(glob.asStr() == "*.rs")

        let matcher = glob.compileMatcher()
        #expect(matcher.isMatch(path: "src/lib.rs"))
        #expect(!matcher.isMatch(path: "src/lib.kt"))

        let candidate = Candidate.Companion.shared.new(path: "src/main.rs")
        #expect(matcher.isMatchCandidate(path: candidate))

        let ci = GlobBuilder.Companion.shared.new(glob: "foo/*.MD")
            .caseInsensitive(yes: true)
            .literalSeparator(yes: true)
            .build()
            .compileMatcher()
        #expect(ci.isMatch(path: "foo/readme.md"))
        #expect(!ci.isMatch(path: "foo/bar/readme.md"))
    }

    @Test
    func globSetMatchesAndEscapesPatterns() {
        let emptySet = GlobSet.Companion.shared.empty()
        #expect(emptySet.isEmpty())
        #expect(emptySet.len() == 0)
        #expect(!emptySet.isMatch(path: "anything"))

        let set = GlobSetBuilder.Companion.shared.new()
            .add(pat: Glob.Companion.shared.new(glob: "*.rs"))
            .add(pat: Glob.Companion.shared.new(glob: "src/**"))
            .build()
        #expect(!set.isEmpty())
        #expect(set.len() == 2)
        #expect(set.isMatch(path: "main.rs"))
        #expect(set.matches(path: "src/lib.rs") == [0, 1])
        #expect(set.matches(path: "src/lib.kt") == [1])
        #expect(set.matches(path: "README.md").isEmpty)
        #expect(escape(s: "[a]?") == "[[]a[]][?]")
    }
}
