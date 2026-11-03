// port-lint: tests serde_impl.rs
package io.github.kotlinmania.globset

import io.github.kotlinmania.serdecore.de.StringDeserialize
import io.github.kotlinmania.serdecore.de.mapDeserialize
import io.github.kotlinmania.serdecore.de.value.StrDeserializer
import io.github.kotlinmania.serdecore.de.value.StringDeserializer
import io.github.kotlinmania.serdecore.de.value.intoDeserializer
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SerdeImplTests {
    @Test
    fun globDeserializeBorrowed() {
        val mapDe = mapDeserialize(StringDeserialize, Glob.Companion)
        val de = mapOf(StrDeserializer.new("markdown") to StrDeserializer.new("*.md")).intoDeserializer()
        val map = mapDe.deserialize(de).getOrThrow()
        assertEquals(Glob.new("*.md"), map["markdown"])
    }

    @Test
    fun globDeserializeOwned() {
        val glob = Glob.Companion.deserialize(StringDeserializer.new("*.md")).getOrThrow()
        assertEquals(Glob.new("*.md"), glob)
    }

    @Test
    fun globDeserializeError() {
        val mapDe = mapDeserialize(StringDeserialize, Glob.Companion)
        val de = mapOf(StrDeserializer.new("error") to StrDeserializer.new("[")).intoDeserializer()
        val map = mapDe.deserialize(de)
        assertTrue(map.isFailure)
    }

    @Test
    fun globJsonWorks() {
        val testGlob = Glob.new("src/**/*.rs")
        assertTokens(
            testGlob,
            Glob.Companion,
            listOf(Token.Str("src/**/*.rs")),
        )
    }

    @Test
    fun globSetDeserialize() {
        val de = listOf(StrDeserializer.new("src/**/*.rs"), StrDeserializer.new("README.md")).intoDeserializer()
        val set = GlobSet.Companion.deserialize(de).getOrThrow()
        assertTrue(set.isMatch("src/lib.rs"))
        assertFalse(set.isMatch("Cargo.lock"))
    }
}
