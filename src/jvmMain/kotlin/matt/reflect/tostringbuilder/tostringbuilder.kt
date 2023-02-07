package matt.reflect.tostringbuilder

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.jvm.isAccessible
import matt.lang.tostring.toStringBuilder

fun Any.toStringBuilder(
  vararg props: KProperty<*>
): String {
  return toStringBuilder(props.associate {
	it.name to it.apply {
	  isAccessible = true
	}.getter.call()
  })
}

fun Any.toStringBuilder(
  map: Map<String, Any?> = mapOf()
): String {
  val realMap = map.toMutableMap()
  if (realMap.isEmpty()) {
	realMap["@"] = hashCode()
  }
  return this::class.firstSimpleName() + map.entries.joinToString(prefix = "[", postfix = "]") {
	"${it.key}=${it.value}"
  }
}



//fun Any.matt.model.tostringbuilder.toStringBuilder(vararg values: Pair<String, Any?>): String {
//  val suffix = if (values.isEmpty()) "@" + this.hashCode() else values.joinToString(" ") {
//	it.first + "=" + it.second
//  }
//  return "${this::class.simpleName} [$suffix]"
//}


