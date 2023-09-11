package matt.reflect.scan

import matt.classload.Jar
import matt.classload.JvmClassGetter
import matt.classload.useJarClassGetter
import matt.lang.anno.SeeURL
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classpathwork.ClassPathWorker
import matt.lang.model.file.FilePath
import matt.reflect.pack.MATT_PACK
import matt.reflect.pack.Pack
import matt.reflect.scan.classgraph.ClassGraphScannerTool
import matt.reflect.scan.matttool.MattScannerTool
import matt.reflect.scan.reflections.ReflectionsScannerTool
import java.lang.reflect.Method
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction

private const val DEFAULT_INCLUDE_PARENT_CLASSLOADERS = true

fun systemScanner() =
    ClassScanner(ClassLoader.getSystemClassLoader(), includeParentClassloaders = DEFAULT_INCLUDE_PARENT_CLASSLOADERS)


fun JvmClassGetter.scanner() = ClassScanner(*classLoaders)

@SeeURL("https://www.baeldung.com/jar-file-get-class-names")
class JarScanner(private val jar: FilePath) {
    fun classNames(
        within: Pack
    ): Set<JvmQualifiedClassName> {
        val classNames: MutableSet<JvmQualifiedClassName> = HashSet()
        JarFile(jar.filePath).use { jarFile ->
            val e: Enumeration<JarEntry> = jarFile.entries()
            while (e.hasMoreElements()) {
                val jarEntry: JarEntry = e.nextElement()
                if (
                    jarEntry.name.endsWith(".class")
                    && !jarEntry.name.endsWith("module-info.class")
                    && jarEntry.name.startsWith("${within.asUnixFilePath().also { require(!it.endsWith("/")) }}/")
                ) {
                    val className: String = jarEntry.name
                        .replace("/", ".")
                        .replace(".class", "")
                    classNames.add(JvmQualifiedClassName(className))
                }
            }
            return classNames
        }
    }

    fun <R> useClassScanner(op: ClassScanner.() -> R): R = useJarClassGetter(Jar(jar)) {
        scanner().run(op)
    }

    fun loadAllClasses(within: Pack = MATT_PACK): Set<Class<*>> {
        val classNames = classNames(within)
        return useJarClassGetter(Jar(jar)) {
            classNames.mapTo(mutableSetOf()) {
                it.getJ()!!
            }
        }
    }
}

class ClassScanner internal constructor(
    vararg classLoaders: ClassLoader,
    val includeParentClassloaders: Boolean = DEFAULT_INCLUDE_PARENT_CLASSLOADERS
) : ClassPathWorker(*classLoaders) {
    fun usingReflections() =
        ReflectionsScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)

    fun usingClassGraph() = ClassGraphScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)
    fun usingMatt() = MattScannerTool(*classLoaders, includeParentClassloaders = includeParentClassloaders)
}


interface ClassScannerTool {
    fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>>
    fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method>
    fun <T : Any> KClass<T>.subClasses(within: Pack): Set<KClass<out T>>
    fun findClass(qName: JvmQualifiedClassName): KClass<*>?

}

context(ClassScannerTool)
fun <T : Any> KClass<T>.mattSubClasses(): Set<KClass<out T>> = subClasses(MATT_PACK)

context(ClassScannerTool)
fun KClass<out Annotation>.annotatedMattKTypes(): List<KClass<out Any>> = annotatedMattJTypes().map { it.kotlin }

context(ClassScannerTool)
fun KClass<out Annotation>.annotatedMattKFunctions() = annotatedMattJFunctions().map { it.kotlinFunction }

