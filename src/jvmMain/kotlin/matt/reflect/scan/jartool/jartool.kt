package matt.reflect.scan.jartool

import matt.classload.Jar
import matt.classload.useJarClassGetter
import matt.collect.mapToSet
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

    override fun <T : Any> KClass<T>.subClasses(within: Set<Pack>): Set<KClass<out T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> KClass<T>.mostConcreteTypes(within: Set<Pack>): Set<KClass<out T>> {
        TODO("Not yet implemented")
    }


    override fun classNames(within: Set<Pack>?): Set<JvmQualifiedClassName> {
        require(within != null && within.isNotEmpty()) {
            TODO("can within be null or empty here? not sure")
        }
        val withins = within.mapToSet {
            "${it.asUnixFilePath().also { require(!it.endsWith("/")) }}/"
        }

        jarFile.use { jarFile ->
            return jarFile.entries().asSequence().mapNotNullTo(mutableSetOf()) { jarEntry ->
                if (
                    jarEntry.name.endsWith(".class")
                    && !jarEntry.name.endsWith("module-info.class")
                    && withins.any { jarEntry.name.startsWith(it) }
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
        TODO("Not yet implemented")
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }
}