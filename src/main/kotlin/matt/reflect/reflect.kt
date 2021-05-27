package matt.reflect

import matt.klib.dmap.withStoringDefault
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty


val KClass<*>.hasNoArgsConstructor  /*straight from createInstance()*/
  get() = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) } != null


@Target(AnnotationTarget.CLASS)
annotation class ConstructedThroughReflection(val by: KClass<*>)


fun ismac() = System.getProperty("os.name").startsWith("Mac")

@Target(AnnotationTarget.CLASS)
annotation class NoArgConstructor

fun KClass<out Annotation>.annotatedJTypes() = Reflections("matt").getTypesAnnotatedWith(
  this.java
)!!

fun KClass<out Annotation>.annotatedKTypes() = annotatedJTypes().map { it.kotlin }

fun testProtoTypeSucceeded(): Boolean {

  if (ismac()) {
	/*I should re-enable this useful logging at some point. It takes like a full second and I could optimize its usage.*/
	Reflections.log = null
  }





  NoArgConstructor::class.annotatedKTypes()
	  .forEach {
		print("testing $it hasNoArgsConstructor... ")
		if (!it.hasNoArgsConstructor) {
		  return false
		}
	  }
  return true
}

private val subclassCache = mutableMapOf<KClass<*>, List<KClass<*>>>().withStoringDefault {
  Reflections.log = null
  val skls = Reflections().getSubTypesOf(it.java)!!.map { it.kotlin }
  println(skls)
  skls
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> KClass<T>.subclasses() = subclassCache[this] as List<KClass<out T>>


fun Any.toStringBuilder(
  vararg props: KProperty<*>
): String {
  return toStringBuilder(props.associate { it.name to it.getter.call() })
}

fun Any.toStringBuilder(
  vararg kvPairs: Pair<String, Any?>
) = toStringBuilder(mapOf(*kvPairs))

fun Any.toStringBuilder(
  map: Map<String, Any?>
): String {
  return "[ " + this::class.simpleName!! + " " + map.entries.joinToString {
	it.key + "=" + it.value
		.toString()
  } + (if (map.isNotEmpty()) " ]" else "]")
}