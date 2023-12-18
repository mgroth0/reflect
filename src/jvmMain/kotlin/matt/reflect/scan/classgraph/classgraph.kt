package matt.reflect.scan.classgraph

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList
import io.github.classgraph.ClassInfoList.ClassInfoFilter
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.MethodInfo
import io.github.classgraph.MethodInfoList
import io.github.classgraph.ScanResult
import matt.lang.anno.NotSynchronizedForPerformance
import matt.lang.anno.SeeURL
import matt.lang.assertions.require.requireNot
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classname.jvmQualifiedClassName
import matt.lang.shutdown.ShutdownContext
import matt.lang.shutdown.closingAtShutdown
import matt.reflect.pack.MATT_PACK
import matt.reflect.pack.Pack
import matt.reflect.scan.ClassScannerTool
import matt.reflect.scan.DEFAULT_INIT_CLASSES
import matt.reflect.scan.classgraph.ClassGraphW.Annotations
import matt.reflect.scan.classgraph.ClassGraphW.Methods
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction


class ClassGraphScannerTool(
    internal vararg val classLoaders: ClassLoader,
    internal val includeParentClassloaders: Boolean
) : ClassScannerTool {

    override fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method> = classGraph {
        packs += MATT_PACK
        details += Annotations
        details += Methods
    }.closingScan {
        classesWithMethodAnnotation(this@annotatedMattJFunctions).methods().withAnnotation(this@annotatedMattJFunctions)
            .load()
    }

    override fun KClass<out Annotation>.annotatedMattJTypes() = classGraph {
        packs += MATT_PACK
        details += Annotations
    }.closingScan {
        classesWithAnnotation(this@annotatedMattJTypes).load()
    }

    override fun <T : Any> KClass<T>.subClasses(within: Set<Pack>) = classGraph {
        within.forEach {
            packs += it
        }
        details += Annotations
    }.closingScan {
        subtypesOf(this@subClasses).loadKotlin()
    }

    override fun <T : Any> KClass<T>.mostConcreteTypes(within: Set<Pack>) = classGraph {
        within.forEach {
            packs += it
        }
        details += Annotations
    }.closingScan {
        mostConcreteTypesOf(this@mostConcreteTypes).loadKotlin()
    }

    override fun classNames(within: Set<Pack>?): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }

    override fun allClasses(
        within: Set<Pack>,
        initializeClasses: Boolean
    ): Set<Class<*>> = classGraph {
        within.forEach {
            packs += it
        }
        /*details += Annotations*/
        if (initializeClasses) initClasses()
    }.closingScan {
        this.all().load()
    }.toSet()

    override fun findClass(qName: JvmQualifiedClassName) = classGraph {
        packs += MATT_PACK
        details += Annotations
    }.closingScan {
        find(qName)?.loadKotlin()
    }

    override fun referencedClasses(): Set<JvmQualifiedClassName> {
        TODO("Not yet implemented")
    }

    fun allMattClasses(initializeClasses: Boolean = DEFAULT_INIT_CLASSES) =
        allClasses(setOf(MATT_PACK), initializeClasses = initializeClasses)

    inline fun <reified T1 : Any, reified T2 : Any> requireNoneAreBoth(
        pack: Pack
    ) = requireNoneAreBoth(
        T1::class.java, T2::class.java, pack
    )

    fun <T1 : Any, T2 : Any> requireNoneAreBoth(
        class1: Class<T1>,
        class2: Class<T2>,
        pack: Pack,
    ) {
        classGraph {
            packs += pack
        }.closingScan {
            requireNoneAreBoth(class1, class2)
        }


    }


    inline fun <reified G : Any, reified P : Any> requireNoGenericClassWithFirstParameter(
        pack: Pack,
    ) = requireNoGenericClassWithFirstParameter(
        G::class.java, P::class.java, pack
    )

    fun <G : Any, P : Any> requireNoGenericClassWithFirstParameter(
        genericClass: Class<G>,
        badParameter: Class<P>,
        pack: Pack,

        ) {
        classGraph {
            packs += pack
        }.closingScan {

            subtypesOf(genericClass).requireNoGenericClassWithFirstParameter(genericClass, badParameter)


        }
    }
}

@PublishedApi
internal val DEFAULT_EXCLUDE_CLASS_NAME = null

@PublishedApi
internal val DEFAULT_EXCLUDE_CLASS_NAMES = emptySet<String>()


