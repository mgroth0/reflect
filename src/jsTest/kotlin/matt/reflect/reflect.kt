package matt.reflect

import matt.test.reportAndReThrowErrors
import org.w3c.dom.Document
import kotlin.test.Test
import kotlin.test.assertEquals

actual class SomeTests {
  @Test
  actual fun testClassForName() = reportAndReThrowErrors {
	assertEquals(String::class, classForName("kotlin.String"))
	assertEquals(Document::class, classForName("org.w3c.dom.Document"))
  }
}
