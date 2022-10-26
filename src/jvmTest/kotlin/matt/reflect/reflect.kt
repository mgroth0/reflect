package matt.reflect

import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts


class SomeTests {

  @Test
  @ExperimentalContracts
  fun testClassForName() {
	yesIUseTestLibs()
	require(classForName("kotlin.String") == String::class)
	require(classForName("kotlin.Int") == Int::class)
  }
}
