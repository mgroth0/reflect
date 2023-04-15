package matt.reflect

import matt.lang.nametoclass.classForName
import matt.test.reportAndReThrowErrors
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


actual class ReflectTests {

    @Test
    actual fun testClassForName() = reportAndReThrowErrors {
        yesIUseTestLibs()
        assertEquals(String::class, classForName("kotlin.String"))
        assertEquals(String::class, classForName("kotlin.String?")) /*for kotlinx.serialization*/
        assertEquals(Int::class, classForName("kotlin.Int"))
        assertEquals(Runtime::class, classForName("java.lang.Runtime"))
    }


}
