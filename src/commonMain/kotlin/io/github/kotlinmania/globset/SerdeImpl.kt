// port-lint: source serde_impl.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.globset

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.Visitor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

internal object GlobVisitor : Visitor<Glob> {
    override fun expecting(): String = "a glob pattern"

    override fun visitStr(v: String): SerdeResult<Glob> =
        try {
            SerdeResult.success(Glob.new(v))
        } catch (e: Exception) {
            SerdeResult.failure(SerdeError.custom(e.message ?: "invalid glob pattern"))
        }
}

data object GlobDeserialize : Deserialize<Glob> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Glob> =
        deserializer.deserializeStr(GlobVisitor)
}

internal object GlobSetVisitor : Visitor<GlobSet> {
    override fun expecting(): String = "an array of glob patterns"

    override fun <A> visitSeq(access: A): SerdeResult<GlobSet>
        where A : SeqAccess {
        val builder = GlobSetBuilder.new()
        while (true) {
            when (val next = access.nextElement(GlobDeserialize)) {
                is SerdeResult.Failure -> return SerdeResult.failure(next.error)
                is SerdeResult.Success -> {
                    val glob = next.value ?: break
                    builder.add(glob)
                }
            }
        }
        return try {
            SerdeResult.success(builder.build())
        } catch (e: Exception) {
            SerdeResult.failure(SerdeError.custom(e.message ?: "invalid glob set"))
        }
    }
}

data object GlobSetDeserialize : Deserialize<GlobSet> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<GlobSet> =
        deserializer.deserializeSeq(GlobSetVisitor)
}

@HiddenFromObjC
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

@HiddenFromObjC
object GlobSetSerializer : KSerializer<GlobSet> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: GlobSet): Unit =
        throw UnsupportedOperationException("GlobSet serialization is not supported")

    override fun deserialize(decoder: Decoder): GlobSet {
        val list = delegate.deserialize(decoder)
        val globs = list.map { Glob.new(it) }
        val builder = GlobSetBuilder.new()
        for (glob in globs) {
            builder.add(glob)
        }
        return builder.build()
    }
}
