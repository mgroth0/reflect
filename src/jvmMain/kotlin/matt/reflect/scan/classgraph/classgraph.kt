package matt.reflect.scan.classgraph

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList
import io.github.classgraph.MethodInfo
import io.github.classgraph.MethodInfoList
import io.github.classgraph.ScanResult
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classname.jvmQualifiedClassName
import matt.reflect.pack.MATT_PACK
import matt.reflect.pack.Pack
import matt.reflect.scan.ClassScannerTool
import matt.reflect.scan.DEFAULT_INIT_CLASSES
import matt.reflect.scan.classgraph.ClassGraphW.Annotations
import matt.reflect.scan.classgraph.ClassGraphW.Methods
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction


class ClassGraphScannerTool(
    internal vararg val classLoaders: ClassLoader,
    internal val includeParentClassloaders: Boolean
) : ClassScannerTool {

    override fun KClass<out Annotation>.annotatedMattJFunctions() = classGraph {
        packs += MATT_PACK
        details += Annotations
        details += Methods
    }.scan {
        classesWithMethodAnnotation(this@annotatedMattJFunctions)
            .methods()
            .withAnnotation(this@annotatedMattJFunctions)
            .load()
    }

    override fun KClass<out Annotation>.annotatedMattJTypes() = classGraph {
        packs += MATT_PACK
        details += Annotations
    }.scan {
        classesWithAnnotation(this@annotatedMattJTypes).load()
    }

    override fun <T : Any> KClass<T>.subClasses(within: Pack) = classGraph {
        packs += within
        details += Annotations
    }.scan {
        subtypesOf(this@subClasses).loadKotlin()
    }

    override fun classNames(within: Pack?): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }

    override fun allClasses(
        within: Pack,
        initializeClasses: Boolean
    ): Set<Class<*>> = classGraph {
        packs += within
        /*details += Annotations*/
        if (initializeClasses) initClasses()
    }.scan {
        this.all().loadClasses()
    }.toSet()

    override fun findClass(qName: JvmQualifiedClassName) = classGraph {
        packs += MATT_PACK
        details += Annotations
    }.scan {
        find(qName)?.loadKotlin()
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }

    fun allMattClasses(initializeClasses: Boolean = DEFAULT_INIT_CLASSES) =
        allClasses(MATT_PACK, initializeClasses = initializeClasses)


}

private fun ClassGraphScannerTool.classGraph(
    cfg: ClassGraphW.() -> Unit
) = ClassGraphW(
    classLoaders = classLoaders,
    includeParentClassloaders = includeParentClassloaders
).apply(cfg)


private class ClassGraphW(
    vararg classLoaders: ClassLoader,
    includeParentClassloaders: Boolean
) {

    private var classGraph: ClassGraph = ClassGraph().overrideClassLoaders(*classLoaders)
        .enableClassInfo()
        .enableMemoryMapping()



        /*

        I do not know why I enabled this by default. I searched usages, and I cannot find anywhere where it would currently make sense to have this enabled.

        In particular, it needs to be disabled for `GradleTests.buildServicesAreNotTasks`. Otherwise, that scan will try to get classes from compileOnly gradle classes which are definitely not neccesary in that scan.

        .enableExternalClasses()
        */



        .ignoreClassVisibility().let {
            if (includeParentClassloaders) it else it.ignoreParentClassLoaders()
        }

    sealed interface Detail
    object Annotations : Detail
    object Methods : Detail
    object All : Detail
    object Fields : Detail

    val details = Details()

    inner class Details {
        operator fun plusAssign(detail: Detail) {
            classGraph = when (detail) {
                Annotations -> classGraph.enableAnnotationInfo()
                Methods     -> classGraph.enableMethodInfo().ignoreMethodVisibility()
                Fields      -> classGraph.enableFieldInfo().ignoreFieldVisibility()
                All         -> classGraph.enableAllInfo()
            }
        }
    }

    val packs = Packs()

    inner class Packs {
        operator fun plusAssign(pack: Pack) {
            classGraph = classGraph.acceptPackages("${pack.name}.*")
        }
    }

    fun initClasses() {
        classGraph = classGraph.initializeLoadedClasses()
    }

    fun <R> scan(
        op: ScanResultWrapper.() -> R
    ): R = classGraph.scan().use { scanResult ->
        ScanResultWrapper(scanResult).run(op)
    }
}

private class ScanResultWrapper(private val scanResult: ScanResult) {
    fun classesWithMethodAnnotation(annotation: KClass<out Annotation>): ClassInfoListWrapper<Any> {
        return ClassInfoListWrapper(scanResult.getClassesWithMethodAnnotation(annotation.java))
    }

    fun classesWithAnnotation(annotation: KClass<out Annotation>): ClassInfoListWrapper<Any> {
        return ClassInfoListWrapper(scanResult.getClassesWithAnnotation(annotation.java))
    }

    fun <T : Any> subtypesOf(type: KClass<out T>): ClassInfoListWrapper<T> {
        val superType = scanResult.getClassInfo(type.jvmQualifiedClassName.name)
        return if (superType.isInterface) {
            ClassInfoListWrapper(superType.classesImplementing)
        } else {
            ClassInfoListWrapper(superType.subclasses)
        }
    }

    fun find(name: JvmQualifiedClassName): ClassInfo? {
        return scanResult.getClassInfo(name.name)
    }

    fun all() = scanResult.allClasses

}

private class ClassInfoListWrapper<T : Any>(private val classInfoList: ClassInfoList) {
    fun methods() = classInfoList.asSequence().map { it.methodInfo }.wrap()


    @Suppress("UNCHECKED_CAST")
    fun load(): Set<Class<out T>> = classInfoList.loadClasses().toSet() as Set<Class<out T>>

    fun loadKotlin() = load().mapTo(mutableSetOf()) { it.kotlin }
}

private fun Sequence<MethodInfoList>.wrap() = MethodInfoWrapper(flatMap { it })

private class MethodInfoWrapper(private val methodInfos: Sequence<MethodInfo>) {

    fun withAnnotation(annotation: KClass<out Annotation>): MethodInfoWrapper {
        val jAnno = annotation.java
        return MethodInfoWrapper(methodInfos.filter { it.hasAnnotation(jAnno) })
    }

    fun load() = methodInfos.map { it.loadClassAndGetMethod() }.toSet()
    fun loadKotlin() = load().mapTo(mutableSetOf()) { it.kotlinFunction }
}

private fun ClassInfo.loadKotlin(): KClass<out Any> = loadClass().kotlin