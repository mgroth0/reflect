package matt.reflect

import kotlin.test.Test
import kotlin.test.assertEquals


class CommonReflectTests {

    @Test
    fun simpleClassName() {
        assertEquals(Int::class.firstSimpleName(), "Int")
    }
}


