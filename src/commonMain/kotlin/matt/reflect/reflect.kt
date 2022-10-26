package matt.reflect

import kotlin.reflect.KClass

fun classForName(qualifiedName: String): KClass<*>? = when (qualifiedName) {
  "kotlin.String" -> String::class
  "kotlin.Int"    -> Int::class
  else            -> classForNameImpl(qualifiedName)
}

internal expect fun classForNameImpl(qualifiedName: String): KClass<*>?

expect fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean