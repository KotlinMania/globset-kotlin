// port-lint: tests serde_impl.rs
package io.github.kotlinmania.globset

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SerdeImplTests {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun globDeserializeBorrowed() {
        val string = """{"markdown": "*.md"}"""
        val mapSerializer = MapSerializer(String.serializer(), GlobSerializer)
        val map = json.decodeFromString(mapSerializer, string)
        assertEquals(Glob.new("*.md"), map["markdown"])
    }

    @Test
    fun globDeserializeOwned() {
        val string = """{"markdown": "*.md"}"""
        val obj = json.decodeFromString<JsonObject>(string)
        val globStr = obj["markdown"]!!.jsonPrimitive.content
        val glob = Glob.new(globStr)
        assertEquals(Glob.new("*.md"), glob)
    }

    @Test
    fun globDeserializeError() {
        val string = """{"error": "["}"""
        val mapSerializer = MapSerializer(String.serializer(), GlobSerializer)
        assertFailsWith<Error> {
            json.decodeFromString(mapSerializer, string)
        }
    }

    @Test
    fun globJsonWorks() {
        val testGlob = Glob.new("src/**/*.rs")
        val ser = json.encodeToString(GlobSerializer, testGlob)
        assertEquals("\"src/**/*.rs\"", ser)

        val de = json.decodeFromString(GlobSerializer, ser)
        assertEquals(testGlob, de)
    }

    @Test
    fun globSetDeserialize() {
        val j = """ ["src/**/*.rs", "README.md"] """
        val set = json.decodeFromString(GlobSetSerializer, j)
        assertTrue(set.isMatch("src/lib.rs"))
        assertFalse(set.isMatch("Cargo.lock"))
    }
}
