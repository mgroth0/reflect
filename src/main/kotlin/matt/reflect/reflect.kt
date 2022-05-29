package matt.reflect

import matt.klib.commons.ismac
import matt.klib.dmap.withStoringDefault
import matt.klib.log.debug
import matt.klib.log.profile
import org.reflections8.Reflections
import org.reflections8.scanners.SubTypesScanner
import org.reflections8.scanners.TypeAnnotationsScanner
import org.reflections8.util.ConfigurationBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Duration
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

annotation class TODO(val message: String = "todo")

val KClass<*>.hasNoArgsConstructor  /*straight from createInstance()*/
    get() = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) } != null


@Target(AnnotationTarget.CLASS)
annotation class ConstructedThroughReflection(val by: KClass<*>)





inline fun onLinux(op: () -> Unit) {
    contract {
        callsInPlace(op, AT_MOST_ONCE)
    }
    if (!ismac) op()
}

inline fun onMac(op: () -> Unit) {
    contract {
        callsInPlace(op, AT_MOST_ONCE)
    }
    if (ismac) op()
}

@Target(AnnotationTarget.CLASS)
annotation class NoArgConstructor

fun KClass<out Annotation>.annotatedJTypes() = reflections.getTypesAnnotatedWith(
    this.java
)!!

fun KClass<out Annotation>.annotatedKTypes() = annotatedJTypes().map { it.kotlin }

fun KClass<out Annotation>.annotatedJFunctions() = reflections.getMethodsAnnotatedWith(
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



    profile("getting Reflections...")



    val r = Reflections(
        ConfigurationBuilder()
            .useParallelExecutor(Runtime.getRuntime().availableProcessors())
            .forPackages("matt")
//            .setScanners(TypeAnnotationsScanner(),SubTypesScanner())
        /*this wasnt neccesary on mac*/
        /*ConfigurationBuilder().setScanners(SubTypesScanner())*/
    )

    var tt = System.nanoTime()
    var d = Duration.ofNanos(tt - t).toMillis()
    profile("getting Reflections took $d ms")
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
fun <T : Any> KClass<T>.subclasses() = subclassCache[this] as List<KClass<out T>>


fun Any.toStringBuilder(
    vararg props: KProperty<*>
): String {
    return toStringBuilder(props.associate { it.name to it.apply { isAccessible = true }.getter.call() })
}

fun Any.toStringBuilder(
    vararg kvPairs: Pair<String, Any?>
) = toStringBuilder(mapOf(*kvPairs))

fun Any.toStringBuilder(
    map: Map<String, Any?>
): String {
    return "[ " + this::class.simpleName!! + " " + map.entries.joinToString {
        it.key + "=" + it.value
            .toString()
    } + (if (map.isNotEmpty()) " ]" else "]")
}