package matt.reflect

import matt.lang.NOT_IMPLEMENTED
import kotlin.reflect.KClass

actual fun classForNameImpl(qualifiedName: String): KClass<*>? = null

actual fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean = NOT_IMPLEMENTED

actual fun KClass<*>.firstSimpleName() = this.simpleName!!