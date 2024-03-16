package matt.reflect.test.jcommon.access

import io.kotest.matchers.shouldBe
import matt.reflect.j.access
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.test.Test

class AccessTests {
    @Test
    fun access() {
        val obj = ClassWithAPrivateMethod()
        val cls = ClassWithAPrivateMethod::class
        val secretNumberFunction = cls.declaredMemberFunctions.first { it.name == "secretNumber" }
        val secretVariable = cls.declaredMemberProperties.first { it.name == "secretVariable" }
        assertThrows<IllegalCallableAccessException> {
            secretNumberFunction.call(obj)
        }
        assertThrows<IllegalCallableAccessException> {
            secretVariable.get(obj)
        }
        secretNumberFunction.access {
            call(obj) shouldBe 5
        }
        secretVariable.access {
            getter.call(obj) shouldBe "xyz"
        }
    }
}

private class ClassWithAPrivateMethod() {
    private fun secretNumber() = 5
    private val secretVariable = "xyz"
}
