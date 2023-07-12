package matt.reflect

import kotlin.test.Test
import kotlin.test.assertEquals

expect class ReflectTests {

    fun testClassForName()

}


class CommonReflectTests {
    @Test
    fun aCommonTest() {
        assertEquals(Int::class.firstSimpleName(), "Int")
    }
}
