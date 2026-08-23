// port-lint: source serde_impl.rs
package io.github.kotlinmania.globset

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object GlobSerializer : KSerializer<Glob> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Glob", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Glob) {
        encoder.encodeString(value.glob())
    }

    override fun deserialize(decoder: Decoder): Glob {
        val str = decoder.decodeString()
        return Glob.new(str)
    }
}

object GlobSetSerializer : KSerializer<GlobSet> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: GlobSet): Unit = throw UnsupportedOperationException("GlobSet serialization is not supported")

    override fun deserialize(decoder: Decoder): GlobSet {
        val list = delegate.deserialize(decoder)
        val builder = GlobSetBuilder.new()
        for (pattern in list) {
            builder.add(Glob.new(pattern))
        }
        return builder.build()
    }
}
