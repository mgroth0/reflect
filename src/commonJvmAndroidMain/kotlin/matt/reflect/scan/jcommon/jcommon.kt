package matt.reflect.scan.jcommon

import matt.lang.anno.optin.ExperimentalMattCode
import matt.lang.classname.common.JvmQualifiedClassName
import matt.lang.classpathwork.j.ClassPathWorker
import matt.reflect.pack.MATT_PACK
import matt.reflect.pack.Pack
import matt.reflect.scan.classgraph.ClassGraphScannerTool
import matt.reflect.scan.expect.debugClassLoaders
import matt.reflect.scan.matttool.MattScannerTool
import java.lang.reflect.Method
import kotlin.reflect.KClass


internal const val DEFAULT_INCLUDE_PARENT_CLASSLOADERS = true


fun systemScope(
    includePlatformClassloader: Boolean = DEFAULT_INCLUDE_PARENT_CLASSLOADERS
) =
    ClassScope(ClassLoader.getSystemClassLoader(), includeParentClassloaders = includePlatformClassloader)






@ExperimentalMattCode
fun debugScope() =
    ClassScope(
        *debugClassLoaders().toTypedArray(),
        includeParentClassloaders = true
    )


interface ClassScanningScope
class ClassScope internal constructor(
    vararg classLoaders: ClassLoader,
    val includeParentClassloaders: Boolean = DEFAULT_INCLUDE_PARENT_CLASSLOADERS
) : ClassPathWorker(*classLoaders), ClassScanningScope

interface ClassScannerTool {
    fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>>
    fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method>
    fun <T : Any> KClass<T>.subClasses(within: Set<Pack>): Set<KClass<out T>>
    fun <T : Any> KClass<T>.mostConcreteTypes(within: Set<Pack>): Set<KClass<out T>>
    fun classNames(within: Set<Pack>?): Set<JvmQualifiedClassName>
    fun allClasses(
        within: Set<Pack> = setOf(MATT_PACK),
        initializeClasses: Boolean = DEFAULT_INIT_CLASSES
    ): Set<Class<*>>

    fun findClass(qName: JvmQualifiedClassName): KClass<*>?

    fun referencedClasses(): Set<JvmQualifiedClassName>
}

/*idk why this is true by default but I'd like to reverse it on a day I have more energy*/
const val DEFAULT_INIT_CLASSES = true
fun ClassScope.usingMatt() = MattScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)
fun ClassScope.usingClassGraph() =
    ClassGraphScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)
