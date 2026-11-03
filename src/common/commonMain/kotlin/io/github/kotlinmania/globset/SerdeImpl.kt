// port-lint: source serde_impl.rs
package io.github.kotlinmania.globset

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serializer

fun <Ok> serialize(glob: Glob, serializer: Serializer<Ok>): SerdeResult<Ok> =
    serializer.serializeStr(glob.glob())

internal object GlobVisitor : Visitor<Glob> {
    override fun expecting(): String = "a glob pattern"

    override fun visitStr(v: String): SerdeResult<Glob> =
        try {
            SerdeResult.success(Glob.new(v))
        } catch (e: Exception) {
            SerdeResult.failure(SerdeError.custom(e.message ?: "invalid glob pattern"))
        }
}

fun <D : Deserializer> deserialize(companion: Glob.Companion, deserializer: D): SerdeResult<Glob> =
    deserializer.deserializeStr(GlobVisitor)

internal object GlobSetVisitor : Visitor<GlobSet> {
    override fun expecting(): String = "an array of glob patterns"

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<GlobSet> {
        val builder = GlobSetBuilder.new()
        while (true) {
            when (val next = access.nextElement(Glob.Companion)) {
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

fun <D : Deserializer> deserialize(companion: GlobSet.Companion, deserializer: D): SerdeResult<GlobSet> =
    deserializer.deserializeSeq(GlobSetVisitor)
