package matt.reflect.prop

import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.jvm.isAccessible


interface KPropertyWrapper<T> {
    val name: String
    val returnType: KType
}

abstract class KPropertyWrapperBase<T>(protected val property: KProperty<T>) : KPropertyWrapper<T> {
    final override val name get() = property.name
    final override val returnType get() = property.returnType

    private var didSetAccessible = false
    protected fun ensureAccessible() {
        if (!didSetAccessible) {
            property.isAccessible = true
            didSetAccessible = true
        }
    }

}

class DetachedKPropertyWrapper<R, T>(property: KProperty<T>) : KPropertyWrapperBase<T>(property) {
    fun getFrom(receiver: R): T {
        ensureAccessible()
        return property.getter.call(receiver)
    }

    fun attachedTo(receiver: R) = AttachedKProperty(receiver, property)
}

class AttachedKProperty<R, T>(
    val receiver: R,
    property: KProperty<T>
) : KPropertyWrapperBase<T>(property) {
    fun get(): T {
        ensureAccessible()
        return property.getter.call(receiver)
    }
}