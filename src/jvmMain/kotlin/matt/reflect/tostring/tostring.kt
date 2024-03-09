
package matt.reflect.tostring

import matt.lang.jpy.ExcludeFromPython
import matt.reflect.tostring.common.ReflectingStringableClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible


abstract class PropReflectingStringableClass : ReflectingStringableClass() {
    final override fun toStringProps(): Map<String, Any?> =
        reflectingToStringProps().associate {
            it.name to
                it.apply {
                    isAccessible = true
                }.getter.call()
        }

    @ExcludeFromPython
    open fun reflectingToStringProps(): Set<KProperty<*>> = setOf()
}
