package matt.reflect.scan.jartool

import matt.classload.j.useJarClassGetter
import matt.classload.ja.Jar
import matt.collect.mapToSet
import matt.lang.classname.common.JvmQualifiedClassName
import matt.lang.model.file.AnyFsFile
import matt.reflect.pack.Pack
import matt.reflect.scan.jcommon.ClassScannerTool
import java.lang.reflect.Method
import java.util.jar.JarFile
import kotlin.reflect.KClass


class JarScannerTool(
    private val jarFilePath: AnyFsFile,
    private val jarFile: JarFile
) : ClassScannerTool {
    override fun KClass<out Annotation>.annotatedMattJTypes(): Set<Class<*>> {
        TODO()
    }

    override fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method> {
        TODO()
    }

    override fun <T : Any> KClass<T>.subClasses(within: Set<Pack>): Set<KClass<out T>> {
        TODO()
    }

    override fun <T : Any> KClass<T>.mostConcreteTypes(within: Set<Pack>): Set<KClass<out T>> {
        TODO()
    }


    override fun classNames(within: Set<Pack>?): Set<JvmQualifiedClassName> {
        require(within != null && within.isNotEmpty()) {
            TODO("can within be null or empty here? not sure")
        }
        val withins =
            within.mapToSet {
                "${it.asUnixFilePath().also { require(!it.endsWith("/")) }}/"
            }

        jarFile.use { jarFile ->
            return jarFile.entries().asSequence().mapNotNullTo(mutableSetOf()) { jarEntry ->
                if (
                    jarEntry.name.endsWith(".class")
                    && !jarEntry.name.endsWith("module-info.class")
                    && withins.any { jarEntry.name.startsWith(it) }
                ) {
                    val className: String =
                        jarEntry.name
                            .replace("/", ".")
                            .removeSuffix(".class")
                    JvmQualifiedClassName(className)
                } else null
            }
        }
    }

    override fun allClasses(
        within: Set<Pack>,
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
        TODO()
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO()
    }
}
