package matt.reflect.reflectutil

import matt.lang.anno.SeeURL
import java.lang.reflect.Method

private val theField: Method by lazy {
    Metadata::class.java.declaredMethods.first {
        it.name == "k"
    }
}

@SeeURL("https://stackoverflow.com/a/39806722/6596010")
        /*modified by Matt*/
fun Class<*>.isRegularKotlinClass(): Boolean {
    return this.declaredAnnotations.any {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
                && theField.invoke(it) == 1
    }
}

fun Class<*>.isFileKotlinClass(): Boolean {
    return this.declaredAnnotations.any {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
                && theField.invoke(it) == 2
    }
}
