package matt.reflect.reflections

import matt.lang.NUM_LOGICAL_CORES
import matt.lang.go
import org.reflections8.Reflections
import org.reflections8.scanners.MethodAnnotationsScanner
import org.reflections8.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction

fun KClass<out Annotation>.annotatedMattKTypes(): List<KClass<out Any>> = annotatedMattJTypes().map { it.kotlin }

fun KClass<out Annotation>.annotatedMattKFunctions() = annotatedMattJFunctions().map { it.kotlinFunction }

fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method> =
    methodScanningMattConfig.reflection().getMethodsWithAnnotation(this)!!


fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>> = defaultMattConfig
    .reflection()
    .getTypesAnnotationWith(this)

@Synchronized
fun <T : Any> KClass<T>.mattSubClasses(classLoader: ClassLoader? = null) =
    subclasses(MATT_PACK, classLoader = classLoader)

@Synchronized
fun <T : Any> KClass<T>.subclasses(
    pack: String,
    classLoader: ClassLoader? = null
): List<KClass<out T>> {
    val searchParams = SubClassSearchParams(this, pack)
    val fromCache = if (classLoader == null) subclassCache[searchParams] else null
    @Suppress("UNCHECKED_CAST") return (fromCache ?: run {
        val cfg = ReflectionConfig(pack, classLoader = classLoader)
        val subClasses = cfg.reflection().getSubTypesOf(this)
        if (classLoader == null) {
            subclassCache[searchParams] = subClasses
        }
        subClasses
    }) as List<KClass<out T>>
}


private val defaultMattConfig = ReflectionConfig()
private val methodScanningMattConfig = ReflectionConfig(scanMethodAnnotations = true)

private data class ReflectionConfig(
    val pack: String = MATT_PACK,
    val scanMethodAnnotations: Boolean = false,
    val classLoader: ClassLoader? = null
) {
    fun reflection(): Reflection {
        if (classLoader != null) return Reflection(this)
        return synchronized(reflectionsCache) {
            reflectionsCache[this] ?: Reflection(this).also {
                reflectionsCache[this] = it
            }
        }
    }
}

private val reflectionsCache = mutableMapOf<ReflectionConfig, Reflection>()

private class Reflection(
    cfg: ReflectionConfig,
) {
    private val reflections by lazy {
        /*val t = System.nanoTime()
        debug("getting Reflections...")*/

        var config = ConfigurationBuilder().useParallelExecutor(NUM_LOGICAL_CORES)
        if (cfg.scanMethodAnnotations) {
            config = config.addScanners(MethodAnnotationsScanner())
        }


        config = config.forPackages(cfg.pack)

        cfg.classLoader?.go {
            config.classLoaders = Optional.of(arrayOf(it))
            error("can't get classloaders in reflections to work. Try ClassGraph?")
        }

//        println("config.classLoaders=${config.classLoaders.toList().joinToString { "${it}" }}")


        val r = Reflections(config)


        /*val tt = System.nanoTime()
        val d = Duration.ofNanos(tt - t).toMillis()
        debug("getting Reflections took $d ms")*/
        r
    }

    fun getSubTypesOf(kClass: KClass<*>) = reflections.getSubTypesOf(kClass.java).map { it.kotlin }

    fun getMethodsWithAnnotation(cls: KClass<out Annotation>) = reflections.getMethodsAnnotatedWith(
        cls.java
    )

    fun getTypesAnnotationWith(cls: KClass<out Annotation>) = reflections.getTypesAnnotatedWith(
        cls.java
    )

}

data class SubClassSearchParams(
    val cls: KClass<*>,
    val pack: String = MATT_PACK
)

private val subclassCache = mutableMapOf<SubClassSearchParams, List<KClass<*>>>()

private const val MATT_PACK = "matt"


