// port-lint: source src/pathutil.rs (#[cfg(test)] mod tests)
package io.github.kotlinmania.globset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun bytes(s: String): ByteArray = s.encodeToByteArray()

class PathutilTests {
    @Test fun ext1() = assertEquals(".rs", fileNameExt(bytes("foo.rs"))?.decodeToString())

    @Test fun ext2() = assertEquals(".rs", fileNameExt(bytes(".rs"))?.decodeToString())

    @Test fun ext3() = assertEquals(".rs", fileNameExt(bytes("..rs"))?.decodeToString())

    @Test fun ext4() = assertNull(fileNameExt(bytes("")))

    @Test fun ext5() = assertNull(fileNameExt(bytes("foo")))

    @Test fun normal1() = assertEquals("foo", normalizePath(bytes("foo")).decodeToString())

    @Test fun normal2() = assertEquals("foo/bar", normalizePath(bytes("foo/bar")).decodeToString())

    @Test fun normal3() = assertEquals("foo/bar", normalizePath(bytes("foo\\bar")).decodeToString())

    @Test fun normal4() = assertEquals("foo/bar/baz", normalizePath(bytes("foo\\bar/baz")).decodeToString())
}
