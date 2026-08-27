// port-lint: source globset/src/serde_impl.rs
package io.github.kotlinmania.globset

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object GlobVisitor {
    typealias Value = Glob

    fun expecting(): String = "a glob pattern"

    fun visitStr(v: String): Glob = Glob.new(v)
}

internal object GlobSetVisitor {
    typealias Value = GlobSet

    fun expecting(): String = "an array of glob patterns"

    fun visitSeq(seq: List<Glob>): GlobSet {
        val builder = GlobSetBuilder.new()
        for (glob in seq) {
            builder.add(glob)
        }
        return builder.build()
    }
}

object GlobSerializer : KSerializer<Glob> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Glob", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Glob) {
        encoder.encodeString(value.glob())
    }

    override fun deserialize(decoder: Decoder): Glob {
        val str = decoder.decodeString()
        return GlobVisitor.visitStr(str)
    }
}

object GlobSetSerializer : KSerializer<GlobSet> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: GlobSet): Unit =
        throw UnsupportedOperationException("GlobSet serialization is not supported")

    override fun deserialize(decoder: Decoder): GlobSet {
        val list = delegate.deserialize(decoder)
        val globs = list.map { Glob.new(it) }
        return GlobSetVisitor.visitSeq(globs)
    }
}
