package matt.reflect.reflections

import matt.lang.RUNTIME
import matt.log.debug
import org.reflections8.Reflections
import org.reflections8.util.ConfigurationBuilder
import java.time.Duration
import kotlin.reflect.KClass

@Synchronized fun <T: Any> KClass<T>.subclasses(): List<KClass<out T>> {
  @Suppress("UNCHECKED_CAST")
  return (subclassCache[this] ?: run {

	val subClasses = reflections
	  .getSubTypesOf(java)!!.map { it.kotlin }
	subclassCache[this] = subClasses
	subClasses
  }) as List<KClass<out T>>
}


val reflections by lazy {
  val t = System.nanoTime()
  debug("getting Reflections...")
  val r = Reflections(
	ConfigurationBuilder()
	  .useParallelExecutor(RUNTIME.availableProcessors())
	/*.forPackages("matt")*/
	/*.addScanners(MethodAnnotationsScanner())*/

  )
  val tt = System.nanoTime()
  val d = Duration.ofNanos(tt - t).toMillis()
  debug("getting Reflections took $d ms")
  r
}


private val subclassCache = mutableMapOf<KClass<*>, List<KClass<*>>>()