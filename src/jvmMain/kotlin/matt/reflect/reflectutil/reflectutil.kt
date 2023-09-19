package matt.reflect.reflectutil

import matt.lang.anno.SeeURL
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classname.jvmQualifiedClassName
import java.lang.reflect.Method
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader.Kind.CLASS
import kotlin.reflect.jvm.internal.impl.load.kotlin.header.KotlinClassHeader.Kind.FILE_FACADE

private val theField: Method by lazy {
    Metadata::class.java.declaredMethods.first {
        it.name == "k"
    }
}

private val kotlinMetadataName = JvmQualifiedClassName("kotlin.Metadata")

@SeeURL("https://stackoverflow.com/a/39806722/6596010")
val Class<*>.kotlinMetadataValue
    get() = declaredAnnotations
        .firstOrNull { it.annotationClass.jvmQualifiedClassName == kotlinMetadataName }
        ?.let { theField.invoke(it) }?.let {
            KotlinClassHeader.Kind.getById(it as Int)
        }


fun Class<*>.isRegularKotlinClass(): Boolean {
    return kotlinMetadataValue == CLASS
}

fun Class<*>.isFileKotlinClass(): Boolean {
    return kotlinMetadataValue == FILE_FACADE
}


