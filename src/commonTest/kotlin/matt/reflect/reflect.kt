package matt.reflect

import kotlin.test.Test
import kotlin.test.assertEquals

expect class ReflectTests {

    @Test
    fun testClassForName()

}


class CommonReflectTests {

    @Test
    fun simpleClassName() {
        assertEquals(Int::class.firstSimpleName(), "Int")
    }



}


