package matt.reflect

import matt.klib.dmap.withStoringDefault
import org.reflections8.Reflections
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible


val KClass<*>.hasNoArgsConstructor  /*straight from createInstance()*/
    get() = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) } != null


@Target(AnnotationTarget.CLASS)
annotation class ConstructedThroughReflection(val by: KClass<*>)

val os: String by lazy { System.getProperty("os.name") }
val ismac by lazy { os.startsWith("Mac") }
val isNewMac by lazy {
    ismac && run {
        val proc = ProcessBuilder("uname", "-m").start()
        BufferedReader(InputStreamReader(proc.inputStream)).readText().trim()
    } == "arm64"
}

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

fun KClass<out Annotation>.annotatedJTypes() = Reflections("matt").getTypesAnnotatedWith(
        this.java
)!!

fun KClass<out Annotation>.annotatedKTypes() = annotatedJTypes().map { it.kotlin }

fun testProtoTypeSucceeded(): Boolean {

    /*  if (ismac()) {
        *//*I should re-enable this useful logging at some point. It takes like a full second and I could optimize its usage.*//*
	*//*(Reflections::class.staticProperties.first { it.name == "log" } as KMutableProperty<*>).setter.call(
	  Reflections::class,
	  null
	)*//* *//*this must be through reflection or the expression can't compile without slf4j jar on classpath*//*
	*//*Reflections.log = null*//*
  }*/



  println("testing classes have hasNoArgsConstructor...")

    NoArgConstructor::class.annotatedKTypes()
            .forEach {

                if (!it.hasNoArgsConstructor) {
                    return false
                }
            }
    return true
}

private val subclassCache = mutableMapOf<KClass<*>, List<KClass<*>>>().withStoringDefault {
    /*if (ismac()) {
      (Reflections::class.staticProperties.first { it.name == "log" } as KMutableProperty<*>).setter.call(
        Reflections::class,
        null
      )
    }*/
    val skls =
            Reflections(
                    /*this wasnt neccesary on mac*/
                    /*ConfigurationBuilder().setScanners(SubTypesScanner())*/
            )
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