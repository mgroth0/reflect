package matt.reflect

import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts


class SomeTests {

  @Test
  @ExperimentalContracts
  fun testClassForName() = reportAndReThrowErrors {
	yesIUseTestLibs()
	require(classForName("kotlin.String") == String::class)
	require(classForName("kotlin.Int") == Int::class)
  }
}

fun reportAndReThrowErrors(op: ()->Unit) {
  try {
	op()
  } catch (throwable: Throwable) {
	var e: Throwable? = throwable
	do {
	  e!!
	  println(e)
	  println(e.message)
	  println(e::class)
	  e.printStackTrace()
	  e = e.cause
	} while (e != null)
	throw throwable
  }

}
