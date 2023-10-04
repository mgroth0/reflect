package matt.reflect.scan.reflections

import matt.lang.NUM_LOGICAL_CORES
import matt.lang.classname.JvmQualifiedClassName
import matt.reflect.pack.MATT_PACK
import matt.reflect.pack.Pack
import matt.reflect.scan.ClassScannerTool
import org.reflections8.Reflections
import org.reflections8.scanners.MethodAnnotationsScanner
import org.reflections8.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass


class ReflectionsScannerTool(
    private vararg val classLoaders: ClassLoader,
    @Suppress("unused") val includeParentClassloaders: Boolean
) : ClassScannerTool {

    private val reflectionsCache = mutableMapOf<ReflectionConfig, Reflection>()

    override fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>> {
        return ReflectionConfig(
            pack = MATT_PACK,
            classLoaders = classLoaders.toList()
        ).reflection().getTypesAnnotationWith(this)
    }

    override fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method> = ReflectionConfig(
        pack = MATT_PACK,
        classLoaders = classLoaders.toList(),
        scanMethodAnnotations = true
    ).reflection().getMethodsWithAnnotation(this)

    private fun ReflectionConfig.reflection() = synchronized(reflectionsCache) {
        reflectionsCache.getOrPut(this) {
            Reflection(this)
        }
    }


    override fun findClass(qName: JvmQualifiedClassName): KClass<*>? {
        TODO("Not yet implemented")
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }


    @Synchronized
    override fun <T : Any> KClass<T>.subClasses(
        within: Pack,
    ): Set<KClass<out T>> {
        return (run {
            val cfg = ReflectionConfig(within, classLoaders = classLoaders.toList())
            val subClasses = cfg.reflection().getSubTypesOf(this)
            subClasses.toSet()
        })
    }

    override fun <T : Any> KClass<T>.mostConcreteTypes(within: Pack): Set<KClass<out T>> {
        TODO("Not yet implemented")
    }

    override fun classNames(within: Pack?): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }

    override fun allClasses(
        within: Pack,
        initializeClasses: Boolean
    ): Set<Class<*>> {
        TODO("Not yet implemented")
    }


}

private data class ReflectionConfig(
    val pack: Pack,
    val scanMethodAnnotations: Boolean = false,
    val classLoaders: List<ClassLoader>
)


private class Reflection(
    cfg: ReflectionConfig,
) {
    private val reflections by lazy {
        var config = ConfigurationBuilder().useParallelExecutor(NUM_LOGICAL_CORES)
        if (cfg.scanMethodAnnotations) {
            config = config.addScanners(MethodAnnotationsScanner())
        }
        config = config.forPackages(cfg.pack.name)
        println("WARNING!: Reflections ignores  the includeParentClassloaders property becase it doesn't know how to go one way or the other. Use ClassGraph?")
        config.classLoaders = Optional.of(cfg.classLoaders.toTypedArray())
//        cfg.classLoader?.go {
//            config.classLoaders = Optional.of(arrayOf(it))
        println("WARNING: can't get classloaders in reflections to work. Try ClassGraph?")
//        }
        Reflections(config)
    }

    fun <T : Any> getSubTypesOf(kClass: KClass<out T>): List<KClass<out T>> =
        reflections.getSubTypesOf(kClass.java).map { it.kotlin }

    fun getMethodsWithAnnotation(cls: KClass<out Annotation>): MutableSet<Method> = reflections.getMethodsAnnotatedWith(
        cls.java
    )

    fun getTypesAnnotationWith(cls: KClass<out Annotation>): MutableSet<Class<*>> = reflections.getTypesAnnotatedWith(
        cls.java
    )

}
