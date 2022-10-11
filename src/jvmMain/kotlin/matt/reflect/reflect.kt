@file:JvmName("ReflectJvmKt")

package matt.reflect

import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

object YesIUseReflect

annotation class TODO(val message: String = "todo")

val KClass<*>.hasNoArgsConstructor  /*straight from createInstance()*/
  get() = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) } != null


@Target(AnnotationTarget.CLASS) annotation class ConstructedThroughReflection(val by: KClass<*>)


@Target(AnnotationTarget.CLASS) annotation class NoArgConstructor

fun KClass<out Annotation>.annotatedJTypes(): MutableSet<Class<*>> = reflections.getTypesAnnotatedWith(
  this.java
)!!

fun KClass<out Annotation>.annotatedKTypes(): List<KClass<out Any>> = annotatedJTypes().map { it.kotlin }

fun KClass<out Annotation>.annotatedJFunctions(): MutableSet<Method> = reflections.getMethodsAnnotatedWith(
  this.java
)!!

fun KClass<out Annotation>.annotatedKFunctions() = annotatedJFunctions().map { it.kotlinFunction }


fun <V: Any?, R: Any?> KFunction<V>.access(op: KFunction<V>.()->R): R {
  val oldAccessible = this.isAccessible
  isAccessible = true
  val r = op(this)
  isAccessible = oldAccessible
  return r
}

fun <V: Any?, R: Any?> KProperty<V>.access(op: KProperty<V>.()->R): R {
  val oldAccessible = this.isAccessible
  isAccessible = true
  val r = op(this)
  isAccessible = oldAccessible
  return r
}

fun KProperty0<*>.accessAndGetDelegate() = access {
  this@accessAndGetDelegate.getDelegate()
}

fun <T> KProperty1<T, *>.accessAndGetDelegate(receiver: T) = access {
  this@accessAndGetDelegate.getDelegate(receiver)
}


actual fun classForName(qualifiedName: String): KClass<*>? {
  return try {
	Class.forName(qualifiedName).kotlin
  } catch (e: ClassNotFoundException) {
	null
  }
}

actual fun KClass<*>.isSubTypeOf(cls: KClass<*>): Boolean = this.isSubclassOf(cls)


fun <T: Any> KClass<out T>.recurseSealedClasses(): Sequence<KClass<out T>> = sequence<KClass<out T>> {
  yield(this@recurseSealedClasses)
  sealedSubclasses.forEach {
	yieldAll(it.recurseSealedClasses())
  }
}

fun <T: Any> Sequence<KClass<out T>>.objectInstances() = mapNotNull { it.objectInstance }.toList()