fun ClassGraphScannerTool.classGraph(
    cfg: ClassGraphW.() -> Unit
) = ClassGraphW(
    classLoaders = classLoaders, includeParentClassloaders = includeParentClassloaders
).apply(cfg)


/*This is how ClassGraph checks if a classloader is a/the system classloader. I should do it this way too to be consistent and because its mostly likely the most robust. Maybe checking equality in otherwise dosn't work in some corner cases, and maybe this is also the most performant*/
fun ClassLoader.isAppClassLoader() = this::class.java.name == "jdk.internal.loader.ClassLoaders\$AppClassLoader"

class ClassGraphW internal constructor(
    vararg classLoaders: ClassLoader,
    includeParentClassloaders: Boolean
) {


    @SeeURL("https://github.com/classgraph/classgraph/issues/795")
    private var classGraph: ClassGraph =
        ClassGraph().overrideClassLoaders(*classLoaders).enableClassInfo().enableMemoryMapping()


            /*

            I do not know why I enabled this by default. I searched usages, and I cannot find anywhere where it would currently make sense to have this enabled.

            In particular, it needs to be disabled for `GradleTests.buildServicesAreNotTasks`. Otherwise, that scan will try to get classes from compileOnly gradle classes which are definitely not neccesary in that scan.

            .enableExternalClasses()
            */


            .ignoreClassVisibility().let {
                if (includeParentClassloaders) {/*SystemJarsAndModules is disabled by default. If I am "including parents", logical by that I mean to include platform classes */
                    it.enableSystemJarsAndModules()
                } else {
                    if (classLoaders.size == 1) {
                        if (classLoaders.single().isAppClassLoader()) {
                            @SeeURL("https://github.com/classgraph/classgraph/issues/795")/*technically we are including the parent classloader. However, enableSystemJarsAndModules() is disabled by default so I guess that is equivalent to not using the platform classloader? idk. */

                            it
                        } else {
                            it.ignoreParentClassLoaders()
                        }
                    } else {
                        error("not sure what exactly to do here yet")
                    }

                }
            }


            .disableModuleScanning()


    sealed interface Detail
    data object Annotations : Detail
    data object Methods : Detail
    data object All : Detail
    data object Fields : Detail

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

    fun <R> closingScan(
        op: ScanResultWrapper.() -> R
    ): R = openScan().use(op)

    context(ShutdownContext)
    fun scanAndCloseAtShutdown() = openScan().closingAtShutdown()

    private fun openScan() = ScanResultWrapper(classGraph.scan())

}


interface ScannedClasses<T : Any> : Collection<ClassInfo> {
    fun <R : T> subtypesOf(type: KClass<out R>): ScannedClasses<R>
    fun <R : T> subtypesOf(type: Class<out R>): ScannedClasses<R>

    fun filtered(predicate: ClassInfoFilter): ScannedClasses<T>
    fun <T1 : T, T2 : T> requireNoneAreBoth(
        class1: Class<T1>,
        class2: Class<T2>,
        excludeClassName: String? = DEFAULT_EXCLUDE_CLASS_NAME
    )

    fun <G : T, P : Any> requireNoGenericClassWithFirstParameter(
        class1: Class<G>,
        class2: Class<P>,
        excludeClassNames: Set<String> = DEFAULT_EXCLUDE_CLASS_NAMES
    )

    fun <R : Any> scannedClassesOf(vararg classes: KClass<out R>): ScannedClasses<R>

    fun loadKotlin(): Set<KClass<out T>>

    fun hasClassInfo(name: String): Boolean

    fun <R: T> empty(): ScannedClasses<R>

}

fun <T : Any, R : T> ScannedClasses<T>.mostConcreteTypesOf(
    type: KClass<out R>
): ScannedClasses<R> {
    if (!hasClassInfo(type.qualifiedName!!)) {
        return empty()
    }
    val r = subtypesOf(type).filtered {
        it.subclasses.isEmpty() && it.classesImplementing.isEmpty()
    }
    return if (r.isEmpty()) {
        scannedClassesOf(type)
    } else r
}

inline fun <reified T1 : Any, reified T2 : Any> ScannedClasses<Any>.requireNoneAreBoth(
    excludeClassName: String? = DEFAULT_EXCLUDE_CLASS_NAME
) = requireNoneAreBoth(
    class1 = T1::class.java,
    class2 = T2::class.java,
    excludeClassName = excludeClassName
)

