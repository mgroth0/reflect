package matt.reflect.tostring

import matt.lang.tostring.BaseStringableClass
import matt.reflect.firstSimpleName


//fun Any.toStringBuilder(
//    map: Map<String, Any?> = mapOf()
//): String {
//    val realMap = map.toMutableMap()
//    if (realMap.isEmpty()) {
//        realMap["@"] = hashCode()
//    }
//    return this::class.firstSimpleName() + map.entries.joinToString(prefix = "[", postfix = "]") {
//        "${it.key}=${it.value}"
//    }
//}
//
//
//fun Any.toStringBuilder(
//    vararg kvPairs: Pair<String, Any?>
//) = toStringBuilder(mapOf(*kvPairs))


abstract class ReflectingStringableClass : BaseStringableClass() {
    final override fun className() = this::class.firstSimpleName()
}
