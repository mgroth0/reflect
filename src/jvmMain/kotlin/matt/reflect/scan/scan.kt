package matt.reflect.scan

import matt.classload.Jar
import matt.classload.JvmClassGetter
import matt.classload.useJarClassGetter
import matt.lang.anno.optin.ExperimentalMattCode
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classpathwork.ClassPathWorker
import matt.lang.model.file.FsFile
import matt.reflect.pack.MATT_PACK
import matt.reflect.pack.Pack
import matt.reflect.scan.classgraph.ClassGraphScannerTool
import matt.reflect.scan.jartool.JarScannerTool
import matt.reflect.scan.matttool.MattScannerTool
import matt.reflect.scan.reflections.ReflectionsScannerTool
import java.lang.reflect.Method
import java.util.jar.JarFile
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction

private const val DEFAULT_INCLUDE_PARENT_CLASSLOADERS = true

fun systemScope() =
    ClassScope(ClassLoader.getSystemClassLoader(), includeParentClassloaders = DEFAULT_INCLUDE_PARENT_CLASSLOADERS)

@ExperimentalMattCode
fun platformScope() =
    ClassScope(ClassLoader.getPlatformClassLoader(), includeParentClassloaders = DEFAULT_INCLUDE_PARENT_CLASSLOADERS)

@ExperimentalMattCode
fun debugScope() = ClassScope(
    ClassLoader.getSystemClassLoader(),
    ClassLoader.getPlatformClassLoader(),
    includeParentClassloaders = true
)


fun JvmClassGetter.scanner() = ClassScope(*classLoaders)

interface ClassScanningScope

fun JarFile.readEachEntry() = sequence {
    val e = entries()
    while (e.hasMoreElements()) {
        yield(e.nextElement())
    }
}

class JarScope(val jar: FsFile) : ClassScanningScope {

    fun <R> usingJarScanner(op: ClassScannerTool.() -> R) = JarFile(jar.path).use {
        JarScannerTool(jar, it).run(op)
    }

    fun <R> useClassScanner(op: ClassScope.() -> R): R = useJarClassGetter(Jar(jar)) {
        scanner().run(op)
    }

}


class ClassScope internal constructor(
    vararg classLoaders: ClassLoader,
    val includeParentClassloaders: Boolean = DEFAULT_INCLUDE_PARENT_CLASSLOADERS
) : ClassPathWorker(*classLoaders), ClassScanningScope {
    fun usingReflections() =
        ReflectionsScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)

    fun usingClassGraph() = ClassGraphScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)
    fun usingMatt() = MattScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)
}


interface ClassScannerTool {
    fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>>
    fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method>
    fun <T : Any> KClass<T>.subClasses(within: Pack): Set<KClass<out T>>
    fun classNames(within: Pack?): Set<JvmQualifiedClassName>
    fun allClasses(
        within: Pack = MATT_PACK,
        initializeClasses: Boolean = DEFAULT_INIT_CLASSES
    ): Set<Class<*>>

    fun findClass(qName: JvmQualifiedClassName): KClass<*>?

    fun referencedClasses(): Set<JvmQualifiedClassName>

}

/*idk why this is true by default but I'd like to reverse it on a day I have more energy*/
const val DEFAULT_INIT_CLASSES = true

context(ClassScannerTool)
fun <T : Any> KClass<T>.mattSubClasses(): Set<KClass<out T>> = subClasses(MATT_PACK)

context(ClassScannerTool)
fun KClass<out Annotation>.annotatedMattKTypes(): List<KClass<out Any>> = annotatedMattJTypes().map { it.kotlin }

context(ClassScannerTool)
fun KClass<out Annotation>.annotatedMattKFunctions() = annotatedMattJFunctions().map { it.kotlinFunction }

