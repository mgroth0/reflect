package matt.reflect.pack

import kotlinx.serialization.Serializable
import matt.lang.classname.common.JvmQualifiedClassName
import matt.prim.str.hasWhitespace
import matt.prim.str.joinWithPeriods


/*for convenience, avoid name collision with the jvm class "Package"*/
@Serializable
@JvmInline
value class Pack(val name: String) {
    constructor(vararg parts: String) : this(parts.joinWithPeriods())

    init {
        require(!name.endsWith("."))
        require(!name.hasWhitespace())
    }

    operator fun get(subName: String) = Pack(name, subName)
    override fun toString(): String = name

    fun asUnixFilePath() = name.replace(".", "/")
}

val MATT_PACK = Pack("matt")

val JvmQualifiedClassName.isMattClass get() = name.startsWith(MATT_PACK.name)
