package matt.reflect.reflections

import matt.lang.RUNTIME
import matt.log.debug
import org.reflections8.Reflections
import org.reflections8.scanners.MethodAnnotationsScanner
import org.reflections8.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.time.Duration
import kotlin.reflect.KClass

fun KClass<out Annotation>.annotatedMattJFunctions(): Set<Method> =
  methodScanningMattConfig.reflection().getMethodsWithAnnotation(this)!!


fun KClass<out Annotation>.annotatedMattTypes(): List<KClass<*>> = defaultMattConfig
  .reflection()
  .getTypesAnnotationWith(this)


@Synchronized fun <T: Any> KClass<T>.subclasses(pack: String? = null): List<KClass<out T>> {
  val searchParams = if (pack != null) SubClassSearchParams(this, pack) else SubClassSearchParams(this)
  @Suppress("UNCHECKED_CAST") return (subclassCache[searchParams] ?: run {
	val cfg = if (pack != null) ReflectionConfig(pack) else ReflectionConfig()
	val subClasses = cfg.reflection().getSubTypesOf(this)
	subclassCache[searchParams] = subClasses
	subClasses
  }) as List<KClass<out T>>
}


private val defaultMattConfig = ReflectionConfig()
private val methodScanningMattConfig = ReflectionConfig(scanMethodAnnotations = true)

private data class ReflectionConfig(
  val pack: String = MATT_PACK,
  val scanMethodAnnotations: Boolean = false
) {
  fun reflection() = synchronized(reflectionsCache) {
	reflectionsCache[this] ?: Reflection(this).also {
	  reflectionsCache[this] = it
	}
  }
}

private val reflectionsCache = mutableMapOf<ReflectionConfig, Reflection>()

private class Reflection(
  cfg: ReflectionConfig,
) {
  private val reflections by lazy {
	val t = System.nanoTime()
	debug("getting Reflections...")

	var config = ConfigurationBuilder().useParallelExecutor(RUNTIME.availableProcessors())
	if (cfg.scanMethodAnnotations) {
	  config = config.addScanners(MethodAnnotationsScanner())
	}


	config = config.forPackages(cfg.pack)

	val r = Reflections(config)


	val tt = System.nanoTime()
	val d = Duration.ofNanos(tt - t).toMillis()
	debug("getting Reflections took $d ms")
	r
  }

  fun getSubTypesOf(kClass: KClass<*>) = reflections.getSubTypesOf(kClass.java).map { it.kotlin }

  fun getMethodsWithAnnotation(cls: KClass<out Annotation>) = reflections.getMethodsAnnotatedWith(
	cls.java
  )

  fun getTypesAnnotationWith(cls: KClass<out Annotation>) = reflections.getTypesAnnotatedWith(
	cls.java
  ).map { it.kotlin }

}

data class SubClassSearchParams(val cls: KClass<*>, val pack: String = MATT_PACK)

private val subclassCache = mutableMapOf<SubClassSearchParams, List<KClass<*>>>()

private const val MATT_PACK = "matt"