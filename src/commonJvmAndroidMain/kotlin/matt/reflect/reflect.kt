@file:JvmName("ReflectJvmAndroidKt")

package matt.reflect

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSubclassOf


actual fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean = this.isSubclassOf(cls)


actual fun KClass<*>.firstSimpleName() = this.simpleName ?: this.allSuperclasses.first().simpleName!!
