package matt.reflect.tostringbuilder

import matt.reflect.tostring.toStringBuilder
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

fun Any.toStringBuilder(
  vararg props: KProperty<*>
): String {
  return toStringBuilder(props.associate {
	it.name to it.apply {
	  isAccessible = true
	}.getter.call()
  })
}

//fun Any.matt.model.tostringbuilder.toStringBuilder(vararg values: Pair<String, Any?>): String {
//  val suffix = if (values.isEmpty()) "@" + this.hashCode() else values.joinToString(" ") {
//	it.first + "=" + it.second
//  }
//  return "${this::class.simpleName} [$suffix]"
//}


