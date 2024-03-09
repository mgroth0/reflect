
package matt.reflect

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSubclassOf


actual fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean = isSubclassOf(cls)


actual fun KClass<*>.firstSimpleName() = simpleName ?: allSuperclasses.first().simpleName!!
