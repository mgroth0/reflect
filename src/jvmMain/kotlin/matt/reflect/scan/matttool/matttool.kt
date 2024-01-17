package matt.reflect.scan.matttool

import matt.lang.classname.JvmQualifiedClassName
import matt.reflect.pack.Pack
import matt.reflect.scan.ClassScannerTool
import java.lang.reflect.Method
import kotlin.reflect.KClass


class MattScannerTool(
    private vararg val classLoaders: ClassLoader,
    val includeParentClassloaders: Boolean
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
        TODO()
    }

    override fun allClasses(
        within: Set<Pack>,
        initializeClasses: Boolean
    ): Set<Class<*>> {
        TODO()
    }


    override fun findClass(qName: JvmQualifiedClassName): KClass<*>? {
        return findJClass(qName)?.kotlin
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO()
    }

    fun findJClass(qName: JvmQualifiedClassName): Class<*>? {
        classLoaders.forEach {
            val c = it.loadClass(qName.name)
            if (c != null) {
                return c
            }
        }
        return null
    }

}