package matt.reflect

import kotlin.reflect.KClass

fun classForName(qualifiedName: String): KClass<*>? = when (qualifiedName) {
  "kotlin.String" -> String::class
  "kotlin.Int"    -> Int::class
  else            -> classForNameImpl(qualifiedName) ?: run {
	/*kotlinx.serialization serial descriptor names when nullable can include "?"*/
	if (qualifiedName.endsWith("?")) {
	  classForName(
		qualifiedName.removeSuffix("?")
	  )
	} else null
  }
}

internal expect fun classForNameImpl(qualifiedName: String): KClass<*>?

expect fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean

expect fun KClass<*>.firstSimpleName(): String