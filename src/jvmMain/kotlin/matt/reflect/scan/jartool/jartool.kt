package matt.reflect.scan.jartool

import matt.classload.Jar
import matt.classload.useJarClassGetter
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.model.file.FsFile
import matt.reflect.pack.Pack
import matt.reflect.scan.ClassScannerTool
import java.lang.reflect.Method
import java.util.jar.JarFile
import kotlin.reflect.KClass


class JarScannerTool(
    private val jarFilePath: FsFile,
    private val jarFile: JarFile
) : ClassScannerTool {
    override fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>> {
        TODO("Not yet implemented")
    }

    override fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> KClass<T>.subClasses(within: Pack): Set<KClass<out T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> KClass<T>.mostConcreteTypes(within: Pack): Set<KClass<out T>> {
        TODO("Not yet implemented")
    }

    override fun classNames(within: Pack?): Set<JvmQualifiedClassName> {
        require(within != null) {
            TODO("can within be null here? not sure")
        }
        jarFile.use { jarFile ->
            return jarFile.entries().asSequence().mapNotNullTo(mutableSetOf()) { jarEntry ->
                if (
                    jarEntry.name.endsWith(".class")
                    && !jarEntry.name.endsWith("module-info.class")
                    && jarEntry.name.startsWith("${within.asUnixFilePath().also { require(!it.endsWith("/")) }}/")
                ) {
                    val className: String = jarEntry.name
                        .replace("/", ".")
                        .removeSuffix(".class")
                    JvmQualifiedClassName(className)
                } else null
            }
        }
    }

    override fun allClasses(
        within: Pack,
        initializeClasses: Boolean
    ): Set<Class<*>> {
        val classNames = classNames(within)
        return useJarClassGetter(Jar(jarFilePath)) {
            classNames.mapTo(mutableSetOf()) {
                it.getJ(initializeClasses) ?: error("could not get class: $it")
            }
        }
    }


    override fun findClass(qName: JvmQualifiedClassName): KClass<*>? {
        TODO("Not yet implemented")
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }
}