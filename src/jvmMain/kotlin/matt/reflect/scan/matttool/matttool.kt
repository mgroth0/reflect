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
        TODO("Not yet implemented")
    }

    override fun <T : Any> KClass<T>.subClasses(within: Pack): Set<KClass<out T>> {
        TODO("Not yet implemented")
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


    override fun findClass(qName: JvmQualifiedClassName): KClass<*>? {
        return findJClass(qName)?.kotlin
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
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