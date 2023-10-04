package matt.reflect.reflectutil

import matt.collect.filterToSet
import matt.collect.itr.recurse.recurse
import matt.collect.mapToSet
import matt.lang.anno.SeeURL
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classname.jvmQualifiedClassName
import matt.reflect.prop.AttachedKProperty
import matt.reflect.prop.DetachedKPropertyWrapper
import matt.reflect.prop.KPropertyWrapper
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader.Kind.CLASS
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader.Kind.FILE_FACADE
import kotlin.reflect.typeOf

private val theField: Method by lazy {
    Metadata::class.java.declaredMethods.first {
        it.name == "k"
    }
}

private val kotlinMetadataName = JvmQualifiedClassName("kotlin.Metadata")

@SeeURL("https://stackoverflow.com/a/39806722/6596010")
val Class<*>.kotlinMetadataValue
    get() = declaredAnnotations.firstOrNull { it.annotationClass.jvmQualifiedClassName == kotlinMetadataName }
        ?.let { theField.invoke(it) }?.let {
            KotlinClassHeader.Kind.getById(it as Int)
        }

fun Class<*>.isRegularKotlinClass(): Boolean {
    return kotlinMetadataValue == CLASS
}

fun Class<*>.isFileKotlinClass(): Boolean {
    return kotlinMetadataValue == FILE_FACADE
}

fun <R : Any> R.classMemberPropertiesPlusInherited() = this::class.memberPropertiesPlusInherited().mapToSet {
    it.attachedTo(this)
}

fun <R : Any> KClass<out R>.memberPropertiesPlusInherited() = recurse(includeSelf = true) {
    @Suppress("UNCHECKED_CAST")
    it.superclasses as List<KClass<out R>>
}.flatMapTo(mutableSetOf()) {
    it.memberProperties
}.mapToSet { DetachedKPropertyWrapper<R, Any?>(it) }

@JvmName("filterReturnsToSet1")
inline fun <reified T> Set<KPropertyWrapper<*>>.filterReturnsToSet() = filterToSet {
    it.returns<T>()
}.mapToSet {
    @Suppress("UNCHECKED_CAST")
    it as KPropertyWrapper<T>
}

@JvmName("filterReturnsToSet2")
inline fun <R, reified T> Set<DetachedKPropertyWrapper<R, *>>.filterReturnsToSet() = filterToSet {
    it.returns<T>()
}.mapToSet {
    @Suppress("UNCHECKED_CAST")
    it as DetachedKPropertyWrapper<R, T>
}

@JvmName("filterReturnsToSet3")
inline fun <R, reified T> Set<AttachedKProperty<R, *>>.filterReturnsToSet() = filterToSet {
    it.returns<T>()
}.mapToSet {
    @Suppress("UNCHECKED_CAST")
    it as AttachedKProperty<R, T>
}

inline fun <reified T> KPropertyWrapper<*>.returns() = returnType.isSubtypeOf(typeOf<T>())


