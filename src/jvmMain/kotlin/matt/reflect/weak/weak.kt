package matt.reflect.weak

import matt.lang.delegation.provider
import matt.lang.weak.MyWeakRef
import matt.lang.weak.WeakRefInter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible


abstract class WeakThing<T: WeakThing<T>>: WeakRefInter<T> {

  private val weakRefs = mutableMapOf<String, WeakProp<*>>()

  private val theConstructor by lazy {
	this::class.primaryConstructor!!.apply {
	  isAccessible = true
	}
  }

  @Synchronized
  override fun deref(): T? {


	val success = weakRefs.values.all {
	  it.tryDeref()
	}

	return if (success) {
	  val ensured = theConstructor.call()
	  ensured.weakRefs.values.forEach {
		it.tempRef = this.weakRefs[it.name]!!.tempRef!!
		it.name
	  }
	  @Suppress("UNCHECKED_CAST")
	  ensured as T
	} else {
	  weakRefs.values.forEach {
		it.tempRef = null
	  }
	  null
	}


  }

  protected fun <T: Any> weak() = provider {
	WeakProp<T>(it)
  }

  protected inner class WeakProp<T: Any>(val name: String): ReadWriteProperty<Any?, T> {

	internal var wref: MyWeakRef<T>? = null

	internal var tempRef: Any? = null

	internal fun tryDeref(): Boolean {
	  val t = wref!!.deref()
	  if (t == null) {
		return false
	  } else {
		tempRef = t
		return true
	  }
	}

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
	  @Suppress("UNCHECKED_CAST")
	  return tempRef as T
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
	  wref = MyWeakRef(value)
	}

	init {
	  weakRefs[name] = this
	}

  }

}