@file:JvmName("ToStringJvmKt")

package matt.reflect.tostring

import matt.lang.jpy.ExcludeFromPython
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

//fun Any.toStringBuilder(
//    vararg props: KProperty<*>
//): String {
//    return toStringBuilder(props.associate {
//        it.name to it.apply {
//            isAccessible = true
//        }.getter.call()
//    })
//}


abstract class PropReflectingStringableClass : ReflectingStringableClass() {
    final override fun toStringProps(): Map<String, Any?> = reflectingToStringProps().associate {
        it.name to it.apply {
            isAccessible = true
        }.getter.call()
    }

    @ExcludeFromPython
    open fun reflectingToStringProps(): Set<KProperty<*>> = setOf()
}
