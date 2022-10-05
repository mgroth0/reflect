package matt.reflect

import kotlin.reflect.KClass

expect fun classForName(qualifiedName: String): KClass<*>?

expect fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean