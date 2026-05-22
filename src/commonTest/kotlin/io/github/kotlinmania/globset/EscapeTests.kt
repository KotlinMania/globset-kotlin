// port-lint: source src/lib.rs (#[cfg(test)] mod tests::escape)
package io.github.kotlinmania.globset

import kotlin.test.Test
import kotlin.test.assertEquals

class EscapeTests {
    @Test fun escape() {
        assertEquals("foo", escape("foo"))
        assertEquals("foo[*]", escape("foo*"))
        assertEquals("[[][]]", escape("[]"))
        assertEquals("[*][?]", escape("*?"))
        assertEquals("src/[*][*]/[*].rs", escape("src/**/*.rs"))
        assertEquals("bar[[]ab[]]baz", escape("bar[ab]baz"))
        assertEquals("bar[[]!![]]!baz", escape("bar[!!]!baz"))
    }
}