inline fun <reified G : Any, reified P : Any> ScannedClasses<in G>.requireNoGenericClassWithFirstParameter(
    excludeClassNames: Set<String> = DEFAULT_EXCLUDE_CLASS_NAMES
) = requireNoGenericClassWithFirstParameter(
    G::class.java, P::class.java, excludeClassNames = excludeClassNames
)


abstract class ScannedClassesBase<T : Any> : ScannedClasses<T> {
    final override fun <T1 : T, T2 : T> requireNoneAreBoth(
        class1: Class<T1>,
        class2: Class<T2>,
        excludeClassName: String?
    ) {
        val subclassesOf1 = subtypesOf(class1)
        if (class2.isInterface) {
            subclassesOf1.forEach {
                if (it.name == excludeClassName) return@forEach
                requireNot(it.implementsInterface(class2)) {
                    "$it should not be both a $class1 and a $class2"
                }
            }
        } else {
            subclassesOf1.forEach {
                if (it.name == excludeClassName) return@forEach
                requireNot(it.extendsSuperclass(class2)) {
                    "$it should not be both a $class1 and a $class2"
                }
            }
        }
    }

    abstract protected val scan: ScanResultWrapper
    final override fun <R : Any> scannedClassesOf(vararg classes: KClass<out R>): ScannedClasses<R> {
        return ClassInfoListWrapper(
            ClassInfoList(classes.map { scan.getClassInfo(it.jvmQualifiedClassName.name) }), scan
        )
    }
}


@NotSynchronizedForPerformance
class ScanResultWrapper internal constructor(val scanResult: ScanResult) : ScannedClassesBase<Any>(), AutoCloseable {
    fun classesWithMethodAnnotation(annotation: KClass<out Annotation>): ClassInfoListWrapper<Any> {
        return classInfoListWrapper(scanResult.getClassesWithMethodAnnotation(annotation.java))
    }

    fun classesWithAnnotation(annotation: KClass<out Annotation>): ClassInfoListWrapper<Any> {
        return classInfoListWrapper(scanResult.getClassesWithAnnotation(annotation.java))
    }


    override fun <R : Any> subtypesOf(type: KClass<out R>) = subtypesOf<R>(type.java)
    override fun <R : Any> subtypesOf(type: Class<out R>): ClassInfoListWrapper<R> {
        return classInfoListWrapper(cachedSubtypesOf(type))
    }

    @NotSynchronizedForPerformance
    internal fun <R : Any> cachedSubtypesOf(type: Class<out R>): ClassInfoList {
        return cachedSubTypes.getOrPut(type.name) {
            if (type.isInterface) {
                scanResult.getClassesImplementing(type)
            } else {
                scanResult.getSubclasses(type)
            }
        }
    }

    @NotSynchronizedForPerformance
    private val cachedSubTypes = mutableMapOf<String, ClassInfoList>()

    fun find(name: JvmQualifiedClassName): ClassInfo? {
        return scanResult.getClassInfo(name.name)
    }

    fun all() = classInfoListWrapper<Any>(scanResult.allClasses)

    override fun loadKotlin(): Set<KClass<out Any>> {
        return all().loadKotlin()
    }

    override fun filtered(predicate: ClassInfoFilter) =
        classInfoListWrapper<Any>(scanResult.allClasses.filter(predicate))

    override val scan = this

    override val size: Int
        get() = all().size

    override fun contains(element: ClassInfo): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<ClassInfo>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): Iterator<ClassInfo> {
        return all().iterator()
    }


    private fun <T : Any> classInfoListWrapper(classInfoList: ClassInfoList) =
        ClassInfoListWrapper<T>(classInfoList, scan = this)

    override fun hasClassInfo(name: String) = scanResult.getClassInfo(name) != null
    override fun <R : Any> empty(): ScannedClasses<R> {
        return classInfoListWrapper(ClassInfoList())
    }

    internal fun getClassInfo(name: String) = scanResult.getClassInfo(name) ?: error("No class found with name $name")

    override fun <G : Any, P : Any> requireNoGenericClassWithFirstParameter(
        class1: Class<G>,
        class2: Class<P>,
        excludeClassNames: Set<String>
    ) {
        return all().requireNoGenericClassWithFirstParameter(class1, class2, excludeClassNames)
    }

    override fun close() {
        scanResult.close()
    }

}

