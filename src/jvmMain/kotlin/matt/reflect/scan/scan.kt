package matt.reflect.scan

import matt.classload.j.useJarClassGetter
import matt.classload.ja.Jar
import matt.classload.ja.JvmClassGetter
import matt.lang.anno.optin.ExperimentalMattCode
import matt.lang.model.file.AnyFsFile
import matt.reflect.pack.MATT_PACK
import matt.reflect.scan.jartool.JarScannerTool
import matt.reflect.scan.jcommon.ClassScannerTool
import matt.reflect.scan.jcommon.ClassScanningScope
import matt.reflect.scan.jcommon.ClassScope
import matt.reflect.scan.jcommon.DEFAULT_INCLUDE_PARENT_CLASSLOADERS
import java.util.jar.JarFile
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction



/*DOES NOT RESOLVE ON ANDROID, OR IN TESTS, OR SOMETHING.*/
@ExperimentalMattCode
fun platformScope() =
    ClassScope(ClassLoader.getPlatformClassLoader(), includeParentClassloaders = DEFAULT_INCLUDE_PARENT_CLASSLOADERS)

fun JvmClassGetter.scanner() = ClassScope(*classLoaders)

fun JarFile.readEachEntry() =
    sequence {
        val e = entries()
        while (e.hasMoreElements()) {
            yield(e.nextElement())
        }
    }

class JarScope(val jar: AnyFsFile) : ClassScanningScope {

    fun <R> usingJarScanner(op: ClassScannerTool.() -> R) =
        JarFile(jar.path).use {
            JarScannerTool(jar, it).run(op)
        }

    fun <R> useClassScanner(op: ClassScope.() -> R): R =
        useJarClassGetter(Jar(jar)) {
            scanner().run(op)
        }
}


context(ClassScannerTool)
fun <T : Any> KClass<T>.mattSubClasses(): Set<KClass<out T>> = subClasses(setOf(MATT_PACK))

context(ClassScannerTool)
fun KClass<out Annotation>.annotatedMattKTypes(): List<KClass<out Any>> = annotatedMattJTypes().map { it.kotlin }

context(ClassScannerTool)
fun KClass<out Annotation>.annotatedMattKFunctions() = annotatedMattJFunctions().map { it.kotlinFunction }

context(ClassScannerTool)
fun <T : Any> KClass<T>.mostConcreteMattImplementations(): Set<KClass<out T>> = mostConcreteTypes(setOf(MATT_PACK))
