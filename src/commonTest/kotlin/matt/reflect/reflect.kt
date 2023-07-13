package matt.reflect

import matt.test.onlyIfBasic
import kotlin.test.Test
import kotlin.test.assertEquals

expect class ReflectTests {

    @Test
    fun testClassForName()

}


class CommonReflectTests {

    @Test
    fun simpleClassName() {
        onlyIfBasic()
        assertEquals(Int::class.firstSimpleName(), "Int")
    }


}


