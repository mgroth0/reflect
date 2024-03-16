package matt.reflect.weak

import matt.lang.delegation.provider
import matt.lang.weak.common.WeakRefInter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.cast


abstract class WeakThing<T: WeakThing<T>>: WeakRefInter<T>() {

    private val weakRefs = mutableMapOf<String, WeakProp<*>>()

  /*  private val theConstructor by lazy {
        this::class.primaryConstructor!!.apply {
            isAccessible = true
        }
    }*/
    protected abstract fun constructNew(): T

    @Synchronized
    final override fun deref(): T? {


        val success =
            weakRefs.values.all {
                it.tryDeref()
            }

        return if (success) {
            val ensured = constructNew()
            ensured.weakRefs.values.forEach {
                it.setTheTempRef(weakRefs[it.name]!!.tempRef!!)
                it.name
            }

            ensured
        } else {
            weakRefs.values.forEach {
                it.tempRef = null
            }
            null
        }
    }

    protected inline fun <reified T: Any> weak() =
        provider {
            WeakProp<T>(it, T::class)
        }

    protected inner class WeakProp<T: Any>(val name: String, private val cls: KClass<T>): ReadWriteProperty<Any?, T> {

        internal var wref: WeakRefInter<T>? = null

        internal fun setTheTempRef(any: Any) {
            tempRef = cls.cast(any)
        }

        internal var tempRef: T? = null

        internal fun tryDeref(): Boolean {
            val t = wref!!.deref()
            if (t == null) {
                return false
            } else {
                tempRef = t
                return true
            }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = tempRef as T

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            wref = matt.lang.weak.weak(value)
        }

        init {
            weakRefs[name] = this
        }
    }
}
