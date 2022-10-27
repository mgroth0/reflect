package matt.reflect

import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.test.assertEquals


actual class SomeTests {

  @Test
  @ExperimentalContracts
  fun testClassForName() = reportAndReThrowErrors {
	yesIUseTestLibs()
	assertEquals(String::class, classForName("kotlin.String"))
	assertEquals(Int::class, classForName("kotlin.Int"))
	assertEquals(Runtime::class, classForName("java.lang.Runtime"))
  }



}
