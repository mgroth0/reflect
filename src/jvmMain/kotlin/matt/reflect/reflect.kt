@file:JvmName("ReflectJvmKt")
package matt.reflect

import matt.collect.dmap.withStoringDefault
import matt.collect.itr.recurse.recurse
import matt.lang.RUNTIME
import matt.log.debug
import org.reflections8.Reflections
import org.reflections8.scanners.MethodAnnotationsScanner
import org.reflections8.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

class NeedClassToShowThisDepIsBeingUsed(val s: String)

annotation class TODO(val message: String = "todo")

val KClass<*>.hasNoArgsConstructor  /*straight from createInstance()*/
  get() = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) } != null


@Target(AnnotationTarget.CLASS)
annotation class ConstructedThroughReflection(val by: KClass<*>)


@Target(AnnotationTarget.CLASS)
annotation class NoArgConstructor

fun KClass<out Annotation>.annotatedJTypes(): MutableSet<Class<*>> = reflections.getTypesAnnotatedWith(
  this.java
)!!

fun KClass<out Annotation>.annotatedKTypes(): List<KClass<out Any>> = annotatedJTypes().map { it.kotlin }

fun KClass<out Annotation>.annotatedJFunctions(): MutableSet<Method> = reflections.getMethodsAnnotatedWith(
  this.java
)!!

fun KClass<out Annotation>.annotatedKFunctions() = annotatedJFunctions().map { it.kotlinFunction }


/*fun testProtoTypeSucceeded(): Boolean {



    *//*  if (ismac()) {
        *//**//*I should re-enable this useful logging at some point. It takes like a full second and I could optimize its usage.*//**//*
	*//**//*(Reflections::class.staticProperties.first { it.name == "log" } as KMutableProperty<*>).setter.call(
	  Reflections::class,
	  null
	)*//**//* *//**//*this must be through reflection or the expression can't compile without slf4j jar on classpath*//**//*
	*//**//*Reflections.log = null*//**//*
  }*//*

    val t = System.nanoTime()
    profile("testing classes have hasNoArgsConstructor...")

    val annotatedKTypes = NoArgConstructor::class.annotatedKTypes()

    var tt = System.nanoTime()
    var d = Duration.ofNanos(tt - t).toMillis()
    profile("getting annotatedKTypes took $d ms")

    annotatedKTypes.forEach {
        if (!it.hasNoArgsConstructor) {
            return false
        }
    }
    tt = System.nanoTime()
    d = Duration.ofNanos(tt - t).toMillis()
    profile("test took $d ms")
    return true
}*/

val reflections by lazy {
  val t = System.nanoTime()



  debug("getting Reflections...")


  val r = Reflections(
	ConfigurationBuilder()
	  .useParallelExecutor(RUNTIME.availableProcessors())
	  .forPackages("matt")

	  .addScanners(MethodAnnotationsScanner())

	//            .setScanners(TypeAnnotationsScanner(),SubTypesScanner())
	/*this wasnt neccesary on mac*/
	/*ConfigurationBuilder().setScanners(SubTypesScanner())*/
  )

  var tt = System.nanoTime()
  var d = Duration.ofNanos(tt - t).toMillis()
  debug("getting Reflections took $d ms")
  r
}

private val subclassCache = mutableMapOf<KClass<*>, List<KClass<*>>>().withStoringDefault {
  /*if (ismac()) {
	(Reflections::class.staticProperties.first { it.name == "log" } as KMutableProperty<*>).setter.call(
	  Reflections::class,
	  null
	)
  }*/
  val skls = reflections

	.getSubTypesOf(it.java)!!.map { it.kotlin }
  /*println(skls)*/
  skls
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> KClass<T>.subclasses() = subclassCache[this] as List<KClass<out T>>


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


fun <T: Any> KClass<out T>.recurseSealedClasses() = recurse {
  it.sealedSubclasses
}

fun <T: Any> Sequence<KClass<out T>>.objectInstances() = mapNotNull { it.objectInstance }.toList()
