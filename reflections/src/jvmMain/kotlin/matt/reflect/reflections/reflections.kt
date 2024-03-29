package matt.reflect.reflections


import matt.collect.itr.mapToArray
import matt.lang.classname.common.JvmQualifiedClassName
import matt.lang.j.NUM_LOGICAL_CORES
import matt.reflect.pack.MATT_PACK
import matt.reflect.pack.Pack
import matt.reflect.scan.jcommon.ClassScannerTool
import matt.reflect.scan.jcommon.ClassScope
import org.reflections8.Reflections
import org.reflections8.scanners.MethodAnnotationsScanner
import org.reflections8.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.Optional
import kotlin.reflect.KClass


fun ClassScope.usingReflections() =
    ReflectionsScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)

class ReflectionsScannerTool(
    private vararg val classLoaders: ClassLoader,
    @Suppress("unused") val includeParentClassloaders: Boolean
) : ClassScannerTool {

    private val reflectionsCache = mutableMapOf<ReflectionConfig, Reflection>()

    override fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>> =
        ReflectionConfig(
            pack = setOf(MATT_PACK),
            classLoaders = classLoaders.toList()
        ).reflection().getTypesAnnotationWith(this)

    override fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method> =
        ReflectionConfig(
            pack = setOf(MATT_PACK),
            classLoaders = classLoaders.toList(),
            scanMethodAnnotations = true
        ).reflection().getMethodsWithAnnotation(this)

    private fun ReflectionConfig.reflection() =
        synchronized(reflectionsCache) {
            reflectionsCache.getOrPut(this) {
                Reflection(this)
            }
        }


    override fun findClass(qName: JvmQualifiedClassName): KClass<*>? {
        TODO()
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO()
    }


    @Synchronized
    override fun <T : Any> KClass<T>.subClasses(
        within: Set<Pack>
    ): Set<KClass<out T>> = (
        run {
            val cfg = ReflectionConfig(within, classLoaders = classLoaders.toList())
            val subClasses = cfg.reflection().getSubTypesOf(this)
            subClasses.toSet()
        }
    )

    override fun <T : Any> KClass<T>.mostConcreteTypes(within: Set<Pack>): Set<KClass<out T>> {
        TODO()
    }

    override fun classNames(within: Set<Pack>?): Set<JvmQualifiedClassName> {
        TODO()
    }

    override fun allClasses(
        within: Set<Pack>,
        initializeClasses: Boolean
    ): Set<Class<*>> {
        TODO()
    }
}

private data class ReflectionConfig(
    val pack: Set<Pack>,
    val scanMethodAnnotations: Boolean = false,
    val classLoaders: List<ClassLoader>
)


private class Reflection(
    cfg: ReflectionConfig
) {
    private val reflections by lazy {
        var config = ConfigurationBuilder().useParallelExecutor(NUM_LOGICAL_CORES)
        if (cfg.scanMethodAnnotations) {
            config = config.addScanners(MethodAnnotationsScanner())
        }
        check(cfg.pack.isNotEmpty())
        config = config.forPackages(*cfg.pack.mapToArray { it.name })
        println("WARNING!: Reflections ignores  the includeParentClassloaders property becase it doesn't know how to go one way or the other. Use ClassGraph?")
        config.classLoaders = Optional.of(cfg.classLoaders.toTypedArray())
        println("WARNING: can't get classloaders in reflections to work. Try ClassGraph?")
        Reflections(config)
    }

    fun <T : Any> getSubTypesOf(kClass: KClass<out T>): List<KClass<out T>> =
        reflections.getSubTypesOf(kClass.java).map { it.kotlin }

    fun getMethodsWithAnnotation(cls: KClass<out Annotation>): MutableSet<Method> =
        reflections.getMethodsAnnotatedWith(
            cls.java
        )

    fun getTypesAnnotationWith(cls: KClass<out Annotation>): MutableSet<Class<*>> =
        reflections.getTypesAnnotatedWith(
            cls.java
        )
}
