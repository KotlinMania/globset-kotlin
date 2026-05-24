// port-lint: source src/fnv.rs
package io.github.kotlinmania.globset

/** A convenience alias for creating a hash map with an FNV hasher. */
internal typealias HashMap<K, V> = kotlin.collections.HashMap<K, V>

/** A hasher that implements the Fowler–Noll–Vo (FNV) hash. */
internal class Hasher(private var state: ULong = OFFSET_BASIS) {
    fun finish(): ULong = state

    fun write(bytes: ByteArray) {
        for (byte in bytes) {
            state = state xor byte.toUByte().toULong()
            state = state * PRIME
        }
    }

    companion object {
        private const val OFFSET_BASIS: ULong = 0xcbf29ce484222325uL
        private const val PRIME: ULong = 0x100000001b3uL
    }
}
