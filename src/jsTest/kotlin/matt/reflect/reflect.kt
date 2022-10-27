package matt.reflect

import matt.test.reportAndReThrowErrors
import kotlin.test.Test

actual class SomeTests {
  @Test
  actual fun testClassForName() = reportAndReThrowErrors {
	/*It is not implemented in javascript yet*/
  }
}