private tailrec fun getTypeParamValue(
    next: ClassRefTypeSignature,
    targetClassName: String,
    getClassInfo: (String) -> ClassInfo
): String {
    val baseName = next.baseClassName
    if (baseName == targetClassName) {
        val aSet = mutableSetOf<String>()
        next.typeArguments.first().findReferencedClassNames(aSet)
        val className = aSet.single()
        return className
    } else {
        val nextCls = getClassInfo(baseName)
        val nextNext = nextCls.typeSignature.superclassSignature
        return getTypeParamValue(
            next = nextNext, targetClassName = targetClassName, getClassInfo = getClassInfo
        )
    }
}


class ClassInfoListWrapper<T : Any> internal constructor(
    private val classInfoList: ClassInfoList,
    override val scan: ScanResultWrapper,
) : List<ClassInfo> by classInfoList, ScannedClassesBase<T>() {



    fun methods() = classInfoList.asSequence().map { it.methodInfo }.wrap()

    @Suppress("UNCHECKED_CAST")
    fun load(): Set<Class<out T>> = classInfoList.loadClasses().toSet() as Set<Class<out T>>


    override fun loadKotlin() = load().mapTo(mutableSetOf()) { it.kotlin }
    override fun hasClassInfo(name: String): Boolean {
        return classInfoList.any { it.name == name }
    }

    override fun <R : T> empty(): ScannedClasses<R> {
        return classInfoListWrapper(ClassInfoList())
    }


    override fun <R : T> subtypesOf(type: KClass<out R>) = subtypesOf(type.java)
    override fun <R : T> subtypesOf(type: Class<out R>): ClassInfoListWrapper<R> {
        val cachedSubTypes = scan.cachedSubtypesOf(type)
        return classInfoListWrapper(cachedSubTypes.intersect(classInfoList))
    }


    override fun filtered(predicate: ClassInfoFilter): ScannedClasses<T> {
        return classInfoListWrapper(classInfoList.filter(predicate))
    }


    override fun <G : T, P : Any> requireNoGenericClassWithFirstParameter(
        class1: Class<G>,
        class2: Class<P>,
        excludeClassNames: Set<String>
    ) {
        val className1 = class1.name
        val theSubTypes = subtypesOf(class1)
        val className2 = class2.name

        if (class1.isInterface) {
            theSubTypes.forEach {
                if (it.name in excludeClassNames) return@forEach
                val typeSig = (it.typeSignature) ?: return@forEach

                /*it is recursive already! phew!*/
                val theParam = typeSig
                    .superinterfaceSignatures
                    .first {
                        it.baseClassName == className1
                    }.typeArguments.first()


                val aSet = mutableSetOf<String>()
                theParam.findReferencedClassNames(aSet)
                if (aSet.isNotEmpty()) {
                    val clsName = aSet.single()

                    require(clsName != className2) {
                        "expected type parameter to not be $className2"
                    }
                }


            }
        } else {
            theSubTypes.forEach {
                if (it.name in excludeClassNames) return@forEach
                val typeSig = it.typeSignature

                val candidateSignature = typeSig.superclassSignature


                val paramValue = getTypeParamValue(
                    next = candidateSignature, targetClassName = class1.name, getClassInfo = scan::getClassInfo
                )


                require(className2 != paramValue) {
                    "expected type parameter to not be $className2 "
                }

            }
        }
    }

    private fun <T : Any> classInfoListWrapper(classInfoList: ClassInfoList) =
        ClassInfoListWrapper<T>(classInfoList, scan = scan)

}

private fun Sequence<MethodInfoList>.wrap() = MethodInfoWrapper(flatMap { it })

class MethodInfoWrapper internal constructor(private val methodInfos: Sequence<MethodInfo>) {

    fun withAnnotation(annotation: KClass<out Annotation>): MethodInfoWrapper {
        val jAnno = annotation.java
        return MethodInfoWrapper(methodInfos.filter { it.hasAnnotation(jAnno) })
    }

    fun load(): Set<Method> = methodInfos.map { it.loadClassAndGetMethod() }.toSet()
    fun loadKotlin() = load().mapTo(mutableSetOf()) { it.kotlinFunction }

}

private fun ClassInfo.loadKotlin(): KClass<out Any> = loadClass().kotlin