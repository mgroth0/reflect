package matt.reflect

import kotlin.reflect.KClass



expect fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean

expect fun KClass<*>.firstSimpleName(): String